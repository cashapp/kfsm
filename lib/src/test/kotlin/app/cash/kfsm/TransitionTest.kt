package app.cash.kfsm

import arrow.core.nonEmptySetOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class TransitionTest : StringSpec({

  "cannot create an invalid state transition" {
    shouldThrow<IllegalArgumentException> { Transition(A, C) }
  }

  "cannot create an invalid state transition from a set of states" {
    shouldThrow<IllegalArgumentException> { Transition(nonEmptySetOf(B, A), C) }
  }

})
