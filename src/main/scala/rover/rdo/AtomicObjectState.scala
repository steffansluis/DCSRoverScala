package rover.rdo

abstract class AtomicObjectState[A <: AtomicObjectState[A]] extends ObjectState {
	// binding this to a self-type A
	this: A =>

	private var ops: List[Op] = List()

	type Op = A => A

	def applyOp(operation: Op): A = {
		// Operation must apply itself to the state
		// but we want the state to take in the operations
		// so that the framework can record the op
		val result = operation.apply(this)

		// Record the operation in the "log" or "event stream" of operations
		this.ops = this.ops :+ operation

		return result
	}
}
