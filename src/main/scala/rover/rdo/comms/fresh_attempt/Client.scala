package rover.rdo.comms.fresh_attempt

import rover.rdo.ObjectId
import rover.rdo.state.AtomicObjectState

/**
  * Interface for the clients. Provides primitives to exchange
  * RdObject's state with the server
  * @tparam A The state itself
  */
abstract class Client[A <: Serializable] {
	def fetch(objectId: ObjectId)

	def push(state: AtomicObjectState[A])
}
