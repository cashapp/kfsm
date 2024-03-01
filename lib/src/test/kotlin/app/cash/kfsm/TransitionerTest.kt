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
    pre: (Value<LetterState>) -> ErrorOr<Unit> = { Unit.right() },
    post: (Value<LetterState>) -> ErrorOr<Unit> = { Unit.right() },
    persist: (Value<LetterState>) -> ErrorOr<Value<LetterState>> = { it.right() },
  ) = object : Transitioner<LetterState>(persist) {
    var preHookExecuted = 0
    var postHookExecuted = 0

    override fun preHook(value: Value<LetterState>): ErrorOr<Unit> = pre(value).also { preHookExecuted += 1 }
    override fun postHook(value: Value<LetterState>): ErrorOr<Unit> = post(value).also { postHookExecuted += 1 }
  }

  fun transition(from: LetterState = A, to: LetterState = B) = object : Transition<LetterState>(from, to) {
    var effected = 0
    override fun effect(value: Value<LetterState>): ErrorOr<Value<LetterState>> {
      effected += 1
      return value.update(to).right()
    }
  }

  "effects valid transition" {
    val transition = transition()
    val transitioner = transitioner()

    transitioner.transition(Letter(A), transition) shouldBeRight Letter(B)

    transitioner.preHookExecuted shouldBe 1
    transition.effected shouldBe 1
    transitioner.postHookExecuted shouldBe 1
  }

  "ignores completed transition" {
    val transition = transition()
    val transitioner = transitioner()

    transitioner.transition(Letter(B), transition) shouldBeRight Letter(B)

    transitioner.preHookExecuted shouldBe 0
    transition.effected shouldBe 0
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error on invalid transition" {
    val transition = transition()
    val transitioner = transitioner()

    transitioner.transition(Letter(C), transition).shouldBeLeft()
      .shouldHaveMessage("Value cannot transition [A] to B, because it is currently C")

    transitioner.preHookExecuted shouldBe 0
    transition.effected shouldBe 0
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when preHook errors" {
    val error = RuntimeException("preHook error")

    val transition = transition()
    val transitioner = transitioner(pre = { error.left() })

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transitioner.preHookExecuted shouldBe 1
    transition.effected shouldBe 0
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when effect errors" {
    val error = RuntimeException("effect error")

    val transition = object : Transition<LetterState>(A, B) {
      override fun effect(value: Value<LetterState>): ErrorOr<Value<LetterState>> = error.left()
    }
    val transitioner = transitioner()

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transitioner.preHookExecuted shouldBe 1
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when postHook errors" {
    val error = RuntimeException("postHook error")

    val transition = transition()
    val transitioner = transitioner(post = { error.left() })

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transition.effected shouldBe 1
    transitioner.preHookExecuted shouldBe 1
    transitioner.postHookExecuted shouldBe 1
  }

  "returns error when preHook throws" {
    val error = RuntimeException("preHook error")

    val transition = transition()
    val transitioner = transitioner(pre = { throw error })

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transition.effected shouldBe 0
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when effect throws" {
    val error = RuntimeException("effect error")

    val transition = object : Transition<LetterState>(A, B) {
      override fun effect(value: Value<LetterState>): ErrorOr<Value<LetterState>> = throw error
    }
    val transitioner = transitioner()

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transitioner.preHookExecuted shouldBe 1
    transitioner.postHookExecuted shouldBe 0
  }

  "returns error when postHook throws" {
    val error = RuntimeException("postHook error")

    val transition = transition()
    val transitioner = transitioner(post = { throw error })

    transitioner.transition(Letter(A), transition) shouldBeLeft error

    transition.effected shouldBe 1
    transitioner.preHookExecuted shouldBe 1
  }

  "returns error when persist fails" {
    val error = RuntimeException("persist error")

    val transition = transition()
    val transitioner = transitioner(persist = { error.left() })

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

  "can transition in a loop" {
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

})

