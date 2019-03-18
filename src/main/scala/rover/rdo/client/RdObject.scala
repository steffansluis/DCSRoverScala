package rover.rdo.client

import rover.rdo.AtomicObjectState

import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async}
import scala.concurrent.{Promise}

//FIXME: use hashes instead of Longs/Strings?
abstract class RdObject[A](var state: AtomicObjectState[A]) {

	// TODO: "is up to date" or "version" methods

	protected final def modifyState(op: AtomicObjectState[A]#Op): Unit = {
		state = state.applyOp(op)
//		onStateModified(state)
	}

	protected def onStateModified(oldState: AtomicObjectState[A]): Promise[Unit]

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
class CommonAncestor[A](private val one: RdObject[A], private val other: RdObject[A]) extends RdObject[A](null) { // todo: fixme with a deferred state
	override def onStateModified(oldState: AtomicObjectState[A]): Promise[Unit] = {
		Promise() completeWith async { }
	}

	// determine it once and defer all RdObject methods to it
	def commonAncestor: RdObject[A] = {
		// determine here... & probably cache or is that not needed in scala? :S
		// FIXME: currently we return just the atomic state..what about the outstanding operations?
		for (i <- one.state.log.asList.reverse) {
			for (j <- other.state.log.asList.reverse) {

				println()
				println("I:" + i)
				println("J:" + j)

				if (i.stateResult == j.stateResult){
					val indexOfI = one.state.log.asList.indexOf(i)
					val logRecordsUpToI = one.state.log.asList.slice(0, indexOfI+1)
					val logUpToI = new Log[A](logRecordsUpToI)
//					val ancestor = new RdObject[A](AtomicObjectState.fromLog(logUpToI))
//					return ancestor
				}
			}
		}
		// FIXME: typed exception, better error logs
		throw new RuntimeException("Failed to determine a common ancestor")
	}

	override def toString: String = {
		commonAncestor.state.toString
	}
}