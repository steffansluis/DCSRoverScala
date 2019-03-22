package chatapp

import rover.Client.OAuth2Credentials
import rover.rdo.state.AtomicObjectState
import rover.{Client, Server, Session, HTTPServer}


class ChatServer extends HTTPServer[List[ChatMessage]](_mapToStates = Map("chat" -> ChatServer.CHAT_STATE)) {

}

object ChatServer {
	val CHAT_STATE = AtomicObjectState.initial(List[ChatMessage](new ChatMessage("test", ChatUser.Steffan)))

	def main(args: Array[String]): Unit = {
		val server = new ChatServer
		server.start()
	}
}
