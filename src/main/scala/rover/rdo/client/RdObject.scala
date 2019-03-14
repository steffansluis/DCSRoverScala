package rover.rdo.client

import rover.rdo.AtomicObjectState

//FIXME: use hashes instead of Longs/Strings?
class RdObject[A] () {

	var state: AtomicObjectState[A] = null

	def this(state: AtomicObjectState[A]) = {
		this()
		this.state = state
	}

	/**
	  * The current version of the RDO instance, if current == stable
	  * then the RDO does not contain any unsaved state changes
	  * @return The current version of the RDO state
	  */
	def currentVersion: Long = {
		return state.opsSize
	}

	//FIXME: this is a crude method of ectractingthe version; we need a better method
	//two states are equivalent here if merely the same amount of operations are performed
	def currentVersion(cstate: AtomicObjectState[A]): Long  = {
		return cstate.opsSize
	}

	/**
	  * The persisted (on master) version this RDO instance was based on
	  * initially.
	  * @return Version of persisted RDO state the instance has started with
	  */
//	def stableVersion: Long = {
//		// TODO: determine from state
//		null
//	}


	def getValues: List[A] = state.getStates


	protected final def modifyState(op: AtomicObjectState[A]#Op): Unit = {
		state.applyOp(op)
	}

	protected final def immutableState: A = {
		return state.immutableState
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
class CommonAncestor[A](private val one: RdObject[A], private val other: RdObject[A]) extends RdObject[A] {

	// determine it once and defer all RdObject methods to it
	def commonAncestor: RdObject[A] = {
		// determine here... & probably cache or is that not needed in scala? :S
		// FIXME: proper determination (need to have whole range of intermediate
		// versions available
		for (i <- one.getValues.reverse) {
			for (j <- other.getValues.reverse) {
				if (currentVersion(new AtomicObjectState[A](i)) == currentVersion(new AtomicObjectState[A](j))) {
					val ancestor = new RdObject[A](new AtomicObjectState[A](i))
					return ancestor
				}
			}
		}
		return new RdObject[A]()
	}

	override def toString: String = {
		commonAncestor.getValues.toString()
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