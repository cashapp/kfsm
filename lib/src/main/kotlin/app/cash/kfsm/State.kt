package app.cash.kfsm

open class State(transitionsFn: () -> Set<State>) {

  /** all states that can be transitioned to directly from this state */
  val subsequentStates: Set<State> by lazy { transitionsFn() }

  /** all states that are reachable from this state */
  val reachableStates: Set<State> by lazy { expand() }

  /**
   * Whether this state can transition to the given other state.
   */
  open fun canDirectlyTransitionTo(other: State): Boolean = subsequentStates.contains(other)

  /**
   * Whether this state could directly or indirectly transition to the given state.
   */
  open fun canEventuallyTransitionTo(other: State): Boolean = reachableStates.contains(other)

  private fun expand(found: Set<State> = emptySet()): Set<State> =
    subsequentStates.minus(found).flatMap {
      it.expand(subsequentStates + found) + it
    }.toSet().plus(found)
}
