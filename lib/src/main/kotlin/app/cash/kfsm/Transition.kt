package app.cash.kfsm

import app.cash.quiver.extensions.ErrorOr
import arrow.core.right

open class Transition<S : State>(val from: Set<S>, val to: S) {

  init {
    require(from.isNotEmpty()) { "from must not be empty" }
    from.filterNot { it.canTransitionTo(to) }.let {
      require(it.isEmpty()) { "invalid transition(s): ${it.map { from -> "$from->$to" }}" }
    }
  }

  constructor(from: S, to: S) : this(setOf(from), to)

  open fun effect(value: Value<S>): ErrorOr<Value<S>> = value.right()
}
