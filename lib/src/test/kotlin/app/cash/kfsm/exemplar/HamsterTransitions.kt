package app.cash.kfsm.exemplar

import app.cash.kfsm.Transition
import app.cash.kfsm.exemplar.Hamster.Asleep
import app.cash.kfsm.exemplar.Hamster.Awake
import app.cash.kfsm.exemplar.Hamster.Eating
import app.cash.kfsm.exemplar.Hamster.Resting
import app.cash.kfsm.exemplar.Hamster.RunningOnWheel
import app.cash.quiver.extensions.ErrorOr
import arrow.core.NonEmptySet
import arrow.core.left
import arrow.core.nonEmptySetOf
import arrow.core.right

// Create your own base transition class in order to extend your transition collection with common functionality
abstract class HamsterTransition(
  from: NonEmptySet<Hamster.State>,
  to: Hamster.State
) : Transition<Hamster, Hamster.State>(from, to) {
  constructor(from: Hamster.State, to: Hamster.State) : this(nonEmptySetOf(from), to)

  // Demonstrates how you can add base behaviour to transitions for use in pre and post hooks.
  open val description: String = ""
}

class EatBreakfast(private val food: String) : HamsterTransition(from = Awake, to = Eating) {
  override suspend fun effect(value: Hamster): ErrorOr<Hamster> =
    when (food) {
      "broccoli" -> {
        value.eat(food)
        value.right()
      }

      "cheese" -> LactoseIntoleranceTroubles(food).left()
      else -> value.right()
    }

  override val description = "eating $food for breakfast"
}

object RunOnWheel : HamsterTransition(from = Eating, to = RunningOnWheel) {
  override suspend fun effect(value: Hamster): ErrorOr<Hamster> {
    // This println represents a side-effect
    println("$value moves to the wheel")
    return value.right()
  }

  override val description = "running on the wheel"
}

object GoToBed : HamsterTransition(from = nonEmptySetOf(Eating, RunningOnWheel, Resting), to = Asleep) {
  override suspend fun effect(value: Hamster): ErrorOr<Hamster> {
    value.sleep()
    return value.right()
  }

  override val description = "going to bed"
}

object FlakeOut : HamsterTransition(from = nonEmptySetOf(Eating, RunningOnWheel), to = Resting) {
  override suspend fun effect(value: Hamster): ErrorOr<Hamster> {
    println("$value has had enough and is sitting cute")
    return value.right()
  }

  override val description = "tapping out"
}

object WakeUp : HamsterTransition(from = Asleep, to = Awake) {
  override suspend fun effect(value: Hamster): ErrorOr<Hamster> {
    println("$value opens her eyes")
    return value.right()
  }

  override val description = "waking up"
}

data class LactoseIntoleranceTroubles(val consumed: String) : Exception("Hamster tummy troubles eating $consumed")
