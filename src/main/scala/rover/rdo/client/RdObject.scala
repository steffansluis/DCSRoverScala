package rover.rdo.client

import rover.rdo.AtomicObjectState

//FIXME: use hashes instead of Longs/Strings?
class RdObject[A](var state: AtomicObjectState[A]) {

//	var state: AtomicObjectState[A] = null

//	def this(state: AtomicObjectState[A]) = {
//		this()
//		this.state = state
//	}

	/**
	  * The current version of the RDO instance, if current == stable
	  * then the RDO does not contain any unsaved state changes
	  * @return The current version of the RDO state
	  */
//	def currentVersion: Long = {
//		return state.numOperations
//	}

	//FIXME: this is a crude method of ectractingthe version; we need a better method
	//two states are equivalent here if merely the same amount of operations are performed
//	def currentVersion(cstate: AtomicObjectState[A]): Long  = {
//		return cstate.numOperations
//	}

	/**
	  * The persisted (on master) version this RDO instance was based on
	  * initially.
	  * @return Version of persisted RDO state the instance has started with
	  */
//	def stableVersion: Long = {
//		// TODO: determine from state
//		null
//	}


//	def getImmutableStates: List[A] = state.getImmutableStates


	protected final def modifyState(op: AtomicObjectState[A]#Op): Unit = {
		state = state.applyOp(op)
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
class CommonAncestor[A](private val one: RdObject[A], private val other: RdObject[A]) extends RdObject[A](null) { // todo: fixme with a deferred state

	// determine it once and defer all RdObject methods to it
	def commonAncestor: RdObject[A] = {
		// determine here... & probably cache or is that not needed in scala? :S
		// FIXME: currently we return just the atomic state..what about the outstanding operations?
		// can there be a fork in the history of the provided RDOs?
		// since client RDOs apply tentative updates there shouldn't be any, apart from the common ancestor
		// FIXME: might need to keep track of history from the last stable point
		for (i <- one.state.log.asList.reverse) {
			for (j <- other.state.log.asList.reverse) {
//				if (currentVersion(new AtomicObjectState[A](i)) == currentVersion(new AtomicObjectState[A](j))) {
				println()
				println("I:" + i)
				println("J:" + j)

				if (i.stateResult == j.stateResult){
					val indexOfI = one.state.log.asList.indexOf(i)
					val logRecordsUpToI = one.state.log.asList.slice(0, indexOfI+1)
					val logUpToI = new Log[A](logRecordsUpToI)
					val ancestor = new RdObject[A](AtomicObjectState.fromLog(logUpToI))
					return ancestor
				}
			}
		}
		throw new RuntimeException("HENK IS DEAD")
//		return new RdObject[A]()
	}

	override def toString: String = {
		commonAncestor.state.toString
	}


//	override def currentVersion: Long = {
//		commonAncestor.currentVersion
//	}
//
//	override def stableVersion: Long = {
//		commonAncestor.stableVersion
//	}
//
//	// FIXME: determine what to do with this, fix return value/type
//    def hasDiverged: Long =  {
//		// FIXME: non-logical result
//	    if (this.currentVersion != other.currentVersion){
//			commonAncestor.currentVersion
//		}
//		else{
//			println("Non-divergent objects")
//			this.currentVersion
//		}
//	}
}

class updateRDO(){
	//TODO: apply tentative updates to RDO

}

class revertRDO(){
	//TOD: also revert changes
}