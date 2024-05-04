package app.cash.kfsm

interface Value<V: Value<V, S>, S : State> {
  val state: S
  fun update(newState: S): V
}
