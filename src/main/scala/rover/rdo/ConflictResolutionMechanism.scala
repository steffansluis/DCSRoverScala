package rover.rdo

import rover.rdo.client.{CommonAncestor, RdObject}

trait ConflictResolutionMechanism[A] {
	def resolveConflict(conflictedState: ConflictedState[A]): AtomicObjectState[A]
}

class ConflictedState[A] private (val serverVersion: AtomicObjectState[A], val incomingVersion: AtomicObjectState[A]) {
	def commonAncestor: CommonAncestor[A] = {
		return new CommonAncestor[A](serverVersion, incomingVersion)
	}
}

object ConflictedState {
	def from[A](serverVersion: RdObject[A], incomingVersion: RdObject[A]): ConflictedState[A] = {
		return new ConflictedState[A](serverVersion.state, incomingVersion.state)
	}

	def from[A](serverVersion: AtomicObjectState[A], incomingVersion: AtomicObjectState[A]): ConflictedState[A] = {
		return new ConflictedState[A](serverVersion, incomingVersion)
	}
}