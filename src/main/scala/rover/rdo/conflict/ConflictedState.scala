package rover.rdo.conflict

import rover.rdo.{AtomicObjectState, CommonAncestor}
import rover.rdo.client.{DiffWithAncestor, RdObject}

class ConflictedState[A] private (val serverVersion: AtomicObjectState[A], val incomingVersion: AtomicObjectState[A]) {
	def commonAncestor: CommonAncestor[A] = {
		return new CommonAncestor[A](serverVersion, incomingVersion)
	}
	def diffWithAncestor(childState: AtomicObjectState[A]): DiffWithAncestor[A] = {
		val commonAncestor = this.commonAncestor.state
		return new DiffWithAncestor[A](childState, commonAncestor)
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
