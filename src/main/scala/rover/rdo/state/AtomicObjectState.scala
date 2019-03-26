package rover.rdo.state

import rover.rdo.ObjectId

trait AtomicObjectState[A] {
	type Op = A => A

	def immutableState: A

	def log: StateLog[A]
	def applyOp(operation: Op): AtomicObjectState[A]

	override def equals(obj: Any): Boolean
}

// TODO: make ctor private?
class InitialAtomicObjectState[A] (identityState: A) extends AtomicObjectState[A] {

	override def immutableState: A = identityState
	
	override def log: StateLog[A] = StateLog.withInitialState(this.identityState)
	
	override def applyOp(operation: Op): AtomicObjectState[A] = {
		val resultingState = operation.apply(immutableState)
		return new BasicAtomicObjectState[A](resultingState, log)
	}
}

class BasicAtomicObjectState[A] (val immutableState: A, val log: StateLog[A]) extends AtomicObjectState[A] {

	def applyOp(operation: Op): AtomicObjectState[A] = {
		// Operation must apply itself to the state
		// but we want the state to take in the operations
		// so that the framework can record the op
		val result = operation.apply(this.immutableState)

		// Record the operation in the Log
		val updatedLog = log.appended(OpAppliedRecord(operation, this))

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
		return new InitialAtomicObjectState[A](value)
	}

//	def fromLog[A](log: StateLog[A]): AtomicObjectState[A] = {
//		return log.latestState.resultingAtomic
//	}

	def byApplyingOp[A](stateFrom: AtomicObjectState[A], op: AtomicObjectState[A]#Op): AtomicObjectState[A] = {
		val resultingState = op.apply(stateFrom.immutableState)
		val appendedLog = stateFrom.log.appended(new OpAppliedRecord[A](op, stateFrom))

		val resultingAtomicState = new BasicAtomicObjectState[A](resultingState, appendedLog)

		return resultingAtomicState
	}
}