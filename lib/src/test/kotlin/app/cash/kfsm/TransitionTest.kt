package app.cash.kfsm

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TransitionTest : StringSpec({

  "cannot create an invalid state transition" {
    shouldThrow<IllegalArgumentException> { Transition(A, C) }
  }

  "cannot create an invalid state transition from a set of states" {
    shouldThrow<IllegalArgumentException> { Transition(setOf(B, A), C) }
  }

  "cannot create a transition from nothing" {
    shouldThrow<IllegalArgumentException> { Transition(emptySet(), C) }
  }

})
