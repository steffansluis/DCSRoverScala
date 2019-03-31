package chatapp

import rover.HTTPServer
import rover.rdo.state.AtomicObjectState

class ChatServer extends HTTPServer[List[ChatMessage]](_mapToStates = Map("chat" -> ChatServer.CHAT_STATE)) {

}

object ChatServer {
	val CHAT_STATE = AtomicObjectState.initial(List[ChatMessage](new ChatMessage("test", ChatUser.Steffan)))

	def main(args: Array[String]): Unit = {
//		val server = new ChatServer
//		server.start()
//	implicit val stateEncoder: Encoder[AtomicObjectState[List[ChatMessage]]] = Encoder.forProduct1("immutableState")(rdo => (rdo.immutableState))
//	implicit val stateDecoder: Decoder[AtomicObjectState[List[ChatMessage]]] = deriveDecoder[AtomicObjectState[List[ChatMessage]]]

		// Immutablestate only
//		var listJson = CHAT_STATE.immutableState.asJson
//		print(listJson)

		// As 'Any' state
//    val anyStateJson = CHAT_STATE.asInstanceOf[AtomicObjectState[Any]].asJson
//    println(anyStateJson)

		// Generic state
//		val stateJson = CHAT_STATE.asJson
//    println(stateJson)

		val toJsonState = AtomicObjectState.toJson(CHAT_STATE)
		println(toJsonState)
	}
}
