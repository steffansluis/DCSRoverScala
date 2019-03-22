package votingapp

import rover.rdo.conflict.{ConflictResolutionMechanism, ConflictedState}
import rover.rdo.state.{AtomicObjectState, BasicAtomicObjectState}

class PollConflictResolutionMechanism extends ConflictResolutionMechanism[Votes] {
	override def resolveConflict(conflictedState: ConflictedState[Votes]): AtomicObjectState[Votes] = {
		val changesIncoming = conflictedState.changesIncomingRelativeToCommonAncestor
		var versionToApplyOn = conflictedState.serverVersion

		// simply assume no same vote can be present in both diffs, hence append-only
		// when including some kind of identity to the vote, then can be more smarter here
//		var versionUnderResolution = conflictedState.serverVersion
//		for (change <- changesIncoming.asList) {
//			versionUnderResolution = versionUnderResolution.applyOp()
//		}
//
//		return versionUnderResolution
//
//		return AtomicObjectState.fromLog(conflictedState.commonAncestor.state.log +: )

		for (operationToApply <- changesIncoming.asList) {
			versionToApplyOn = operationToApply.appliedFunction(versionToApplyOn)
		}

		val resultingVersion = versionToApplyOn

		return resultingVersion

//		versionToApplyOn.
//
//		return AtomicObjectState.fromLog(conflictedState.serverVersion.log.appended(changesIncoming.asList))
	}
}
