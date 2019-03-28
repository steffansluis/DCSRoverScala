package chatapp.model

import chatapp.{ChatConflictResolutionMechanism, ChatUser}
import rover.rdo.RdObject
import rover.rdo.conflict.{CommonAncestor, ConflictedState}
import rover.rdo.state.{AtomicObjectState, InitialAtomicObjectState}

import scala.async.Async.async
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


// FIXME: ensure messages can be read, but not modified or reassigned...
// FIXME: after state & rd object impl change
class Chat(_initialState: AtomicObjectState[List[ChatMessage]]) extends RdObject[List[ChatMessage]](_initialState) {
//	type State = List[ChatMessage]
	type Updater = AtomicObjectState[List[ChatMessage]] => Future[Unit]

	val _onStateModified: Chat#Updater = null

	def send(message: ChatMessage): Future[Unit]= {
		val op: AtomicObjectState[List[ChatMessage]]#Op = s => s :+ message

		async {
			modifyState(op)
		}
	}


	override def onStateModified(oldState: AtomicObjectState[List[ChatMessage]]): Future[Unit] = {
		_onStateModified(state)
	}

	def currentVersion(): Long = {
		immutableState.size
	}

}

object Chat {
	def fromRDO(rdo: RdObject[List[ChatMessage]]): Chat = {
		new Chat(rdo.state)
	}

	def copyOf(chat: Chat) = {
		new Chat(chat.state)
	}
}

object test {
	def main(args: Array[String]): Unit ={
		val initialState = new InitialAtomicObjectState[List[ChatMessage]](List(new ChatMessage("Welcome", new ChatUser("system"))))
		val chat = new Chat(initialState)
		val THREAD_SLEEP = 1000

		//stage 1: Copying
		val res = chat.send(new ChatMessage("Hey", ChatUser.Giannis))
		Thread.sleep(THREAD_SLEEP)
		val chat2 = Chat.copyOf(chat)

		println("**** Stage 1: Should be equal here ****")
		println(chat.state.immutableState.last.toString)
		println(chat2.state.immutableState.last.toString)

		//staget 2: forking
		chat.send(new ChatMessage("Wassup", ChatUser.Steffan))
		chat.send(new ChatMessage("Yo", ChatUser.Steffan))

		chat2.send(new ChatMessage("YoYo", ChatUser.Steffan))


		Thread.sleep(THREAD_SLEEP)
		println(s"\n\n")
		println("**** Stage 2: Diverged ****")
		println(chat.state.immutableState.last.toString)
		println(chat2.state.immutableState.last.toString)

		val commmonAncestor = CommonAncestor.from(chat, chat2)
		val commonAncestorState = commmonAncestor.state

		println(s"\n\n")
		println("**** Common Ancestor ****")
		println(s"state: ${commonAncestorState.immutableState.last.toString}")


		val resolved = new ChatConflictResolutionMechanism().resolveConflict(ConflictedState.from(chat, chat2))
		println(s"\n\n")
		println("**** Conflict Resolution ****")
		println(resolved.asAtomicObjectState.immutableState.last.toString)
		println(resolved.asAtomicObjectState.log.asList.mkString("\n     "))

	}


}



