package app.cash.kfsm

import arrow.core.NonEmptySet
import arrow.core.nonEmptySetOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class TransitionTest : StringSpec({

  "cannot create an invalid state transition" {
    shouldThrow<IllegalArgumentException> { LetterTransition(A, C) }
  }

  "cannot create an invalid state transition from a set of states" {
    shouldThrow<IllegalArgumentException> { LetterTransition(nonEmptySetOf(B, A), C) }
  }

})

open class LetterTransition(from: NonEmptySet<Char>, to: Char): Transition<Letter, Char>(from, to) {
  constructor(from: Char, to: Char) : this(nonEmptySetOf(from), to)

  val specificToThisTransitionType: String = "$from -> $to"
}

