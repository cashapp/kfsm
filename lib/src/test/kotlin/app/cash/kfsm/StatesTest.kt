package app.cash.kfsm

import app.cash.kfsm.States.Companion.toStates
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.set
import io.kotest.property.checkAll

class StatesTest : StringSpec({

  "vararg constructor" {
    checkAll(Arb.set(arbChar, range = 1 .. 5)) { set ->
      States(set.first(), set).set shouldBe set
      set.toStates().set shouldBe set
    }
  }

  "fails to create from empty set" {
    shouldThrow<IllegalArgumentException> {
      emptySet<Char>().toStates()
    }
  }
}) {
  companion object {
    val arbChar: Arb<Char> = Arb.element(A, B, C, D, E)
  }
}
