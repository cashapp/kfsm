package com.squareup.cash.kfsm

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import kotlin.reflect.KClass

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

object StateMachine {

  /** Check your state machine covers all subtypes */
  fun <S : State> verify(head: S, kClass: KClass<S>) = walkTree(head).flatMap { seen ->
    val notSeen = kClass.sealedSubclasses.minus(seen.map { it::class }.toSet()).toList().sortedBy { it.simpleName }
    if (notSeen.isEmpty()) {
      seen.right()
    } else {
      "Did not encounter [${notSeen.map { it.simpleName }.joinToString(", ")}]".left()
    }
  }

  private fun walkTree(
    current: State,
    statesSeen: Set<State> = emptySet()
  ): Either<String, Set<State>> =

    when {
      statesSeen.contains(current) -> statesSeen.right()
      current.transitions.isEmpty() -> statesSeen.plus(current).right()
      else -> current.transitions.map { walkTree(it, statesSeen.plus(current)) }.reduce { a, b ->
        when {
          a.isLeft() -> a
          b.isLeft() -> b
          else -> a.flatMap { left -> b.map { right -> left.plus(right) } }
        }
      }
    }
}
