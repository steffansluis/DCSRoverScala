package rover

import rover.rdo.client.RdObject

abstract class RoverApplication[A, C, RDO <: RdObject[A]](server: Server[A,C]) {
}
