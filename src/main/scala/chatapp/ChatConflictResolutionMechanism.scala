package chatapp

import rover.rdo.conflict.{ConflictResolutionMechanism, ConflictedState, ResolvedMerge, SimpleConflictResolutionMechanism}

class ChatConflictResolutionMechanism(conflictedState: ConflictedState[List[ChatMessage]]) {

//  override def resolveConflict(conflictedState: ConflictedState[List[ChatMessage]]): ResolvedMerge[List[ChatMessage]] = {
//    val changesIncoming = conflictedState.changesIncomingRelativeToCommonAncestor
//    var versionToApplyOn = conflictedState.serverVersion
//
//    for (operationToApply <- changesIncoming.asList){
//      versionToApplyOn = operationToApply.appliedFunction(versionToApplyOn)
//    }
//
//    val resultingVersion = versionToApplyOn
//
//    return new ResolvedMerge[List[ChatMessage]](conflictedState, resultingVersion.immutableState, this)
//  }

  def resolved: ResolvedMerge[List[ChatMessage]] = {
    return new SimpleConflictResolutionMechanism[List[ChatMessage]].resolveConflict(conflictedState)
  }
}

