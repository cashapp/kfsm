package com.squareup.cash.kfsm

import com.squareup.cash.kfsm.exemplar.Asleep
import com.squareup.cash.kfsm.exemplar.Awake
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class StateMachineMermaidTest : StringSpec({

  "writes valid mermaid for ValidState" {
    StateMachine.mermaid(Valid1) shouldBeRight """stateDiagram-v2
    |    [*] --> Valid1
    |    Valid1 --> Valid2
    |    Valid1 --> Valid3
    |    Valid2 --> Valid3
    |    Valid3 --> Valid4
    |    Valid3 --> Valid5
    """.trimMargin()
  }

  "writes valid mermaid for HamsterState" {
    StateMachine.mermaid(Asleep) shouldBeRight """stateDiagram-v2
    |    [*] --> Asleep
    |    Asleep --> Awake
    |    Awake --> Eating
    |    Eating --> Asleep
    |    Eating --> Resting
    |    Eating --> RunningOnWheel
    |    Resting --> Asleep
    |    RunningOnWheel --> Asleep
    |    RunningOnWheel --> Resting
    """.trimMargin()
  }

})
