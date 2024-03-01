package app.cash.kfsm

import arrow.core.nonEmptySetOf
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class InvalidStateTransitionTest : StringSpec({
  "with single from-state has correct message" {
    InvalidStateTransition(Transition(A, B), Letter(E)).message shouldBe
      "Value cannot transition {A} to B, because it is currently E"
  }

  "with many from-states has correct message" {
    InvalidStateTransition(Transition(nonEmptySetOf(C, B), D), Letter(E)).message shouldBe
      "Value cannot transition {B, C} to D, because it is currently E"
  }
})
