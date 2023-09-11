package com.squareup.cash.kfsm.exemplar

import app.cash.quiver.extensions.OutcomeOf
import arrow.core.Either.Companion.catch
import arrow.core.flatMap
import arrow.core.right
import com.squareup.cash.kfsm.DefaultStateTransitioner
import com.squareup.cash.kfsm.StateMachine
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class HamsterFunTest : StringSpec({

  val hamster = Hamster(Awake)
  var globalLock = false
  var ownerNotifications = 0
  val transitioner = DefaultStateTransitioner<Hamster, HamsterState, HamsterFailure, HamsterNotificationType>(
    update = { h, t ->
      if (h.locked) {
        globalLock = false
        h.unlock().copy(state = t.to).right()
      } else {
        throw RuntimeException("Who let the hamsters out?")
      }
    },
    notifyOnSuccess = { h, n, _ ->
      catch {
        n.forEach { type ->
          when (type) {
            Console -> println("updated: $h via $type")
            TheVoid -> println("Uhh..")
            Owner -> ownerNotifications++
          }
        }
      }
    },
    lock = { h ->
      OutcomeOf.catchOption {
        globalLock = true
        h.lock()
      }
    },
    unlockOnError = {
      catch {
        globalLock = false
      }
    }
  )

  beforeTest {
    ownerNotifications = 0
  }

  "a newly woken hamster eats broccoli" {
    val result = transitioner.transition(hamster, EatBreakfast("broccoli")).shouldBeRight()
    result.state shouldBe Eating
  }

  "the hamster has trouble eating cheese" {
    transitioner.transition(hamster, EatBreakfast("cheese")) shouldBeLeft LactoseIntoleranceTroubles("cheese")
  }

  "a locked hamster can never truly be free" {
    val failure = transitioner.transition(hamster.lock().getOrNull()!!, EatBreakfast("broccoli")).shouldBeLeft()
    (failure as InternalHamsterError).t.message shouldBe "State cannot obtain lock for transition Awake➜Eating"
  }

  "a failed effect must unlock the hamster" {
    transitioner.transition(hamster, EatBreakfast("cheese")) shouldBeLeft LactoseIntoleranceTroubles("cheese")
    globalLock shouldBe false
  }

  "a sleeping hamster can awaken yet again" {
    transitioner.transition(hamster, EatBreakfast("broccoli"))
      .flatMap { transitioner.transition(it, RunOnWheel) }
      .flatMap { transitioner.transition(it, GoToBed) }
      .flatMap { transitioner.transition(it, WakeUp) }
      .flatMap { transitioner.transition(it, EatBreakfast("broccoli")) }
      .shouldBeRight().state shouldBe Eating
  }

  "the state machine is hunky dory" {
    StateMachine.verify(Awake, HamsterState::class).shouldBeRight()
  }

  "if the hamster is over it and goes to bed then the owner should be notified" {
    transitioner.transition(hamster, EatBreakfast("broccoli"))
      .flatMap { transitioner.transition(it, FlakeOut) }
      .flatMap { transitioner.transition(it, GoToBed) }
      .shouldBeRight().state shouldBe Asleep
    ownerNotifications shouldBe 1
  }
})
