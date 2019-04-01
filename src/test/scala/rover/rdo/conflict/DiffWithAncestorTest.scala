package rover.rdo.conflict

import org.scalatest.FunSuite
import rover.rdo.state.AtomicObjectState

class DiffWithAncestorTest extends FunSuite {
	test("diff works when common ancestor is initial state and only one change") {
		val initial = AtomicObjectState.initial[List[String]](List())
		val modified = initial.applyOp(s => s :+ "Henk")
		
		val diff = new DiffWithAncestor(modified, new CommonAncestor(initial, modified))
		
		assert(diff.asList.size == 1)
		assert(diff.asList.head == modified.log.asList.reverse.head)
	}
}
