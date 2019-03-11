package rover.rdo
	
trait ObjectState {

}

abstract class AtomicObjectState[A <: AtomicObjectState[A]] extends ObjectState {
	// binding this to a self-type A
	this: A =>
	
	// not sure due to types... do we want the _specific_ subtype here? Not the abstract state
	private var ops: List[Op] = List()
	
	type Op = A => A
	
	def applyOp(operation: Op): A = {
		// Let the operation apply itself on the state
		//			operation.
		val result = operation.apply(this)

		// Record the operation in the "log" or "event stream" of operations
		this.ops = this.ops :+ operation

		return result
	}
}