package rover.rdo.client

import rover.rdo.AtomicObjectState

import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async}
import scala.concurrent.{Promise, Future}

//FIXME: use hashes instead of Longs/Strings?
class RdObject[A](var state: AtomicObjectState[A]) {

	// TODO: "is up to date" or "version" methods

	protected final def modifyState(op: AtomicObjectState[A]#Op): Unit = {
		state = state.applyOp(op)
//		onStateModified(state)
	}

	protected def onStateModified(oldState: AtomicObjectState[A]): Future[Unit] = {
		async {

		}
	}

	protected final def immutableState: A = {
		return state.immutableState
	}

	override def toString: String = {
		state.toString
	}
}

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
	def commonAncestor: AtomicObjectState[A] = {
		// determine here... & probably cache or is that not needed in scala? :S
		// FIXME: currently we return just the atomic state..what about the outstanding operations?
		for (i <- one.log.asList.reverse) {
			for (j <- other.log.asList.reverse) {

				println()
				println("I:" + i)
				println("J:" + j)

				if (i.stateResult == j.stateResult){
					val indexOfI = one.log.asList.indexOf(i)
					val logRecordsUpToI = one.log.asList.slice(0, indexOfI+1)
					val logUpToI = new StateLog[A](logRecordsUpToI)
					val ancestor = AtomicObjectState.fromLog(logUpToI)
					return ancestor
				}
			}
		}
		// FIXME: typed exception, better error logs
		throw new RuntimeException("Failed to determine a common ancestor")
	}

	override def toString: String = {
		commonAncestor.toString
	}

	override def immutableState: A = {
		commonAncestor.immutableState
	}

	override protected[rdo] def log: StateLog[A] = {
		commonAncestor.log
	}

	override def applyOp(operation: Op): AtomicObjectState[A] = {
		return commonAncestor.applyOp(operation)
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

class DiffWithAncestor[A](private val child: AtomicObjectState[A], private val ancestor: AtomicObjectState[A]) {

	def diffWithAncestor: List[LogRecord[A]] = {
		for(i <- child.log.asList) {
			if(i.stateResult == ancestor.log.latestState.stateResult) {
				val indexOfI = child.log.asList.indexOf(i)
				val logRecordsUpToI = child.log.asList.slice(indexOfI + 1, child.log.asList.size - 1)
				return logRecordsUpToI
			}
		}
		throw new RuntimeException("Failed to determine difference with this ancestor")
	}

	override def toString: String = {
		"DiffWithAncestor{ \n	" + diffWithAncestor.mkString("\n	") + "\n}"
	}

}