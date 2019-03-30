package rover.rdo.conflict

import rover.rdo.{ObjectId, RdObject}
import rover.rdo.state.{AtomicObjectState, StateLog}

/**
  * Encapsulates the concept of a "common ancestor" RDO. That is, given two
  * RDO instances, the most recent state that both of them share.
  * This class is also responsible for determining the common ancestor between
  * the two instances.
  * @param one Some RDO
  * @param other Some other RDO
  */
class CommonAncestor[A <: Serializable](private val one: AtomicObjectState[A], private val other: AtomicObjectState[A]) extends AtomicObjectState[A] { // todo: fixme with a deferred state
	if (one.objectId != other.objectId) {
		throw new RuntimeException("Given AtomicObjectStates do not share same objectId. Not allowed to compare the two.")
	}

	override def objectId: ObjectId = one.objectId // one or other, doesn't matter

	// determine it once and defer all RdObject methods to it
	def state: AtomicObjectState[A] = {
		// todo: investigate if this function is exectured every time, or just once

		for (i <- one.log.asList.reverse) {
			for (j <- other.log.asList.reverse) {

				// FIXME: There can be more than one parent, if ancestor is in an "incoming change" of a merge, it will not find it
				//noinspection ExistsEquals
				if (i.parent.exists(parentI => j.parent.exists(parentJ => parentI == parentJ))) {
					val ancestor = i.parent.get
					return ancestor
				}
			}
		}
		// TODO: typed exception, better error logs
		throw new RuntimeException("Failed to determine a common ancestor")
	}

	override def toString: String = {
		state.toString
	}

	override def immutableState: A = {
		state.immutableState
	}

	override def log: StateLog[A] = {
		state.log
	}

	override def applyOp(operation: Op): AtomicObjectState[A] = {
		return state.applyOp(operation)
	}
}

object CommonAncestor {
	def from[A <: Serializable](serverVersion: RdObject[A], incomingVersion: RdObject[A]): CommonAncestor[A] = {
		return new CommonAncestor[A](serverVersion.state, incomingVersion.state)
	}

	def from[A <: Serializable](serverVersion: AtomicObjectState[A], incomingVersion: AtomicObjectState[A]): CommonAncestor[A] = {
		return new CommonAncestor[A](serverVersion, incomingVersion)
	}
}