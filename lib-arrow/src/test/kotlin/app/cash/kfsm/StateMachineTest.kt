package app.cash.kfsm

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class StateMachineTest : StringSpec({

  "Returns the states when the machine is valid" {
    StateMachine.verify(Valid1) shouldBeRight setOf(
      Valid1,
      Valid2,
      Valid3,
      Valid4,
      Valid5
    )
  }

  "Can verify machines with self-loops" {
    StateMachine.verify(UniCycle1).shouldBeRight()
  }

  "Can verify machines with 2 party loops" {
    StateMachine.verify(BiCycle1).shouldBeRight()
  }

  "Can verify machines with 3+ party loops" {
    StateMachine.verify(TriCycle1).shouldBeRight()
  }

  "Returns failure when not all states are encountered" {
    StateMachine.verify(Valid3) shouldBeLeft "Did not encounter [Valid1, Valid2]"
  }

  "produces mermaid diagram source for non-cyclical state machine" {
    StateMachine.mermaid(Valid1) shouldBeRight """
      |stateDiagram-v2
      |    [*] --> Valid1
      |    Valid1 --> Valid2
      |    Valid1 --> Valid3
      |    Valid2 --> Valid3
      |    Valid3 --> Valid4
      |    Valid3 --> Valid5
    """.trimMargin()
  }

  "produces mermaid diagram source for cyclical state machine" {
    StateMachine.mermaid(TriCycle3) shouldBeRight """
      |stateDiagram-v2
      |    [*] --> TriCycle3
      |    TriCycle1 --> TriCycle2
      |    TriCycle2 --> TriCycle3
      |    TriCycle3 --> TriCycle1
    """.trimMargin()
  }
})

sealed class ValidState(to: () -> Set<ValidState>) : State(to)
data object Valid1 : ValidState({ setOf(Valid2, Valid3) })
data object Valid2 : ValidState({ setOf(Valid3) })
data object Valid3 : ValidState({ setOf(Valid4, Valid5) })
data object Valid4 : ValidState({ setOf() })
data object Valid5 : ValidState({ setOf() })

sealed class UniCycleState(to: () -> Set<UniCycleState>) : State(to)
data object UniCycle1 : UniCycleState({ setOf(UniCycle1) })

sealed class BiCycleState(to: () -> Set<BiCycleState>) : State(to)
data object BiCycle1 : BiCycleState({ setOf(BiCycle2) })
data object BiCycle2 : BiCycleState({ setOf(BiCycle1) })

sealed class TriCycleState(to: () -> Set<TriCycleState>) : State(to)
data object TriCycle1 : TriCycleState({ setOf(TriCycle2) })
data object TriCycle2 : TriCycleState({ setOf(TriCycle3) })
data object TriCycle3 : TriCycleState({ setOf(TriCycle1) })
