package app.cash.kfsm

import arrow.core.nonEmptySetOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

typealias LetterTransition = Transition<Letter, Char>

class TransitionTest : StringSpec({

  "cannot create an invalid state transition" {
    shouldThrow<IllegalArgumentException> { LetterTransition(A, C) }
  }

  "cannot create an invalid state transition from a set of states" {
    shouldThrow<IllegalArgumentException> { LetterTransition(nonEmptySetOf(B, A), C) }
  }

})
