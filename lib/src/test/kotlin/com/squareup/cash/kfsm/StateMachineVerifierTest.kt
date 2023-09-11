package com.squareup.cash.kfsm

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec

class StateMachineVerifierTest : StringSpec({

  "Returns the states when the machine is valid" {
    StateMachine.verify(Valid1, ValidState::class) shouldBeRight setOf(
      Valid1,
      Valid2,
      Valid3,
      Valid4,
      Valid5
    )
  }

  "Returns failure when not all states are encountered" {
    StateMachine.verify(Valid3, ValidState::class) shouldBeLeft "Did not encounter [Valid1, Valid2]"
  }

  "Does not return failure when there is a cycle in the state machine" {
    StateMachine.verify(Cycle1, CycleState::class).shouldBeRight()
  }
})

sealed class ValidState(to: () -> Set<ValidState>) : State(to)
object Valid1 : ValidState({ setOf(Valid2, Valid3) })
object Valid2 : ValidState({ setOf(Valid3) })
object Valid3 : ValidState({ setOf(Valid4, Valid5) })
object Valid4 : ValidState({ setOf() })
object Valid5 : ValidState({ setOf() })

sealed class CycleState(to: () -> Set<CycleState>) : State(to)
object Cycle1 : CycleState({ setOf(Cycle2) })
object Cycle2 : CycleState({ setOf(Cycle3) })
object Cycle3 : CycleState({ setOf(Cycle1) })
