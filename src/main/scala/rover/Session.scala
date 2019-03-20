package rover

import rover.rdo.AtomicObjectState
import rover.rdo.client.RdObject

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async, await}

class Session[C](credentials: C, server: Server[C], client: Client[C]) {
  type Id = String
  type Identifier = C => Id

  // TODO: Move this to the proper place
  type ObjectId = Id

//  val server: Server[C] = null
//  val client: Client[C] = null

  // TODO: Implement errors
  def importRDO[T](objectId: ObjectId): Future[RdObject[T]] = {
    // TODO: Hacks
    async {
      if (objectId == "chat") new RdObject[T]( AtomicObjectState.initial(List[Any]().asInstanceOf[T]) )
      else null
    }
  }
  
  def exportRDO[T](rdo: RdObject[T]): ObjectId = {
    null
  }


}

object Session {
  // TODO: Probably better to make this mutable? It needs to be persistent in any case
  private var _CACHE = Map[Session[Any]#Id, Session[Any]]()

  def get[T](sessionId: Session[T]#Id): Session[T] = {
    _CACHE.get(sessionId).asInstanceOf[Session[T]]
  }

  def set[T](sessionId: Session[T]#Id, session: Session[T]): Unit = {
    _CACHE = _CACHE.updated(sessionId, session.asInstanceOf[Session[Any]])
  }
}