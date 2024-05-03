package com.squareup.cash.kfsm

import arrow.core.Either
import arrow.core.right
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class TransitionTest : StringSpec({

  "can construct when the to state is a from state" {
    object : Transition<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      from = setOf(New, WaitingForConfirmation),
      to = WaitingForConfirmation
    ) {
      override suspend fun effect(value: TestValue): Either<TestTransitionFailure, TestValue> = value.right()
      override fun makeFailure(
        value: TestValue,
        effectCompleted: Boolean,
        updateCompleted: Boolean,
        cause: Throwable
      ): TestTransitionFailure = GenericTransitionFailure
    }
  }

  "cannot construct when the to state is unreachable from any from state" {
    shouldThrow<IllegalArgumentException> {
      object : Transition<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
        from = Done,
        to = Failed
      ) {
        override suspend fun effect(value: TestValue): Either<TestTransitionFailure, TestValue> = value.right()
        override fun makeFailure(
          value: TestValue,
          effectCompleted: Boolean,
          updateCompleted: Boolean,
          cause: Throwable
        ): TestTransitionFailure = GenericTransitionFailure
      }
    }
  }

  "can construct from a single from state" {
    object : Transition<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      from = New,
      to = WaitingForConfirmation
    ) {
      override suspend fun effect(value: TestValue): Either<TestTransitionFailure, TestValue> = value.right()
      override fun makeFailure(
        value: TestValue,
        effectCompleted: Boolean,
        updateCompleted: Boolean,
        cause: Throwable
      ): TestTransitionFailure = GenericTransitionFailure
    }
  }

  "can construct from multiple from states" {
    object : Transition<TestValue, TestState, TestTransitionFailure, TestNotificationType>(
      from = setOf(New, WaitingForConfirmation),
      to = Failed
    ) {
      override suspend fun effect(value: TestValue): Either<TestTransitionFailure, TestValue> = value.right()
      override fun makeFailure(
        value: TestValue,
        effectCompleted: Boolean,
        updateCompleted: Boolean,
        cause: Throwable
      ): TestTransitionFailure = GenericTransitionFailure
    }
  }
})
