package app.cash.kfsm

interface Value<V: Value<V, S>, S : State<S>> {
  val state: S
  fun update(newState: S): V
}
