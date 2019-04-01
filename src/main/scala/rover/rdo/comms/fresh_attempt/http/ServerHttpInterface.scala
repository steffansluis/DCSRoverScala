package rover.rdo.comms.fresh_attempt.http

import rover.rdo.ObjectId
import rover.rdo.comms.fresh_attempt.Server
import spark.Spark
import spark.Spark._

/**
  * Provides a default HTTP "restful" endpoints for `Server[A]` implementations
  *
  * @note Not statistically checked during compile-time if implementation is complete.
  *       Could use Spring's RestControllers or other method-based frameworks instead
  *       of Spark to be able to achieve this.
  * @param applicationName The name of the object/state. Is used as part of the http path
  * @param serverImpl The underlying server implementation
  * @tparam A
  */
class ServerHttpInterface[A <: Serializable](
	private val applicationName: String,
	private val port: Integer,
	private val serverImpl: Server[A]
) {
	val endpointPaths = ServerHttpEndpointPaths.forServer(applicationName)

	Spark.port(port)
	
	get(endpointPaths.createEndpoint, (request, result) => {
		val newlyCreated = serverImpl.create()

		val serialized = new SerializedAtomicObjectState[A](newlyCreated)

//		result.body()
		result.`type`("application/octet-stream")

//		result
		serialized.asString
	})

	// Create an endpoint for the "get" server method
	get(endpointPaths.getEndpoint + "/:objectId", (request, result) => {
		val objectIdStringInRequestParam = request.params(":objectId")
		val objectId = ObjectId.from(objectIdStringInRequestParam)
		
		println(s"Get for objectId: $objectIdStringInRequestParam")

		val latestOnServer = serverImpl.get(objectId)
			.getOrElse({
				println("   was not found on server... throwing exception")
				throw new Exception("Server did not have requested object/state...")
			})

		println("   was found on server")
		
		val serializedState = new SerializedAtomicObjectState[A](latestOnServer)

//		result.()
		result.`type`("application/octet-stream")

//		result
		serializedState.asString
	})

	post(endpointPaths.acceptEndpoint, (request, result) => {
		val bytes = request.bodyAsBytes()
		val deserializedAtomicObjectState = new AtomicObjectStateAsByteArray[A](bytes)

		val incomingState = deserializedAtomicObjectState.asAtomicObjectState
		serverImpl.accept(incomingState)

		result.status(200)
		result
	})

	get(endpointPaths.statusEndpoint, (request, result) => {
		// TODO
		result.status(200)

		result
	})
}

final case class ServerHttpEndpointPaths private (private val prefix: String) {
	def createEndpoint = s"$prefix/create"
	def getEndpoint = s"$prefix/get"
	def acceptEndpoint = s"$prefix/accept"
	def statusEndpoint = s"$prefix/status"
}

object ServerHttpEndpointPaths {
	def forServer(appName: String): ServerHttpEndpointPaths = {
		return new ServerHttpEndpointPaths(appName)
	}
	
	def atServer(serverAddress: String, appName: String): ServerHttpEndpointPaths = {
		return new ServerHttpEndpointPaths(s"$serverAddress/$appName")
	}
}
