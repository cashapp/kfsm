package app.cash.kfsm

/** A simple state machine that represents a letter of the alphabet. */
data class Letter(override val state: LetterState) : Value<LetterState> {
  override fun update(newState: LetterState): Letter = copy(state = newState)
}

sealed class LetterState(to: () -> Set<LetterState>) : State(to)

data object A : LetterState(to = { setOf(B) })
data object B : LetterState(to = { setOf(B, C, D) })
data object C : LetterState(to = { setOf(D) })
data object D : LetterState(to = { setOf(B, E) })
data object E : LetterState(to = { emptySet() })
