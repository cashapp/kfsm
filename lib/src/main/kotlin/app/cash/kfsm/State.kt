package app.cash.kfsm

open class State(transitionsFn: () -> Set<State>) {

  /** all states that can be transitioned to directly from this state */
  val transitions: Set<State> by lazy { transitionsFn() }

  /** all states that are reachable from this state */
  val reachable: Set<State> by lazy { expand() }

  /**
   * Whether this state can transition to the given other state.
   */
  open fun canTransitionTo(other: State): Boolean = transitions.contains(other)

  /**
   * Whether this state could directly or indirectly transition to the given state.
   */
  open fun canReach(other: State): Boolean = reachable.contains(other)

  private fun expand(found: Set<State> = emptySet()): Set<State> =
    transitions.minus(found).flatMap {
      it.expand(transitions + found) + it
    }.toSet().plus(found)
}
