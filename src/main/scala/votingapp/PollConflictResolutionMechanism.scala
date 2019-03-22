package votingapp

import rover.rdo.conflict.{ConflictResolutionMechanism, ConflictedState, ResolvedMerge}

class PollConflictResolutionMechanism extends ConflictResolutionMechanism[Votes] {
	override def resolveConflict(conflictedState: ConflictedState[Votes]): ResolvedMerge[Votes] = {
		val changesIncoming = conflictedState.changesIncomingRelativeToCommonAncestor
		var versionToApplyOn = conflictedState.serverVersion

		for (operationToApply <- changesIncoming.asList) {
			versionToApplyOn = operationToApply.appliedFunction(versionToApplyOn)
		}

		val resultingVersion = versionToApplyOn

		// TODO: generate a merge log entry
		return new ResolvedMerge[Votes](conflictedState, resultingVersion.immutableState, this)
	}
}
