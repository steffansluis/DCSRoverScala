package rover.rdo

import rover.rdo.client.RdObject
import rover.rdo.conflict.ConflictResolutionMechanism

class RdoApplication[STATE_IMPL, RDO <: RdObject[STATE_IMPL]](val conflictResolutionMechanism: ConflictResolutionMechanism[STATE_IMPL]) {

}
