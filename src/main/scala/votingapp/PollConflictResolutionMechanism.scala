package votingapp

import rover.rdo.conflict.{ConflictResolutionMechanism, ConflictedState}
import rover.rdo.state.AtomicObjectState

class PollConflictResolutionMechanism extends ConflictResolutionMechanism[Votes] {
	override def resolveConflict(conflictedState: ConflictedState[Votes]): AtomicObjectState[Votes] = {
		val changesIncoming = conflictedState.changesIncomingRelativeToCommonAncestor

		// simply assume no same vote can be present in both diffs, hence append-only
		// when including some kind of identity to the vote, then can be more smarter here
		var versionUnderResolution = conflictedState.serverVersion
		for (change <- changesIncoming.asList) {
			versionUnderResolution = versionUnderResolution.applyOp()
		}

		return versionUnderResolution
	}
}
