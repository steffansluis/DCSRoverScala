package rover.rdo.comms.fresh_attempt.http

import com.mashape.unirest.http.Unirest
import rover.rdo.ObjectId
import rover.rdo.comms.fresh_attempt.Client
import rover.rdo.state.AtomicObjectState

/**
  * Provides a simple to use API to communicate to a state/object server
  * over HTTP.
  *
  * @note This class could have extended the ServerHttpInterface "interface",
  *       but  doing so has potential to cause confusion. As well as forcing
  *       server-side method naming onto the client (accept vs push). Can be
  *       overcome in the future with better naming (both interface as API)
  * @param endpointPaths
  * @tparam A The state itself
  */
class ClientForServerOverHttp[A <: Serializable](
	private val endpointPaths: ServerHttpEndpointPaths
) extends Client[A]{

	override def created(): AtomicObjectState[A] = {
		val createResponse = Unirest.get(endpointPaths.createEndpoint).asBinary()
		val received = DeserializedAtomicObjectState[A](createResponse.getRawBody).asAtomicObjectState

		return received
	}

	override def fetch(objectId: ObjectId): AtomicObjectState[A] = {
		val fetchResponse = Unirest.get(endpointPaths.getEndpoint + "/{objectId}")
    		.routeParam("objectId", objectId.asString)
			.asBinary()
		
		val deserialized = DeserializedAtomicObjectState[A](fetchResponse.getRawBody)

		return deserialized.asAtomicObjectState
	}

	override def push(state: AtomicObjectState[A]): Unit = {
		val serialized = new SerializedAtomicObjectState[A](state)
		Unirest.post(endpointPaths.acceptEndpoint).body(serialized.asBytes)

		// TODO: ?
	}
}
