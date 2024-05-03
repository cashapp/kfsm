package app.cash.kfsm

import app.cash.quiver.extensions.ErrorOr
import arrow.core.NonEmptySet
import arrow.core.nonEmptySetOf
import arrow.core.right

open class Transition<V: Value<V, S>, S : State>(val from: NonEmptySet<S>, val to: S) {

  init {
    from.filterNot { it.canDirectlyTransitionTo(to) }.let {
      require(it.isEmpty()) { "invalid transition(s): ${it.map { from -> "$from->$to" }}" }
    }
  }

  constructor(from: S, to: S) : this(nonEmptySetOf(from), to)

  open suspend fun effect(value: V): ErrorOr<V> = value.right()
}
