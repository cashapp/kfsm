package com.squareup.cash.kfsm.exemplar

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import com.squareup.cash.kfsm.Transitionable

data class Hamster(override val state: HamsterState, val locked: Boolean = false) : Transitionable<HamsterState> {
  fun eat(food: String) {
    println("@ (･ｪ･´)◞    (eats $food)")
  }

  fun sleep() {
    println("◟(`･ｪ･)  ╥━╥   (goes to bed)")
  }

  fun lock(): Option<Hamster> = if (!locked) this.copy(locked = true).some() else None

  fun unlock(): Hamster = this.copy(locked = false)
}
