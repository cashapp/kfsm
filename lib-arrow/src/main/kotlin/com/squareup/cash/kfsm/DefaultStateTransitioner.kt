package com.squareup.cash.kfsm

import app.cash.quiver.asEither
import app.cash.quiver.extensions.ErrorOr
import app.cash.quiver.extensions.OutcomeOf
import app.cash.quiver.toOutcome
import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.Some
import arrow.core.flatten
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import mu.KotlinLogging

class DefaultStateTransitioner<V : Transitionable<S>, S : State, F : TransitionFailure, N : NotificationType>(
  /** Update the value to the new state */
  private val update: suspend (V, Transition<V, S, F, N>) -> ErrorOr<V>,

  /** Perform any notifications as a side effect of a successful transition */
  private val notifyOnSuccess: suspend (V, Set<N>, Transition<V, S, F, N>) -> ErrorOr<Unit> =
    { _, _, _ -> Unit.right() },

  /**
   * A strategy to lock the value so that it cannot be transitioned concurrently
   * - Some indicates the value was locked.
   * - None indicates the value was not locked.
   */
  private val lock: suspend (V) -> OutcomeOf<V> = { v -> Some(v).toOutcome() },

  /** A strategy to unlock the value in the event of an error */
  private val unlockOnError: suspend (V) -> ErrorOr<Unit> = { Unit.right() }

) : StateTransitioner<V, S, F, N> {
  private val logger = KotlinLogging.logger {}

  override suspend fun transition(value: V, transition: Transition<V, S, F, N>): Either<F, V> =
    when {
      transition.from.contains(value.state) -> doTheTransition(value, transition)
      // Self-cycled transitions will be effected by the first case.
      // If we still see a transition to self then this is a no-op.
      transition.to == value.state -> ignoreAlreadyCompletedTransition(value, transition)
      else -> failBadTransition(value, transition)
    }

  /* The value is in the expected state for the provided transition. Let's do it! */
  private suspend fun doTheTransition(
    value: V,
    transition: Transition<V, S, F, N>
  ): Either<F, V> =
    either {
      val lockedValue = lock(value)
        .asEither { LockingFailure(value.state, transition.to) }
        .mapLeft { transition.makeFailure(value, effectCompleted = false, updateCompleted = false, it) }
        .bind()

      val effectedValue = catch { transition.effect(lockedValue) }
        .getOrElse { transition.makeFailure(value, effectCompleted = false, updateCompleted = false, it).left() }
        .onLeft { unlockOnError(lockedValue) }
        .bind()

      val updatedValue = catch { update.invoke(effectedValue, transition) }.flatten()
        .mapLeft { transition.makeFailure(effectedValue, effectCompleted = true, updateCompleted = false, it) }
        .onLeft { unlockOnError(lockedValue) }
        .bind()
        .also { value ->
          catch {
            notifyOnSuccess.invoke(value, transition.notificationsTypes(lockedValue.state), transition)
          }.mapLeft { transition.makeFailure(value, effectCompleted = true, updateCompleted = true, it) }.bind()
        }

      updatedValue
    }.onLeft {
      logger.warn(
        it.getUnderlying().getOrNull()
      ) { "Unexpected error performing transition [transitionError=$it]" }
    }

  /** The value has previously done the provided transition  */
  private fun ignoreAlreadyCompletedTransition(
    value: V,
    transition: Transition<V, S, F, N>
  ): Either<F, V> {
    logger.info { "Skipping ${transition::class.simpleName} [state=${value.state}][value=$value]" }
    return value.right()
  }

  /** Something is wrong. Value is in the wrong state for this transition to apply */
  private fun failBadTransition(
    value: V,
    transition: Transition<V, S, F, N>
  ): Either<F, V> = transition.makeFailure(
    value = value,
    effectCompleted = false,
    updateCompleted = false,
    cause = InvalidStateTransition(value.state, transition.from, transition.to)
  ).left()
}

class InvalidStateTransition(current: State, from: Set<State>, to: State) : Throwable(
  "State cannot transition $from➜$to, because it is currently $current"
)

class LockingFailure(current: State, to: State) : Throwable(
  "State cannot obtain lock for transition ${current::class.simpleName}➜${to::class.simpleName}"
)
