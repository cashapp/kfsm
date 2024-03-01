package app.cash.kfsm

interface Value<S : State> {
  val state: S
  fun update(newState: S): Value<S>
}
