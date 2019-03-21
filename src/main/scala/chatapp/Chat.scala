package chatapp

import rover.rdo.AtomicObjectState
import rover.rdo.client.RdObject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async, await}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


// FIXME: ensure messages can be read, but not modified or reassigned...
// FIXME: after state & rd object impl change
class Chat(_onStateModified: Chat#Updater, _initialState: AtomicObjectState[List[ChatMessage]]) extends RdObject[List[ChatMessage]](_initialState) {
//	type State = List[ChatMessage]
	type Updater = AtomicObjectState[List[ChatMessage]] => Future[Unit]

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
	def fromRDO(rdo: RdObject[List[ChatMessage]], _onStateModified: Chat#Updater): Chat = {
		new Chat(_onStateModified, rdo.state)
	}
}


