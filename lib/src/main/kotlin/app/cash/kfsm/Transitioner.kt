package app.cash.kfsm

import app.cash.quiver.extensions.ErrorOr
import app.cash.quiver.extensions.flatTap
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.flatten
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right

open class Transitioner<V: Value<V, S>, S : State>(
  private val persist: suspend (V) -> ErrorOr<V> = { it.right() }
) {

  open suspend fun preHook(value: V, via: Transition<V, S>): ErrorOr<Unit> = Unit.right()

  open suspend fun postHook(from: S, value: V, via: Transition<V, S>): ErrorOr<Unit> = Unit.right()

  suspend fun transition(
    value: V,
    transition: Transition<V, S>
  ): ErrorOr<V> = when {
    transition.from.contains(value.state) -> doTheTransition(value, transition)
    // Self-cycled transitions will be effected by the first case.
    // If we still see a transition to self then this is a no-op.
    transition.to == value.state -> ignoreAlreadyCompletedTransition(value, transition)
    else -> InvalidStateTransition(transition, value).left()
  }

  private suspend fun doTheTransition(
    value: V,
    transition: Transition<V, S>
  ) = Either.catch {
    preHook(value, transition)
      .flatMap{ transition.effect(value) }
      .map { it.update(transition.to) }
      .flatMap { persist(it) }
      .flatTap { postHook(value.state, it, transition) }
  }.flatten()

  private fun ignoreAlreadyCompletedTransition(
    value: V,
    transition: Transition<V, S>
  ): ErrorOr<V> = value.update(transition.to).right()
}

