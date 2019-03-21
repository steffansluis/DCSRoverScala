package rover

import rover.rdo.AtomicObjectState
import rover.rdo.client.RdObject

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async, await}

class Session[C, A](credentials: C, server: Server[C, A], client: Client[C, A]) {
  type Id = String
  type Identifier = C => Id

  // TODO: Move this to the proper place
  type ObjectId = Id

//  val server: Server[C] = null
//  val client: Client[C] = null

  // TODO: Implement errors
  def importRDO(objectId: ObjectId): Future[AtomicObjectState[A]] = {
    async{
      if (objectId == "chat"){
        AtomicObjectState.initial[A](List[Any]().asInstanceOf[A])
      }
      else null
    }
  }

  def importRDOwithState[A](objectId: ObjectId, stateId: String): Future[Unit] = {
    // TODO: Hacks
    async {
      if (objectId == "chat") {
        val atomicState = server.getAtomicStateWithId(stateId)
        client.appended(stateId, atomicState)
      }
      else null
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
    _CACHE.get(sessionId).asInstanceOf[Session[C, A]]
  }

  def set[C, A](sessionId: Session[C, A]#Id, session: Session[C, A]): Unit = {
    _CACHE = _CACHE.updated(sessionId, session.asInstanceOf[Session[Any, Any]])
  }
}