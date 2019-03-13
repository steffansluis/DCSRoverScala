package rover.rdo

abstract class AtomicObjectState[A](private var value: A) extends ObjectState {
	private var ops: List[Op] = List()

	type Op = A => A

	def immutableState: A = value

	def applyOp(operation: Op): Unit = {
		// Operation must apply itself to the state
		// but we want the state to take in the operations
		// so that the framework can record the op
		val result = operation.apply(this.value)

		// Record the operation in the "log" or "event stream" of operations
		this.ops = this.ops :+ operation

		this.value = result
	}
}
