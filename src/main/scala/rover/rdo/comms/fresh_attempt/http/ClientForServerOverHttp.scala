package rover.rdo.comms.fresh_attempt.http

import rover.rdo.ObjectId
import rover.rdo.comms.fresh_attempt.Client
import rover.rdo.state.AtomicObjectState

class ClientForServerOverHttp[A <: Serializable] extends Client[A]{
	override def fetch(objectId: ObjectId): Unit = ???

	override def push(state: AtomicObjectState[A]): Unit = ???
}
