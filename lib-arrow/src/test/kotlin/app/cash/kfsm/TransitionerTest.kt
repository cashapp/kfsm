package app.cash.kfsm

import app.cash.quiver.extensions.ErrorOr
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class TransitionerTest : StringSpec({

  fun transitioner(
    pre: (Letter, LetterTransition) -> ErrorOr<Unit> = { _, _ -> Unit.right() },
    post: (Char, Letter, LetterTransition) -> ErrorOr<Unit> = { _, _, _ -> Unit.right() },
    persist: (Letter) -> ErrorOr<Letter> = { it.right() },
  ) = object : Transitioner<LetterTransition, Letter, Char>(persist) {
    var preHookExecuted = 0
    var postHookExecuted = 0

    override suspend fun preHook(value: Letter, via: LetterTransition): ErrorOr<Unit> =
      pre(value, via).also { preHookExecuted += 1 }

    override suspend fun postHook(from: Char, value: Letter, via: LetterTransition): ErrorOr<Unit> =
      post(from, value, via).also { postHookExecuted += 1 }
  }

  fun transition(from: Char = A, to: Char = B) = object : LetterTransition(from, to) {
    var effected = 0
    override suspend fun effect(value: Letter): ErrorOr<Letter> {
      effected += 1
      return value.update(to).right()
    }
  }

  "effects valid transition" {
    val transition = transition(from = A, to = B)
    val transitioner = transitioner()

    transitioner.transition(Letter(A), transition) shouldBeRight Letter(B)

    transitioner.preHookExecuted shouldBe 1
    transition.effected shouldBe 1
    transitioner.postHookExecuted shouldBe 1
  }

  "ignores completed transition" {
    val transition = transition(from = A, to = B)
    val transitioner = transitioner()

    transitioner.transition(Letter(B), transition) shouldBeRight Letter(B)

    transitioner.preHookExecuted shouldBe 0
    transition.effected shouldBe 0
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error on invalid transition" {
    val transition = transition(from = A, to = B)
    val transitioner = transitioner()

    transitioner.transition(Letter(C), transition).shouldBeLeft()
      .shouldHaveMessage("Value cannot transition {A} to B, because it is currently C")

    transitioner.preHookExecuted shouldBe 0
    transition.effected shouldBe 0
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when preHook errors" {
    val error = RuntimeException("preHook error")

    val transition = transition(from = A, to = B)
    val transitioner = transitioner(pre = { _, _ -> error.left() })

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transitioner.preHookExecuted shouldBe 1
    transition.effected shouldBe 0
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when effect errors" {
    val error = RuntimeException("effect error")

    val transition = object : LetterTransition(A, B) {
      override suspend fun effect(value: Letter): ErrorOr<Letter> = error.left()
    }
    val transitioner = transitioner()

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transitioner.preHookExecuted shouldBe 1
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when postHook errors" {
    val error = RuntimeException("postHook error")

    val transition = transition(from = A, to = B)
    val transitioner = transitioner(post = { _, _, _ -> error.left() })

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transition.effected shouldBe 1
    transitioner.preHookExecuted shouldBe 1
    transitioner.postHookExecuted shouldBe 1
  }

  "returns error when preHook throws" {
    val error = RuntimeException("preHook error")

    val transition = transition(from = A, to = B)
    val transitioner = transitioner(pre = { _, _ -> throw error })

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transition.effected shouldBe 0
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when effect throws" {
    val error = RuntimeException("effect error")

    val transition = object : LetterTransition(A, B) {
      override suspend fun effect(value: Letter): ErrorOr<Letter> = throw error
    }
    val transitioner = transitioner()

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transitioner.preHookExecuted shouldBe 1
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when postHook throws" {
    val error = RuntimeException("postHook error")

    val transition = transition(from = A, to = B)
    val transitioner = transitioner(post = { _, _, _ -> throw error })

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transition.effected shouldBe 1
    transitioner.preHookExecuted shouldBe 1
  }

  "returns error when persist fails" {
    val error = RuntimeException("persist error")

    val transition = transition(from = A, to = B)
    val transitioner = transitioner(persist = { error.left() })

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transitioner.preHookExecuted shouldBe 1
    transition.effected shouldBe 1
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when persist throws" {
    val error = RuntimeException("persist error")

    val transition = transition(from = A, to = B)
    val transitioner = transitioner(persist = { throw error })

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transitioner.preHookExecuted shouldBe 1
    transition.effected shouldBe 1
    transitioner.postHookExecuted shouldBe 0
  }

  "can transition multiple times" {
    val aToB = transition(A, B)
    val bToC = transition(B, C)
    val cToD = transition(C, D)
    val dToE = transition(D, E)
    val transitioner = transitioner()

    transitioner.transition(Letter(A), aToB)
      .flatMap { transitioner.transition(it, bToC) }
      .flatMap { transitioner.transition(it, cToD) }
      .flatMap { transitioner.transition(it, dToE) } shouldBeRight Letter(E)

    transitioner.preHookExecuted shouldBe 4
    aToB.effected shouldBe 1
    bToC.effected shouldBe 1
    cToD.effected shouldBe 1
    dToE.effected shouldBe 1
    transitioner.postHookExecuted shouldBe 4
  }

  "can transition in a 3+ party loop" {
    val aToB = transition(A, B)
    val bToC = transition(B, C)
    val cToD = transition(C, D)
    val dToB = transition(D, B)
    val dToE = transition(D, E)
    val transitioner = transitioner()

    transitioner.transition(Letter(A), aToB)
      .flatMap { transitioner.transition(it, bToC) }
      .flatMap { transitioner.transition(it, cToD) }
      .flatMap { transitioner.transition(it, dToB) }
      .flatMap { transitioner.transition(it, bToC) }
      .flatMap { transitioner.transition(it, cToD) }
      .flatMap { transitioner.transition(it, dToE) } shouldBeRight Letter(E)

    transitioner.preHookExecuted shouldBe 7
    aToB.effected shouldBe 1
    bToC.effected shouldBe 2
    cToD.effected shouldBe 2
    dToB.effected shouldBe 1
    dToE.effected shouldBe 1
    transitioner.postHookExecuted shouldBe 7
  }

  "can transition in a 2-party loop" {
    val aToB = transition(A, B)
    val bToD = transition(B, D)
    val dToB = transition(D, B)
    val dToE = transition(D, E)
    val transitioner = transitioner()

    transitioner.transition(Letter(A), aToB)
      .flatMap { transitioner.transition(it, bToD) }
      .flatMap { transitioner.transition(it, dToB) }
      .flatMap { transitioner.transition(it, bToD) }
      .flatMap { transitioner.transition(it, dToE) } shouldBeRight Letter(E)

    transitioner.preHookExecuted shouldBe 5
    aToB.effected shouldBe 1
    bToD.effected shouldBe 2
    dToB.effected shouldBe 1
    dToE.effected shouldBe 1
    transitioner.postHookExecuted shouldBe 5
  }

  "can transition to self" {
    val aToB = transition(A, B)
    val bToB = transition(B, B)
    val bToC = transition(B, C)
    val transitioner = transitioner()

    transitioner.transition(Letter(A), aToB)
      .flatMap { transitioner.transition(it, bToB) }
      .flatMap { transitioner.transition(it, bToB) }
      .flatMap { transitioner.transition(it, bToB) }
      .flatMap { transitioner.transition(it, bToC) } shouldBeRight Letter(C)

    transitioner.preHookExecuted shouldBe 5
    aToB.effected shouldBe 1
    bToB.effected shouldBe 3
    bToC.effected shouldBe 1
    transitioner.postHookExecuted shouldBe 5
  }

  "pre hook contains the correct from value and transition" {
    val transition = transition(from = A, to = B)
    val transitioner = transitioner(
      pre = { value, t ->
        value shouldBe Letter(A)
        t shouldBe transition
        t.specificToThisTransitionType shouldBe "NonEmptySet(A) -> B"
        Unit.right()
      }
    )

    transitioner.transition(Letter(A), transition).shouldBeRight()
  }

  "post hook contains the correct from state, post value and transition" {
    val transition = transition(from = B, to = C)
    val transitioner = transitioner(
      post = { from, value, t ->
        from shouldBe B
        value shouldBe Letter(C)
        t shouldBe transition
        t.specificToThisTransitionType shouldBe "NonEmptySet(B) -> C"
        Unit.right()
      }
    )

    transitioner.transition(Letter(B), transition).shouldBeRight()
  }
})

