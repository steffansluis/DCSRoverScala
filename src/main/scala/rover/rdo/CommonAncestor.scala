package rover.rdo

import rover.rdo.client.RdObject
import rover.rdo.state.{AtomicObjectState, StateLog}

/**
  * Encapsulates the concept of a "common ancestor" RDO. That is, given two
  * RDO instances, the most recent state that both of them share.
  * This class is also responsible for determining the common ancestor between
  * the two instances.
  * @param one Some RDO
  * @param other Some other RDO
  */
class CommonAncestor[A](private val one: AtomicObjectState[A], private val other: AtomicObjectState[A]) extends AtomicObjectState[A] { // todo: fixme with a deferred state

	// determine it once and defer all RdObject methods to it
	def state: AtomicObjectState[A] = {
		// todo: investigate if this function is exectured every time, or just once

		for (i <- one.log.asList.reverse) {
			for (j <- other.log.asList.reverse) {

//				println()
//				println("I:" + i)
//				println("J:" + j)

				//noinspection ExistsEquals
				if (i.parent.exists(parentI => j.parent.exists(parentJ => parentI == parentJ))) {
					val ancestor = i.parent.get
					return ancestor
				}
			}
		}
		// FIXME: typed exception, better error logs
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
	def from[A](serverVersion: RdObject[A], incomingVersion: RdObject[A]): CommonAncestor[A] = {
		return new CommonAncestor[A](serverVersion.state, incomingVersion.state)
	}

	def from[A](serverVersion: AtomicObjectState[A], incomingVersion: AtomicObjectState[A]): CommonAncestor[A] = {
		return new CommonAncestor[A](serverVersion, incomingVersion)
	}
}