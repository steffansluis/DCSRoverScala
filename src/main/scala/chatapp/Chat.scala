package chatapp

import rover.rdo.client.RdObject

// FIXME: ensure messages can be read, but not modified or reassigned...
class Chat(var messages: List[ChatMessage]) extends RdObject {

	// TODO: Something with users, crypto stuff on identity

	def appendMessage(chatMessage: ChatMessage): Unit = {
		messages = messages :+ chatMessage
	}

	override def version: Long = {
		messages.size
	}

}

class ChatMessage(val body: String,
                  val author: String,
                  val timestamp: Long = java.time.Instant.now.getEpochSecond())
{

}