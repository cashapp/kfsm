package com.squareup.cash.kfsm

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class StateMachineVerifierTest : StringSpec({

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
