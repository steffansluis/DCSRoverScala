package votingapp

import rover.rdo.AtomicObjectState
import rover.rdo.conflict.{ConflictResolutionMechanism, ConflictedState}

class PollConflictResolutionMechanism extends ConflictResolutionMechanism[Votes] {
	override def resolveConflict(conflictedState: ConflictedState[Votes]): AtomicObjectState[Votes] = {
		val changesIncoming = conflictedState.changesIncomingRelativeToCommonAncestor

		// simply assume no same vote can be present in both diffs, hence append-only
		// when including some kind of identity to the vote, then can be more smarter here
		var versionUnderResolution = conflictedState.serverVersion
		for (change <- changesIncoming.asList) {
			versionUnderResolution = versionUnderResolution.applyOp(change.op)
		}

		return versionUnderResolution
	}
}
