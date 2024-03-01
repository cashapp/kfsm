package app.cash.kfsm

class InvalidStateTransition(transition: Transition<*>, value: Value<*>) : Exception(
  "Value cannot transition ${transition.from} to ${transition.to}, because it is currently ${value.state}"
)
