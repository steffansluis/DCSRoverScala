package rover.rdo.comms.fresh_attempt.http

import rover.rdo.ObjectId
import rover.rdo.comms.fresh_attempt.Server
import spark.Spark._

class ServerHttpInterface[A <: Serializable](
	private val applicationName: String,
	private val serverImpl: Server[A]
) {
	// Create an endpoint for the "get" server method
	get(s"$applicationName/get/:objectId", (request, result) => {
		val objectIdStringInRequestParam = request.params(":objectId")
		val objectId = ObjectId.from(objectIdStringInRequestParam)

		val latestOnServer = serverImpl.get(objectId)
		val serializedState = new SerializedAtomicObjectState[A](latestOnServer)

		result.body(serializedState.asString)
		result.`type`("application/octet-stream")

		result
	})

	post(s"$applicationName/accept", (request, result) => {
		val bytes = request.bodyAsBytes()
		val deserializedAtomicObjectState = new DeserializedAtomicObjectState[A](bytes)

		val incomingState = deserializedAtomicObjectState.asAtomicObjectState
		serverImpl.accept(incomingState)

		result.status(200)
		result
	})

	get(s"$applicationName/status", (request, result) => {
		// TODO
		result.status(200)

		result
	})
}
