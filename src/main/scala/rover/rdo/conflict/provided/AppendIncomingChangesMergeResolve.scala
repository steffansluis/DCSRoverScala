package rover.rdo.conflict.provided

import rover.rdo.conflict.{ConflictResolutionMechanism, ConflictedState, ResolvedMerge}

/**
  * <p>
  *     Simply takes the incoming changes (diff between common ancestor and incoming) and applies
  *     them on top of the server version.
  * </p><br />
  *
  * <p>
  *     A framework supplied implementation of `ConflictResolutionMechanism`
  * </p>
  */
class AppendIncomingChangesMergeResolve[A] extends ConflictResolutionMechanism[A] {
		
	override def resolveConflict(conflictedState: ConflictedState[A]): ResolvedMerge[A] = {
		val changesIncoming = conflictedState.changesIncomingRelativeToCommonAncestor
		var versionToApplyOn = conflictedState.serverVersion
		
		for (operationToApply <- changesIncoming.asList){
			versionToApplyOn = operationToApply.appliedFunction(versionToApplyOn)
		}
		
		val resultingVersion = versionToApplyOn
		
		return new ResolvedMerge[A](conflictedState, resultingVersion.immutableState, this)
	}
}
