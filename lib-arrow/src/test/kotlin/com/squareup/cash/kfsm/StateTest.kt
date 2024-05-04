package com.squareup.cash.kfsm

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class StateTest : StringSpec({

  "state reports that it can transition to another state" {
    New.canTransitionTo(Failed) shouldBe true
  }

  "state reports that it cannot transition to another state" {
    New.canTransitionTo(Done) shouldBe false
  }

  "state reports if it can eventually transition to another state" {
    New.canReach(Done) shouldBe true
  }

  "state reports if it can eventually transition to another state via a cycle" {
    Failed.canReach(Done) shouldBe true
  }

  "state reports if it cannot eventually transition to another state" {
    Done.canReach(New) shouldBe false
  }
})

sealed class TestState(to: () -> Set<TestState> = { emptySet() }) : State(to)
object New : TestState({ setOf(WaitingForConfirmation, Failed) })
object WaitingForConfirmation : TestState({ setOf(WaitingForConfirmation, Done, Failed) })
object Failed : TestState({ setOf(Retrying) })
object Retrying : TestState({ setOf(WaitingForConfirmation) })
object Done : TestState()

sealed class TestTransitionFailure : TransitionFailure
object GenericTransitionFailure : TestTransitionFailure() {
  override fun getUnderlying(): Option<Throwable> = None
}

data class CapturingTransitionFailure(
  val value: TestValue,
  val effectCompleted: Boolean,
  val updateCompleted: Boolean,
  val cause: Throwable
) : TestTransitionFailure() {
  override fun getUnderlying(): Option<Throwable> = cause.some()
}

data class TestValue(override val state: TestState, val confirmed: Boolean = false) : Transitionable<TestState>

sealed class TestNotificationType : NotificationType
object Console : TestNotificationType()
