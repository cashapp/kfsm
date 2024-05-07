package app.cash.kfsm

data class States<S : State>(val a: S, val other: Set<S>) {
  constructor(first: S, vararg others: S) : this(first, others.toSet())

  val set: Set<S> = other + a

  companion object {
    fun <S: State> Set<S>.toStates(): States<S> = this.toList().let {
      States(it.first(), it.drop(1).toSet())
    }
  }
}
