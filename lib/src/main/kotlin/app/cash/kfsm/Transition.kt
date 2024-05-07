package app.cash.kfsm

open class Transition<V: Value<V, S>, S : State>(val from: States<S>, val to: S) {

  init {
    from.set.filterNot { it.canDirectlyTransitionTo(to) }.let {
      require(it.isEmpty()) { "invalid transition(s): ${it.map { from -> "$from->$to" }}" }
    }
  }

  constructor(from: S, to: S) : this(States(from), to)

  open suspend fun effect(value: V): Result<V> = Result.success(value)
}
