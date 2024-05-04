package app.cash.kfsm

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.superclasses

object StateMachine {

  /** Check your state machine covers all subtypes */
  fun <S : State> verify(head: S) = verify(head, baseType(head))

  /** Render a state machine in Mermaid markdown */
  fun <S : State> mermaid(head: S): Either<String, String> = walkTree(head).map { states ->
    listOf("stateDiagram-v2", "[*] --> ${head::class.simpleName}").plus(
      states.toSet().flatMap { from ->
        from.subsequentStates.map { to -> "${from::class.simpleName} --> ${to::class.simpleName}" }
      }.toList().sorted()
    ).joinToString("\n    ")
  }

  private fun <S : State> verify(head: S, type: KClass<out S>) = walkTree(head).flatMap { seen ->
    val notSeen = type.sealedSubclasses.minus(seen.map { it::class }.toSet()).toList().sortedBy { it.simpleName }
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
      current.subsequentStates.isEmpty() -> statesSeen.plus(current).right()
      else -> current.subsequentStates.map { walkTree(it, statesSeen.plus(current)) }.reduce { a, b ->
        when {
          a.isLeft() -> a
          b.isLeft() -> b
          else -> a.flatMap { left -> b.map { right -> left.plus(right) } }
        }
      }
    }

  @Suppress("UNCHECKED_CAST") private fun <S : State> baseType(s: S): KClass<out S> = s::class.allSuperclasses
    .find { it.superclasses.contains(State::class) }!! as KClass<out S>
}
