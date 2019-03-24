package rover

import rover.rdo.RdObject

/**
  * This is a sort of combined repository & factory for RdObjects
  * Currently every RdObject is implemented with AtomicStateObjects
  *
  * @param server
  * @tparam A
  * @tparam C
  * @tparam RDO
  */
abstract class RoverApplication[A, C, RDO <: RdObject[A]](server: Server[A,C]) {
	def checkoutObject(id: String)
}
