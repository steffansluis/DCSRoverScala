package rover

import chatapp.ChatServer
import rover.rdo.{ObjectId, RdObject}
import rover.rdo.state.AtomicObjectState

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async, await}

class Session[C, A](credentials: C, server: Server[C, A], client: Client[C, A]) {
  type Id = String
  type Identifier = C => Id

//  // TODO: Move this to the proper place
//  type ObjectId = Id


  // TODO: Implement errors
  def importDummyRDO(objectId: ObjectId): Future[AtomicObjectState[A]] = {
    async{
      if (objectId == "chat"){
        ChatServer.CHAT_STATE.asInstanceOf[AtomicObjectState[A]]
      }
      else null
    }
  }

  def importRDO(stateId: ObjectId) : Future[AtomicObjectState[A]] = {
    async{
      if (server.containsStateId(stateId)) server.getAtomicStateWithId(stateId)
      else null
    }
  }
  
  def exportRDOwithState[A](stateId: ObjectId): Future[Unit] = {
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
    _CACHE.get(sessionId).asInstanceOf[Session[C, A]]
  }

  def set[C, A](sessionId: Session[C, A]#Id, session: Session[C, A]): Unit = {
    _CACHE = _CACHE.updated(sessionId, session.asInstanceOf[Session[Any, Any]])
  }
}