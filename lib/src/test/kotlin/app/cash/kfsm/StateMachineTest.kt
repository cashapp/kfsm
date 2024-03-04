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

  "Returns failure when not all states are encountered" {
    StateMachine.verify(Valid3) shouldBeLeft "Did not encounter [Valid1, Valid2]"
  }

  "Does not return failure when there is a cycle in the state machine" {
    StateMachine.verify(Cycle1).shouldBeRight()
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
    StateMachine.mermaid(Cycle3) shouldBeRight """
      |stateDiagram-v2
      |    [*] --> Cycle3
      |    Cycle1 --> Cycle2
      |    Cycle2 --> Cycle3
      |    Cycle3 --> Cycle1
    """.trimMargin()
  }
})

sealed class ValidState(to: () -> Set<ValidState>) : State(to)
data object Valid1 : ValidState({ setOf(Valid2, Valid3) })
data object Valid2 : ValidState({ setOf(Valid3) })
data object Valid3 : ValidState({ setOf(Valid4, Valid5) })
data object Valid4 : ValidState({ setOf() })
data object Valid5 : ValidState({ setOf() })


sealed class CycleState(to: () -> Set<CycleState>) : State(to)
data object Cycle1 : CycleState({ setOf(Cycle2) })
data object Cycle2 : CycleState({ setOf(Cycle3) })
data object Cycle3 : CycleState({ setOf(Cycle1) })
