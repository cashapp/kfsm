package com.squareup.cash.kfsm.exemplar

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.squareup.cash.kfsm.State
import com.squareup.cash.kfsm.Transition

abstract class HamsterTransition : Transition<Hamster, HamsterState, HamsterFailure, HamsterNotificationType> {

  constructor(from: HamsterState, to: HamsterState) : super(from, to)
  constructor(from: Set<HamsterState>, to: HamsterState) : super(from, to)

  override fun makeFailure(
    value: Hamster,
    effectCompleted: Boolean,
    updateCompleted: Boolean,
    cause: Throwable
  ): HamsterFailure = InternalHamsterError(cause)

  override fun notificationsTypes(previousState: State): Set<HamsterNotificationType> = setOf(Console)
}

class EatBreakfast(private val food: String) : HamsterTransition(from = Awake, to = Eating) {
  override suspend fun effect(value: Hamster): Either<HamsterFailure, Hamster> =
    when (food) {
      "broccoli" -> {
        value.eat(food)
        value.right()
      }
      "cheese" -> LactoseIntoleranceTroubles(food).left()
      else -> value.right()
    }
}

object RunOnWheel : HamsterTransition(from = Eating, to = RunningOnWheel) {
  override suspend fun effect(value: Hamster): Either<HamsterFailure, Hamster> {
    println("$value moves to the wheel")
    return value.right()
  }
}

object GoToBed : HamsterTransition(from = setOf(Eating, RunningOnWheel, OverIt), to = Asleep) {
  override suspend fun effect(value: Hamster): Either<HamsterFailure, Hamster> {
    value.sleep()
    return value.right()
  }

  // I mean if she's over it, then leave her alone
  override fun notificationsTypes(previousState: State): Set<HamsterNotificationType> = when (previousState) {
    is OverIt -> setOf(Owner)
    else -> setOf(TheVoid)
  }
}

object FlakeOut : HamsterTransition(from = setOf(Eating, RunningOnWheel), to = OverIt) {
  override suspend fun effect(value: Hamster): Either<HamsterFailure, Hamster> {
    println("$value has had enough and is sitting cute")
    return value.right()
  }
}

object WakeUp : HamsterTransition(from = Asleep, to = Awake) {
  override suspend fun effect(value: Hamster): Either<HamsterFailure, Hamster> {
    println("$value opens her eyes")
    return value.right()
  }
}
