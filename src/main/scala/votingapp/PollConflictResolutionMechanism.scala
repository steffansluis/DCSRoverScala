package votingapp

import rover.rdo.conflict.{ConflictResolutionMechanism, ConflictedState, ResolvedMerge, SimpleConflictResolutionMechanism}

class PollConflictResolutionMechanism(conflictedState: ConflictedState[Votes]) {
//	override def resolveConflict(conflictedState: ConflictedState[Votes]): ResolvedMerge[Votes] = {
//		val changesIncoming = conflictedState.changesIncomingRelativeToCommonAncestor
//		var versionToApplyOn = conflictedState.serverVersion
//
//		for (operationToApply <- changesIncoming.asList) {
//			versionToApplyOn = operationToApply.appliedFunction(versionToApplyOn)
//		}
//
//		val resultingVersion = versionToApplyOn
//
//		// TODO: generate a merge log entry
//		return new ResolvedMerge[Votes](conflictedState, resultingVersion.immutableState, this)
//	}

	def resolved: ResolvedMerge[Votes] = {
		return new SimpleConflictResolutionMechanism[Votes].resolveConflict(conflictedState)
	}
}
