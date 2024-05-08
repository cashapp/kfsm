package app.cash.kfsm

/** A collection of states that is guaranteed to be non-empty. */
data class States<S : State>(val a: S, val other: Set<S>) {
  constructor(first: S, vararg others: S) : this(first, others.toSet())

  val set: Set<S> = other + a

  companion object {
    fun <S: State> Set<S>.toStates(): States<S> = when {
      isEmpty() -> throw IllegalArgumentException("Cannot create States from empty set")
      else -> toList().let { States(it.first(), it.drop(1).toSet()) }
    }
  }
}
