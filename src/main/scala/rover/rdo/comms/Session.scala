package rover.rdo.comms

import chatapp.ChatServer
import rover.rdo.ObjectId
import rover.rdo.state.AtomicObjectState

import scala.async.Async.async
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Session[C, A](credentials: C, server: Server[C, A], client: Client[C, A]) {
  type Id = C => ObjectId

  // FIXME hardcoded
  val chatObjectId = ObjectId.from("chat")

  // TODO: Implement errors
  def importRDO(objectId: ObjectId): Future[AtomicObjectState[A]] = {
    async{
      if (objectId == chatObjectId){
        ChatServer.CHAT_STATE.asInstanceOf[AtomicObjectState[A]]
      }
      else throw new RuntimeException(s"Cannot import RDO with id ${objectId.asString}, not supported")
    }
  }

  def importRDOwithState[A](objectId: ObjectId, stateId: String): Future[Unit] = {
    // TODO: Hacks
    async {
      if (objectId == chatObjectId) {
        val atomicState = server.getAtomicStateWithId(stateId)
        client.appendedState(stateId, atomicState)
      }
//      else new RuntimeException(s"Cannot import RDO with state ${stateId} with object id ${objectId.asString}, not supported")
    }
  }

  def exportRDOwithState[A](stateId: String): Future[Unit] = {
    async{
      val atomicState = client.getAtomicStateWithId(stateId)
      server.receivedState(stateId, atomicState)
    }
  }


}

object Session {
  // TODO: Probably better to make this mutable? It needs to be persistent in any case
  private var _CACHE = Map[Session[Any, Any]#Id, Session[Any, Any]]()

  def get[C, A](sessionId: Session[C, A]#Id): Session[C, A] = {
    //fixme: ugly
    _CACHE.get(sessionId.asInstanceOf[Session[Any, Any]#Id])
        .asInstanceOf[Session[C, A]]
  }

  def set[C, A](sessionId: Session[C, A]#Id, session: Session[C, A]): Unit = {
    _CACHE = _CACHE.updated(sessionId.asInstanceOf[Session[Any, Any]#Id], session.asInstanceOf[Session[Any, Any]])
  }
}