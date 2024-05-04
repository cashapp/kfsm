package com.squareup.cash.kfsm

import arrow.core.Either
import arrow.core.Option

interface TransitionFailure {
  fun getUnderlying(): Option<Throwable>
}

interface Transitionable<S : State> {
  val state: S
}

interface NotificationType

abstract class Transition<V : Transitionable<S>, S : State, F : TransitionFailure, N : NotificationType> {
  val from: Set<S>
  val to: S

  constructor(
    from: Set<S>,
    to: S
  ) {
    from.forEach { require(it.canTransitionTo(to)) { "$it->$to is an invalid transition" } }
    this.from = from
    this.to = to
  }

  constructor(
    from: S,
    to: S
  ) : this(setOf(from), to)

  /**
   * The effect to be performed when the withdrawal is in the correct state.
   */
  abstract suspend fun effect(value: V): Either<F, V>

  /**
   * How to construct a failure in the presence of a thrown exception.
   *
   * @param value The Transitionable value attempting to be transitioned
   * @param effectCompleted Whether this transition's effect had successfully completed
   * @param updateCompleted Whether the update function had successfully completed
   * @param cause The cause triggering this failure
   */
  abstract fun makeFailure(value: V, effectCompleted: Boolean, updateCompleted: Boolean, cause: Throwable): F

  open fun notificationsTypes(previousState: State): Set<N> = emptySet()
}
