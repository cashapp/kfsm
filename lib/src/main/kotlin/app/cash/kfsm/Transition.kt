package app.cash.kfsm

open class Transition<V: Value<V, S>, S : State>(val from: Set<S>, val to: S) {

  init {
    require(from.isNotEmpty()) { "At least one from state must be defined" }
    from.filterNot { it.canDirectlyTransitionTo(to) }.let {
      require(it.isEmpty()) { "invalid transition(s): ${it.map { from -> "$from->$to" }}" }
    }
  }

  constructor(from: S, to: S) : this(setOf(from), to)

  open suspend fun effect(value: V): Result<V> = Result.success(value)
}
