package app.cash.kfsm.exemplar

import app.cash.kfsm.Transitioner
import app.cash.kfsm.exemplar.Hamster.State
import app.cash.quiver.extensions.ErrorOr
import arrow.core.Either
import arrow.core.right

class HamsterTransitioner(
  val saves: MutableList<Hamster> = mutableListOf()
) : Transitioner<HamsterTransition, Hamster, State>(
  // This is where you define how to save your updated value to a data store
  persist = { it.also(saves::add).right() }
) {

  val locks = mutableListOf<Hamster>()
  val unlocks = mutableListOf<Hamster>()
  val notifications = mutableListOf<String>()


  // Any action you might wish to take prior to transitioning, such as pessimistic locking
  override suspend fun preHook(value: Hamster, via: HamsterTransition): ErrorOr<Unit> = Either.catch {
    locks.add(value)
  }

  // Any action you might wish to take after transitioning successfully, such as sending events or notifications
  override suspend fun postHook(from: State, value: Hamster, via: HamsterTransition): ErrorOr<Unit> = Either.catch {
    notifications.add("${value.name} was $from, then began ${via.description} and is now ${via.to}")
    unlocks.add(value)
  }
}
