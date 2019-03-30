package rover.rdo.comms.fresh_attempt

import rover.rdo.ObjectId
import rover.rdo.state.AtomicObjectState

abstract class Server[A <: Serializable] {
	/**
	  * <p>
	  *     Request the most recent object's state
	  *     from the server.
	  * </p>
	  * @param objectId The id of the RdObject/State
	  */
	def get(objectId: ObjectId): AtomicObjectState[A]

	/**
	  * <p>
	  *     Present the state to the server for the changes to be included
	  *     in the master version.
	  * </p><br />
	  * <p>
	  *     It is up to the server implementation
	  *     how, if at all, to reconcile any divergence of state between the
	  *     incoming and the local version(s) it maintains.
	  * </p>
	  * @param state The incoming state that is to be presented to server
	  */
	def accept(incomingState: AtomicObjectState[A]) // todo return some kind of status/result

	/**
	  * <p>
	  *     Request the status of the object's state.
      * </p>
	  * @param objectId The id of the object/state of which the status is requested
	  */
	def status(objectId: ObjectId) // TODO: return type with relevant info (latest version?)
}
