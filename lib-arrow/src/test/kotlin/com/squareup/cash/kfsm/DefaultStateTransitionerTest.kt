package com.squareup.cash.kfsm

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.left
import arrow.core.right
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DefaultStateTransitionerTest : StringSpec({

  val value = TestValue(WaitingForConfirmation)

  "successful transition executes the effect and returns an updated value" {
    val transitioner = DefaultStateTransitioner<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      update = { v, t -> v.copy(state = t.to).right() }
    )
    val transition = Confirm()

    transitioner.transition(value, transition) shouldBe TestValue(Done, confirmed = true).right()
    transition.effectCalled shouldBe true
  }

  "successful transition issues notification" {
    var notified = false
    DefaultStateTransitioner<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      update = { v, t -> v.copy(state = t.to).right() },
      notifyOnSuccess = { v, _, _ -> catch { notified = (v == v.copy(state = Done)) } }
    ).transition(value, Confirm())

    notified shouldBe true
  }

  "effect failure should be returned without update or notification" {
    val transition = Confirm(effectResult = GenericTransitionFailure.left())
    var notified = false
    var updated = false

    DefaultStateTransitioner<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      update = { v, t ->
        updated = true
        v.copy(state = t.to).right()
      },
      notifyOnSuccess = { _, _, _ -> catch { notified = true } }
    ).transition(value, transition) shouldBe GenericTransitionFailure.left()

    transition.effectCalled shouldBe true
    notified shouldBe false
    updated shouldBe false
  }

  "effect exception should be returned as a failure" {
    val error = RuntimeException("Haha!")
    val unsafeConfirm = object : Transition<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      from = WaitingForConfirmation,
      to = Done
    ) {
      override suspend fun effect(value: TestValue): Either<TestTransitionFailure, TestValue> {
        throw error
      }

      override fun makeFailure(
        value: TestValue,
        effectCompleted: Boolean,
        updateCompleted: Boolean,
        cause: Throwable
      ) = CapturingTransitionFailure(value, effectCompleted, updateCompleted, cause)
    }

    val transitioner = DefaultStateTransitioner<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      update = { v, t -> v.copy(state = t.to).right() }
    )

    transitioner.transition(value, unsafeConfirm) shouldBe CapturingTransitionFailure(
      value = value,
      effectCompleted = false,
      updateCompleted = false,
      cause = error
    ).left()
  }

  "update exception should be returned as a failure" {
    val error = RuntimeException("Broke")
    val transitioner = DefaultStateTransitioner<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      update = { _, _ -> throw error }
    )
    val transition = Confirm()

    transitioner.transition(value, transition) shouldBe CapturingTransitionFailure(
      value = value.copy(confirmed = true),
      effectCompleted = true,
      updateCompleted = false,
      cause = error
    ).left()
    transition.effectCalled shouldBe true
  }

  "update left should be returned as a failure" {
    val error = RuntimeException("Broke")
    val transitioner = DefaultStateTransitioner<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      update = { _, _ -> error.left() }
    )
    val transition = Confirm()

    transitioner.transition(value, transition) shouldBe CapturingTransitionFailure(
      value = value.copy(confirmed = true),
      effectCompleted = true,
      updateCompleted = false,
      cause = error
    ).left()
    transition.effectCalled shouldBe true
  }

  "notify exception should be returned as a failure" {
    val error = RuntimeException("Broke")
    val transitioner = DefaultStateTransitioner<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      update = { v, _ -> v.right() },
      notifyOnSuccess = { _, _, _ -> throw error }
    )
    val transition = Confirm()

    transitioner.transition(value, transition) shouldBe CapturingTransitionFailure(
      value = value.copy(confirmed = true),
      effectCompleted = true,
      updateCompleted = true,
      cause = error
    ).left()
    transition.effectCalled shouldBe true
  }
}) {
  companion object {
    class Confirm(
      private val effectResult: Either<TestTransitionFailure, Unit> = Unit.right()
    ) : Transition<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      from = WaitingForConfirmation,
      to = Done
    ) {

      var effectCalled: Boolean = false

      override suspend fun effect(value: TestValue): Either<TestTransitionFailure, TestValue> {
        effectCalled = true
        return effectResult.map { value.copy(confirmed = true) }
      }

      override fun makeFailure(
        value: TestValue,
        effectCompleted: Boolean,
        updateCompleted: Boolean,
        cause: Throwable
      ) = CapturingTransitionFailure(value, effectCompleted, updateCompleted, cause)
    }
  }
}
