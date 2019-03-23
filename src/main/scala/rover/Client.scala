package rover

import lol.http._
import lol.json._
import rover.Client.OAuth2Credentials
import rover.rdo.state.AtomicObjectState
import cats.implicits._
import cats.effect.IO

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async, await}
import io.circe._
import rover.rdo.state.AtomicObjectState

/**
  * Class encapsulating a Client, who interacts with a Server within a Session
  * for exchanging RDOs (in fact their atomic states).
  * @param serverAddress. the address of the corresponding server
  * @param identifier, the access token granted to the client for authorized access to server
  * @param mapToStates, map to up-to-date version of local RDOs
  */
//FIXME: create a unique, static id for each RDO upon its creation
class Client[C, A](protected val serverAddress: String, protected val identifier: Session[C, A]#Identifier,
                   protected var mapToStates: Map[String, AtomicObjectState[A]] = Map[String, AtomicObjectState[A]]()){


  val server = Server.fromAddress[C,A](serverAddress)
  def createSession(credentials: C): Session[C, A] = {
    server.createSession(credentials, this.identifier)
  }

  def appendedState(stateId: String, atomicState: AtomicObjectState[A]): Unit ={
    this.mapToStates = this.mapToStates + (stateId -> atomicState)
  }

  def getAtomicStateWithId(stateId: String): AtomicObjectState[A] ={
    return mapToStates(stateId)
  }
}


object Client {
  class OAuth2Credentials(val accessToken: String, val refreshToken: String) {}
  type OAuth2Client[A] = Client[OAuth2Credentials, A]

  def oauth2[A](): OAuth2Client[A] = {
    null
  }
}

class HTTPClient[A](_serverAddress: String, _identifier: Session[OAuth2Credentials, A]#Identifier)(implicit val encodeA: Encoder[A], implicit val decodeA: Decoder[A]) extends Client[OAuth2Credentials, A](_serverAddress, _identifier) {

    implicit val stateEncoder: Encoder[AtomicObjectState[A]] = Encoder.forProduct1("immutableState")(rdo => (rdo.immutableState))
  //	implicit val stateDecoder: Decoder[AtomicObjectState[List[ChatMessage]]] = deriveDecoder[AtomicObjectState[List[ChatMessage]]]
//    implicit val rdoEncoder: Encoder[RdObject[A]] = Encoder.forProduct1("state")(rdo => (rdo.state))

    implicit val decodeAtomicObjectState: Decoder[AtomicObjectState[A]] = new Decoder[AtomicObjectState[A]] {
		final def apply(c: HCursor): Decoder.Result[AtomicObjectState[A]] =
			for {
				immutableState <- c.downField("immutableState").as[A]
			} yield {
				AtomicObjectState.initial(immutableState)
			}
	}

  def importRDO(objectId: String): AtomicObjectState[A] = {
    val roverClient = lol.http.Client(serverAddress, 8888, "http")
    val userAgent = h"User-Agent" -> h"lolhttp"

    val getState = (for {
      result <- roverClient.run(Get(s"/api/rdo/$objectId").addHeaders(userAgent)) {
        _.readSuccessAs[Json].map(json => {
          json.as[AtomicObjectState[A]]
        })
      }

      state <- IO {
        result.right.get
      }
      _ <-  roverClient.stop()

    } yield (state)).onError { case _ => roverClient.stop() }


    getState.unsafeRunSync

  }

  def exportRDO(objectId: String, state: AtomicObjectState[A]): Unit = {
    val roverClient = lol.http.Client(serverAddress, 8888, "http")
    val userAgent = h"User-Agent" -> h"lolhttp"

//    println(s"Exporting RDO $objectId as $state")
    val setState = (for {
      result <- roverClient.run(Post(s"/api/rdo/$objectId", stateEncoder.apply(state).toString).addHeaders(userAgent)) {
        _.readSuccessAs[Json].map(json => {
//          println(s"Received JSON response: $json")
//          json.as[AtomicObjectState[A]]
        })
      }

//      state <- IO {
//        if (result.isLeft) {
//          println(s"${result.left.get}")
//        }
//        result.right.get
//      }
      _ <-  roverClient.stop()

    } yield (state)).onError { case _ => roverClient.stop() }


    setState.unsafeRunSync

  }

}