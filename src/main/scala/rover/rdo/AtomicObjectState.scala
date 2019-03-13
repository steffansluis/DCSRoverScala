package rover.rdo

abstract class AtomicObjectState[A](var value: A) extends ObjectState {
	// binding this to a self-type A
//	this: A =>

//	private var ops: List[Op] = List()

//	type Op = AtomicObjectState[A] => AtomicObjectState[A]

	type Op = A => A
//
//	def applyOp(operation: Op): Unit = {
//		// Operation must apply itself to the state
//		// but we want the state to take in the operations
//		// so that the framework can record the op
//		val result = operation.apply(this)
//
//		// Record the operation in the "log" or "event stream" of operations
//		this.ops = this.ops :+ operation
//
//		this.value = result
//	}
}
