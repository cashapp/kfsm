package app.cash.kfsm

import app.cash.quiver.extensions.ErrorOr
import arrow.core.NonEmptySet
import arrow.core.nonEmptySetOf
import arrow.core.right

open class Transition<S : State>(val from: NonEmptySet<S>, val to: S) {

  init {
    from.filterNot { it.canTransitionTo(to) }.let {
      require(it.isEmpty()) { "invalid transition(s): ${it.map { from -> "$from->$to" }}" }
    }
  }

  constructor(from: S, to: S) : this(nonEmptySetOf(from), to)

  open fun effect(value: Value<S>): ErrorOr<Value<S>> = value.right()
}
