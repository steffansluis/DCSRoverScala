package rover

import java.lang
import java.lang.System

import cats.effect.IO
import io.circe._
import io.circe.syntax._
import lol.http
import lol.http._
import lol.json._
import rover.Client.OAuth2Credentials
import rover.rdo.{ObjectId, RdObject}
import rover.rdo.conflict.ConflictedState
import rover.rdo.conflict.resolve.AppendIncomingChangesMergeResolve
import rover.rdo.state.AtomicObjectState

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
  * Encapsulating the logic of the server.
  * @param address of the server
  * @param mapToClients, map to clients using as key the access token granted to the client
  * @param mapToStates, map to stable (committed) states of RDOs
  * @tparam C type of credentials used for sessions (e.g. OAuth2Credentials for OAuth type of auth)
  * @tparam A type of state of the application (e.g. "Votes" in a poll app, List[Messages] in a chatapp)
*/
class Server[C, A](protected val address: String,
                   protected val mapToClients: Map[Session[C, A]#Identifier,
                     Client[C, A]], protected var mapToStates: Map[ObjectId, AtomicObjectState[A]]) {

  //FIXME: Does the server has its own creds? It merely keeps track of clients' creds
//  val credentials = null

  // TODO: Determine what to do with this
  def clientFromCredentials(credentials: Session[C,A]#Identifier): Client[C, A] = {
    new Client[C, A](this.address, credentials, Map[ObjectId, AtomicObjectState[A]]())
  }

  def createSession(credentials: C, identifier: Session[C,A]#Identifier): Session[C, A] = {
    new Session[C, A](credentials, this, clientFromCredentials(identifier))
  }

  def containsStateId(stateId: ObjectId): Boolean = {
    return this.mapToStates.contains(stateId)
  }

  def mapAtomicState(state: AtomicObjectState[A]) = {
    if (!mapToStates.values.exists(_ == state)){
      val objectId = ObjectId.generateNew()
      this.mapToStates = this.mapToStates + (objectId -> state)
    } else lang.System.err.println("Input state already exists")
  }

  def getAtomicStateWithId(stateId: ObjectId): AtomicObjectState[A] = {
        return mapToStates(stateId)
  }

  def deliveredState(stateId: ObjectId, incomingState: AtomicObjectState[A]): Unit ={
    this.mapToStates = this.mapToStates + (stateId -> incomingState)
  }

  def receivedState(stateId: ObjectId, incomingState: AtomicObjectState[A]): Unit ={
    val serverState = mapToStates(stateId)
    if (serverState != incomingState){
      val conflictedState = ConflictedState.from(serverState, incomingState)
	    // FIXME: make this pluggable, the app should decide.
      val resolver = new AppendIncomingChangesMergeResolve[A]
      val resolved = resolver.resolveConflict(conflictedState)
      this.mapToStates = this.mapToStates + (stateId -> resolved.asAtomicObjectState)
    }

  }
}

class HTTPServer[A](
  val port: Int = 8888,
  _mapToClients: Map[Session[OAuth2Credentials, A]#Identifier, Client[OAuth2Credentials, A]] = Map[Session[OAuth2Credentials, A]#Identifier, Client[OAuth2Credentials, A]](),
  _mapToStates: Map[ObjectId, AtomicObjectState[A]] = Map[ObjectId, AtomicObjectState[A]]()
)(implicit val encodeA: Encoder[A], implicit val decodeA: Decoder[A]) extends Server[OAuth2Credentials, A]("bla", _mapToClients, _mapToStates) {
  def Error(msg: String): Json = Json.obj("error" -> Json.fromString(msg))

	implicit val stateEncoder: Encoder[AtomicObjectState[A]] = Encoder.forProduct1("immutableState")(rdo => (rdo.immutableState))
//	implicit val stateDecoder: Decoder[AtomicObjectState[List[ChatMessage]]] = deriveDecoder[AtomicObjectState[List[ChatMessage]]]
	implicit val rdoEncoder: Encoder[RdObject[A]] = Encoder.forProduct1("state")(rdo => (rdo.state))

  implicit val decodeAtomicObjectState: Decoder[AtomicObjectState[A]] = new Decoder[AtomicObjectState[A]] {
		final def apply(c: HCursor): Decoder.Result[AtomicObjectState[A]] =
			for {
				immutableState <- c.downField("immutableState").as[A]
			} yield {
				AtomicObjectState.initial(immutableState)
			}
	}

//  val mapToState = _mapToStates;

	lazy val Hello: PartialService = {
			case GET at "/hello" =>
				Ok("Hello World, from Rover!")
//			case _ =>
//				NotFound
		}

  lazy val Api: PartialService = {
		// Nothing special here, but look how we handle the 404 case.
		case GET at url"/api/rdo/$id" =>
      println(mapToStates)
      println(s"Getting $id from ${_mapToStates}")
			IO {
				Try(ObjectId.generateFromString(id)).toOption.flatMap(_mapToStates.get).map { state =>
					Ok(state.asJson)
				}.getOrElse {
					NotFound(Error(s"No rdo found for id: `$id'"))
				}
			}

    case request @ POST at url"/api/rdo/$id" =>
      println(s"Handling POST request for $id")
      Try(ObjectId.generateFromString(id)).toOption.flatMap(_mapToStates.get).map { state =>
        // TODO: Decide what to return here
//        val existing: RdObject[A] = new RdObject[A](state)
        request.readAs[Json].map { jsonBody =>
          println(s"Request string: ${jsonBody.toString()}")
          val updatedState = jsonBody.as[AtomicObjectState[A]]
          if (updatedState.isLeft) {
            val failure = updatedState.left.get
            InternalServerError(Error(s"${failure.message}: \n${failure.history.mkString("\n")}"))
          } else {
            val updated = updatedState.right.get
            deliveredState(ObjectId.generateFromString(id), updated)
            Ok(new RdObject(updated).asJson)
          }
          //          val updatedRDO:  = state.copy(
//            text = root.text.string.getOption(jsonBody).getOrElse(state.text),
//            done = root.done.boolean.getOption(jsonBody).getOrElse(state.done)
//          )
        }
      }.getOrElse {
        NotFound(Error(s"No rdo found for id: `$id'"))
      }

	}

	def start(): Unit = {
//		val
		println(s"Starting server at port $port")
		lol.http.Server.listen(port)(Hello.orElse(Api).orElse { case _ => NotFound })

//		async {
//			while(true) {}
//		}
	}

}

object Server {
//  val CHAT_STATE = AtomicObjectState.initial(List[Any]())
  def fromAddress[C, A](address: String): Server[C, A] = {
    return new Server[C, A](address, Map[Session[C, A]#Identifier, Client[C,A]](), Map[ObjectId, AtomicObjectState[A]]())
  }

  def getMapOfServer[C, A](server: Server[C, A]): Map[ObjectId, AtomicObjectState[A]] ={
    return server.mapToStates
  }
}