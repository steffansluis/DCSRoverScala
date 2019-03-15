package rover.rdo

import rover.rdo.client.LogRecord

class AtomicObjectState[A](private var value: A) extends ObjectState {
//	private var ops: List[Op] = List()
//	private var states: List[A] = List(value)

	type Op = A => A

	private val record: LogRecord[A] = new LogRecord[A](List(value))



	def immutableState: A = value

	// The -1 corresponds to the initial state where no operation is yet applied
	def numOperations: Long = this.record.recordSize() - 1

	def getImmutableStates: List[A] =  this.record.getImmutableStates

	def applyOp(operation: Op): Unit = {
		// Operation must apply itself to the state
		// but we want the state to take in the operations
		// so that the framework can record the op
		val result = operation.apply(this.value)

		// Record the operation in the Log
		this.record.updateRecord(new AtomicObjectState[A](result), result, operation)
//		this.ops = this.ops :+ operation
//		this.states = this.states :+ result

		this.value = result
	}
}
