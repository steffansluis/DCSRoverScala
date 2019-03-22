package rover.rdo.state

import rover.rdo.conflict.{ConflictResolutionMechanism, ConflictedState}

/**
  * Represents a general Logged Operation
  * @tparam A The type of the application state data structure
  */
trait RecordedStateModification[A]{
	/* Parent state on which the modification took place */
	def parent: Option[AtomicObjectState[A]]

	def appliedFunction: AtomicObjectState[A] => AtomicObjectState[A]

	/* Assuming simple implementation where it's just a sequence of operations,
	* no checkpoints. The commented code was intended for when checkpointing is
	* applied because the checkpointed states need to be flushed/rest/cleared
	* when rebased
	*/
//	def resultingAtomic: AtomicObjectState[A] = {
//		return appliedFunction.apply(parent.immutableState)
//	}
//	def rebase(newParent: AtomicObjectState[A]): LogRecord[A]
}

case class StateInitializedLogRecord[A](state: A) extends RecordedStateModification[A] {
	// has no parent, it's an initial state
	override def parent: Option[AtomicObjectState[A]] = None

	override def appliedFunction: AtomicObjectState[A] => AtomicObjectState[A] = _ => {
			new BasicAtomicObjectState[A](state, StateLog.empty.appended(this))
		}

//	override def rebase(newParent: AtomicObjectState[A]): LogRecord[A] = {
//		// is not influenced by the parent
//		return this
//	}
}

case class OpAppliedRecord[A](op: AtomicObjectState[A]#Op, stateFrom: AtomicObjectState[A]) extends RecordedStateModification[A] {
	override def parent: Option[AtomicObjectState[A]] = Some(stateFrom)

	override def appliedFunction: AtomicObjectState[A] => AtomicObjectState[A] = (stateFrom: AtomicObjectState[A]) => {
			AtomicObjectState.byApplyingOp(stateFrom, op)
		}

//	override def rebase(newParent: AtomicObjectState[A]): LogRecord[A] = {
//		return this
//	}
}

case class MergeOperation[A](currentParent: AtomicObjectState[A], incomingParent: AtomicObjectState[A], resolver: ConflictResolutionMechanism[A]) extends RecordedStateModification[A] {

	// TODO: can possibly go wrong in ancestor determination, need to investiagate if the ancestor can be in the incoming change-list
	override def parent: Option[AtomicObjectState[A]] = Some(currentParent)

	override def appliedFunction: AtomicObjectState[A] => AtomicObjectState[A] = (stateFrom: AtomicObjectState[A]) => {
		val conflictedState = ConflictedState.from(stateFrom, incomingParent)
		val resolved = resolver.resolveConflict(conflictedState)

		resolved.asAtomicObjectState
	}

//	override def rebase(newParent: AtomicObjectState[A]): LogRecord[A] = {
//		return new MergeOperation[A](newParent, incomingParent, resolver)
//	}
}

/**
  * This class encapsulates all the information stored to log regarding a single RDO state
  */
class StateLog[A] private (private val logList: List[RecordedStateModification[A]] = List()) {

    // Since the lists are immutable, there is no append but rather a new object
	def appended(logRecord: RecordedStateModification[A]): StateLog[A] = {
		val list = this.logList :+ logRecord
		return new StateLog[A](list)
	}

	def appended(logRecords: List[RecordedStateModification[A]]): StateLog[A] = {
		return new StateLog[A](this.logList ++ logRecords)
	}

	def asList: List[RecordedStateModification[A]] = {
		return logList
	}

	def latestState : RecordedStateModification[A] = {
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
		val initial = StateInitializedLogRecord[A](a)
	    return StateLog.empty.appended(initial)
	}
	
	def empty[A]: StateLog[A] = {
		return new StateLog[A]()
	}
}
