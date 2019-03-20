package rover.rdo.conflict

import rover.rdo.AtomicObjectState

trait ConflictResolutionMechanism[A] {
	def resolveConflict(conflictedState: ConflictedState[A]): AtomicObjectState[A]
}
