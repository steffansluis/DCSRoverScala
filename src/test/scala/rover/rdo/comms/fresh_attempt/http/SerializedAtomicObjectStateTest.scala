package rover.rdo.comms.fresh_attempt.http

import org.scalatest.FunSuite
import rover.rdo.state.AtomicObjectState

class SerializedAtomicObjectStateTest extends FunSuite {

	test("Stuff") {
		type Henk = List[String]
		var state: AtomicObjectState[Henk] = AtomicObjectState.initial(List())

		val serialized = new SerializedAtomicObjectState[Henk](state)
		val deserialized = new DeserializedAtomicObjectState[Henk](serialized.asBytes)

		val resState = deserialized.asAtomicObjectState

		assert(state.equals(resState))

		val modifiedOrig = state.applyOp(a => a :+ "henk")
		val isSame = modifiedOrig.equals(resState)
		assert(!isSame)
	}
}
