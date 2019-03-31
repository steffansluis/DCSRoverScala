package rover.rdo.comms.fresh_attempt
import rover.rdo.ObjectId
import rover.rdo.conflict.ConflictedState
import rover.rdo.state.AtomicObjectState

/**
  * Simple implementation without persistence. All persistence is implemented
  * as HashMaps. These are not persisted between restarts of the server.
  * @tparam A The actual state type (i.e. `AtomicObjectState[A]`)
  */
class VolatileServer[A <: Serializable] private (
	val serverConfiguration: ServerConfiguration[A],
	var storage: Map[ObjectId, AtomicObjectState[A]]
) extends Server[A] {

	override def create(): AtomicObjectState[A] = {
		val initial = AtomicObjectState.initial(serverConfiguration.initialStateValue)
		storage = storage.updated(initial.objectId, initial)

		return initial
	}

	override def get(objectById: ObjectId): Option[AtomicObjectState[A]] = {
		return storage.get(objectById)
	}

	override def accept(incomingVersion: AtomicObjectState[A]): Unit = {
		val serverVersion = get(incomingVersion.objectId)
			.getOrElse(throw new Exception("Cannot accept incoming version, no corresponding object/state known to the server"))

		val conflictedState = ConflictedState.from(serverVersion, incomingVersion)
		val mergeResult = serverConfiguration.conflictResolutionMechanism.resolveConflict(conflictedState)

		storage = storage updated (incomingVersion.objectId, mergeResult.asAtomicObjectState)
	}

	override def status(objectId: ObjectId): Unit = {
		// TODO: something
	}
}
