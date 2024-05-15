package app.cash.kfsm

open class Transition<V: Value<V, S>, S : State>(val from: States<S>, val to: S) {

  init {
    from.set.filterNot { it.canDirectlyTransitionTo(to) }.let {
      require(it.isEmpty()) { "invalid transition(s): ${it.map { from -> "$from->$to" }}" }
    }
  }

  constructor(from: S, to: S) : this(States(from), to)

  /** The effect executed when transitioning from [from] to [to]. */
  open fun effect(value: V): Result<V> = Result.success(value)

  /** The effect executed when transitioning from [from] to [to], but only when using `TransitionerAsync` */
  open suspend fun effectAsync(value: V): Result<V> = effect(value)
}
