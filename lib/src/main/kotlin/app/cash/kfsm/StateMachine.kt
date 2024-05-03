package app.cash.kfsm

import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.superclasses

object StateMachine {

  /** Check your state machine covers all subtypes */
  fun <S : State> verify(head: S): Result<Set<State>> = verify(head, baseType(head))

  /** Render a state machine in Mermaid markdown */
  fun <S : State> mermaid(head: S): Result<String> = walkTree(head).map { states ->
    listOf("stateDiagram-v2", "[*] --> ${head::class.simpleName}").plus(
      states.toSet().flatMap { from ->
        from.subsequentStates.map { to -> "${from::class.simpleName} --> ${to::class.simpleName}" }
      }.toList().sorted()
    ).joinToString("\n    ")
  }

  private fun <S : State> verify(head: S, type: KClass<out S>): Result<Set<State>> =
    walkTree(head).mapCatching { seen ->
      val notSeen = type.sealedSubclasses.minus(seen.map { it::class }.toSet()).toList().sortedBy { it.simpleName }
      when {
        notSeen.isEmpty() -> seen
        else -> throw InvalidStateMachine(
          "Did not encounter [${notSeen.map { it.simpleName }.joinToString(", ")}]"
        )
      }
    }

  private fun walkTree(
    current: State,
    statesSeen: Set<State> = emptySet()
  ): Result<Set<State>> = runCatching {
    when {
      statesSeen.contains(current) -> statesSeen
      current.subsequentStates.isEmpty() -> statesSeen.plus(current)
      else -> current.subsequentStates.flatMap {
        walkTree(it, statesSeen.plus(current)).getOrThrow()
      }.toSet()
    }
  }

  @Suppress("UNCHECKED_CAST") private fun <S : State> baseType(s: S): KClass<out S> = s::class.allSuperclasses
    .find { it.superclasses.contains(State::class) }!! as KClass<out S>

}

data class InvalidStateMachine(override val message: String) : Exception(message)
