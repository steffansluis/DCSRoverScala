package rover.rdo.state

import rover.rdo.conflict.{ConflictResolutionMechanism, ConflictedState}

/**
  * Represents a general Logged Operation
  * @tparam A The type of the application state data structure
  */
trait LogRecord[A]{
	/** The before-state */
	def parent: AtomicObjectState[A]
	
	def appliedFunction: A => AtomicObjectState[A]
}

case class StateInitializedLogRecord[A](state: AtomicObjectState[A]) extends LogRecord[A] {
	override def parent: AtomicObjectState[A] = state
	
	override def appliedFunction: A => AtomicObjectState[A] = _ => state
}

case class ApplicationOperationAppliedLogRecord[A](parent: AtomicObjectState[A], op: AtomicObjectState[A]#Op) extends LogRecord[A] {
	override def appliedFunction: A => AtomicObjectState[A] = (stateFrom: A) => {
			val resultingState = op.apply(stateFrom)
			val logUpToResulting = parent.log.appended(this)
			
			val resultingAtomicState = new BasicAtomicObjectState[A](resultingState, logUpToResulting)
			
			resultingAtomicState
		}
}

case class MergeOperation[A](currentParent: AtomicObjectState[A], incomingParent: AtomicObjectState[A], resolver: ConflictResolutionMechanism[A]) extends LogRecord[A] {
	override def parent: AtomicObjectState[A] = currentParent
	
	override def appliedFunction: A => AtomicObjectState[A] = (stateFrom: A) => {
		val conflictedState = ConflictedState.from(currentParent, incomingParent)
		val resultingAtomicState = resolver.resolveConflict(conflictedState)
		
		resultingAtomicState
	}
}

/**
  * This class encapsulates all the information stored to log regarding a single RDO state
  */
class StateLog[A] private (private val logList: List[LogRecord[A]] = List()) {

    // Since the lists are immutable, there is no append but rather a new object
	def appended(logRecord: LogRecord[A]): StateLog[A] = {
		val list = this.logList :+ logRecord
		return new StateLog[A](list)
	}

	def asList: List[LogRecord[A]] = {
		return logList
	}

	def latestState : LogRecord[A] = {
		return logList.last
	}
}

object StateLog {
	/**
	  * Constructs a new log with an initial state. Use for objects with fresh state
	  * @tparam A
	  * @return
	  */
	def withInitialState[A](a: A): StateLog[A] = {
		val initial = StateInitializedLogRecord[A](AtomicObjectState.initial(a))
	    return new StateLog[A]().appended(initial)
	}
	
	def empty[A]: StateLog[A] = {
		return new StateLog[A]()
	}
}
