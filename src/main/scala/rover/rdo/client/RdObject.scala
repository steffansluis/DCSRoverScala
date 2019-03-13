package rover.rdo.client

import rover.rdo.{AtomicObjectState, ObjectState, Server}

//FIXME: use hashes instead of Longs/Strings
trait RdObject[S <: ObjectState] {

	/**
	  * The current version of the RDO instance, if current == stable
	  * then the RDO does not contain any unsaved state changes
	  * @return The current version of the RDO state
	  */
	def currentVersion: Long

	/**
	  * The persisted (on master) version this RDO instance was based on
	  * initially.
	  * @return Version of persisted RDO state the instance has started with
	  */
	def stableVersion: Long

	/**
		* The home server of the RDO.
		*/
	def homeServer: Server[S]

	protected def state: S
}

trait ConflicResolutionStrategy[A <: ConflicResolutionStrategy[A]] {

}

abstract class AtomicRDObject[S <: AtomicObjectState[S]](private val state: S, private val conflictResolutionStrategy: ConflicResolutionStrategy[S]) extends RdObject[S] {
//	override def state: S = {
//
//	}

	def commonAncestorState(other: AtomicRDObject[S]): CommonAncestorState[S] = {
		return new CommonAncestorState[S](this.state, other.state)
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
class CommonAncestorState[S <: AtomicObjectState[S]](private val one: AtomicObjectState[S], private val other: AtomicObjectState[S]) extends AtomicObjectState[S] {

	// Add logic to determine common ancestor here (probably lazily)

	// determine it once and defer all RdObject methods to it
//	private val commonAncestor: RdObject[S] = {
//		// determine here... & probably cache or is that not needed in scala? :S
//		// FIXME: proper determination (need to have whole range of intermediate
//		// versions available
//		one
//	}

//	override def currentVersion: Long = {
//		commonAncestor.currentVersion
//	}
//
//	override def stableVersion: Long = {
//		commonAncestor.stableVersion
//	}

//	override def homeServer: Server[S] = {
//		return null
//	}

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