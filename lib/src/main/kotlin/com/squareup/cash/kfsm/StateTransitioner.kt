package com.squareup.cash.kfsm

import arrow.core.Either

interface StateTransitioner<V : Transitionable<S>, S : State, F : TransitionFailure, N : NotificationType> {

  suspend fun transition(value: V, transition: Transition<V, S, F, N>): Either<F, V>
}
