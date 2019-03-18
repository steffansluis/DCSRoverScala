package rover.rdo

import rover.rdo.client.{Log, LogRecord}

// TODO: make ctor private
class AtomicObjectState[A](private val value: A, private[rdo] val log: Log[A]) extends ObjectState {

	type Op = A => A

	// Initialization of the record with the current state and empyt list of ops

	// FIXME: if we start with some initial first state, the log is empty, need to add "initial value op" or something
//	private val log: Log[A] = new Log[A]()

	def immutableState: A = value

	// The -1 corresponds to the initial state where no operation is yet applied
//	def numOperations: Long = this.record.recordSize() - 1

//	def getImmutableStates: List[A] =  this.record.getImmutableStates

	def applyOp(operation: Op): AtomicObjectState[A] = {
		// Operation must apply itself to the state
		// but we want the state to take in the operations
		// so that the framework can record the op
		val result = operation.apply(this.value)

		// Record the operation in the Log
		val updatedLog = log.appended(LogRecord(value, operation, result))

		return new AtomicObjectState[A](result, updatedLog)
	}

	override def equals(that: Any): Boolean = {
		that match{
			case that: AtomicObjectState[A] => this.immutableState == that.immutableState
			case _ => false
		}
	}

	override def toString: String = {
		value.toString
	}
}

object AtomicObjectState {
	def initial[A](value: A): AtomicObjectState[A] = {
		return new AtomicObjectState[A](value, Log.withInitialState(value))
	}

	def fromLog[A](log: Log[A]): AtomicObjectState[A] = {
		return new AtomicObjectState[A](log.asList.last.stateResult, log)
	}
}