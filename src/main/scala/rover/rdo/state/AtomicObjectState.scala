package rover.rdo.state

import rover.rdo.ObjectState

trait AtomicObjectState[A] extends ObjectState {
	type Op = A => A

	def immutableState: A

	protected[rdo] def log: StateLog[A]
	def applyOp(operation: Op): AtomicObjectState[A]

	override def equals(obj: Any): Boolean
}

class InitialAtomicObjectState[A](identity: A) extends AtomicObjectState[A] {
	override def immutableState: A = identity
	
	override protected def log: StateLog[A] = StateLog.empty
	
	override def applyOp(operation: Op): AtomicObjectState[A] = {
		val resultingState = operation.apply(immutableState)
		return new BasicAtomicObjectState[A](resultingState, log)
	}
}

// TODO: make ctor private
class BasicAtomicObjectState[A](val immutableState: A, protected[rdo] val log: StateLog[A]) extends AtomicObjectState[A] {

	def applyOp(operation: Op): AtomicObjectState[A] = {
		// Operation must apply itself to the state
		// but we want the state to take in the operations
		// so that the framework can record the op
		val result = operation.apply(this.immutableState)

		// Record the operation in the Log
		val updatedLog = log.appended(ApplicationOperationAppliedLogRecord(this, operation))

		return new BasicAtomicObjectState[A](result, updatedLog)
	}

	override def equals(that: Any): Boolean = {
		that match{
			case that: AtomicObjectState[A] => this.immutableState == that.immutableState
			case _ => false
		}
	}

	override def toString: String = {
		immutableState.toString
	}
}

object AtomicObjectState {
	def initial[A](value: A): AtomicObjectState[A] = {
		return new BasicAtomicObjectState[A](value, StateLog.withInitialState(value))
	}

//	def fromLog[A](log: StateLog[A]): AtomicObjectState[A] = {
//		return new BasicAtomicObjectState[A](log.asList.last.stateResult, log)
//	}
}