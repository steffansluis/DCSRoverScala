package rover.rdo

class AtomicObjectState[A](private var value: A) extends ObjectState {
	private var ops: List[Op] = List()
	private var states: List[AtomicObjectState[A]] = List(new AtomicObjectState[A](value))

	type Op = A => A

	def immutableState: A = value

	def opsSize: Long = ops.length

	def getStates: List[AtomicObjectState[A]] =  return states

	def applyOp(operation: Op): Unit = {
		// Operation must apply itself to the state
		// but we want the state to take in the operations
		// so that the framework can record the op
		val result = operation.apply(this.value)

		// Record the operation in the "log" or "event stream" of operations
		this.ops = this.ops :+ operation
		this.states = this.states :+ new AtomicObjectState[A](result)

		this.value = result
	}
}
