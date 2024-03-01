package app.cash.kfsm

import app.cash.quiver.extensions.ErrorOr
import app.cash.quiver.extensions.flatTap
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.flatten
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right

open class Transitioner<S : State>(
  private val persist: (Value<S>) -> ErrorOr<Value<S>> = { it.right() }
) {

  open fun preHook(value: Value<S>): ErrorOr<Unit> = Unit.right()

  open fun postHook(value: Value<S>): ErrorOr<Unit> = Unit.right()

  fun transition(
    value: Value<S>,
    transition: Transition<S>
  ): ErrorOr<Value<S>> = when {
    transition.from.contains(value.state) -> doTheTransition(value, transition)
    // Self-cycled transitions will be effected by the first case.
    // If we still see a transition to self then this is a no-op.
    transition.to == value.state -> ignoreAlreadyCompletedTransition(value, transition)
    else -> InvalidStateTransition(transition, value).left()
  }

  private fun doTheTransition(
    value: Value<S>,
    transition: Transition<S>
  ) = Either.catch {
    preHook(value)
      .flatMap{ transition.effect(value) }
      .map { it.update(transition.to) }
      .flatMap { persist(it) }
      .flatTap { postHook(it) }
  }.flatten()

  private fun ignoreAlreadyCompletedTransition(
    value: Value<S>,
    transition: Transition<S>
  ) = value.update(transition.to).right()
}

