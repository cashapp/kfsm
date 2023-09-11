package com.squareup.cash.kfsm.exemplar

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import com.squareup.cash.kfsm.TransitionFailure

sealed class HamsterFailure : TransitionFailure {
  override fun getUnderlying(): Option<Throwable> = None
}

data class InternalHamsterError(val t: Throwable) : HamsterFailure() {
  override fun getUnderlying(): Option<Throwable> = t.some()
}
object FourOhFourHamsterNotFound : HamsterFailure()
data class LactoseIntoleranceTroubles(val consumed: String) : HamsterFailure()
