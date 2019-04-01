package chatapp

import chatapp.model.ChatMessage
import rover.rdo.ObjectId
import rover.rdo.comms.HTTPServer
import rover.rdo.comms.fresh_attempt.{Server, ServerConfiguration}
import rover.rdo.comms.fresh_attempt.http.{ServerHttpInterface, EphemeralServer}
import rover.rdo.state.AtomicObjectState

// Previous impl:
//class ChatServer extends HTTPServer[List[ChatMessage]](_mapToStates = Map("chat" -> ChatServer.CHAT_STATE)) {
//
//}

class ChatServer(private val serverImpl: EphemeralServer[List[ChatMessage]]) extends Server[List[ChatMessage]] {
	
	private val restInterface = new ServerHttpInterface[List[ChatMessage]]("chatapp", 8080, serverImpl)
	
	override def create(): AtomicObjectState[List[ChatMessage]] = {
		return serverImpl.create()
	}
	
	override def get(objectId: ObjectId): Option[AtomicObjectState[List[ChatMessage]]] = {
		return serverImpl.get(objectId)
	}
	
	override def accept(incomingState: AtomicObjectState[List[ChatMessage]]): Unit = {
		serverImpl.accept(incomingState)
	}
	
	override def status(objectId: ObjectId): Unit = {
		serverImpl.status(objectId)
	}
}

object ChatServer {
	//new ChatMessage("test", ChatUser.Steffan))
	private val INITIAL = List[ChatMessage]()
	
	private val startingServerStateStore = {
		val initialAtomicObjectState = AtomicObjectState.initial(INITIAL)
		
		println(s"Created objectId for chat testing: ${initialAtomicObjectState.objectId}")
		// set for testing
		ObjectId.chatAppChat = initialAtomicObjectState.objectId
		
		// return
		Map(initialAtomicObjectState.objectId -> initialAtomicObjectState)
	}

	def main(args: Array[String]): Unit = {
		
		val serverConfig = new ServerConfiguration[List[ChatMessage]](INITIAL, new ChatConflictResolutionMechanism)
		val serverImpl = new EphemeralServer[List[ChatMessage]](serverConfig, startingServerStateStore)
		val server = new ChatServer(serverImpl)
		
		println("Chat application server has started")
		
		// will it keep on running?
	}
}
