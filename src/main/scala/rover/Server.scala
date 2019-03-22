package rover

import java.security.Timestamp
import java.util.Date

import rover.Client.OAuth2Credentials
import rover.rdo.AtomicObjectState
import rover.rdo.client.{CommonAncestor, RdObject}
import lol.http._
import lol.json._

import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.IO
import chatapp.{ChatMessage, ChatUser}
import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import scalaoauth2.provider.{AuthInfo, AuthorizationRequest, ClientCredential, DataHandler, Password, TokenEndpoint}

import scala.util.Try
import scala.async.Async.{async, await}
import scala.concurrent.Future


/**
  * Encapsulating the logic of the server.
  * @param address of the server
  * @param mapToClients, map to clients using as key the access token granted to the client
  * @param mapToStates, map to stable (committed) states of RDOs
*/
class Server[C, A]( protected val address: String,
                    protected val mapToClients: Map[Session[C, A]#Identifier, Client[C, A]], protected var mapToStates: Map[String, AtomicObjectState[A]]) {

  //FIXME: Does the server has its own creds? It merely keeps track of clients' creds
//  val credentials = null

  // TODO: Determine what to do with this
  def clientFromCredentials(credentials: Session[C,A]#Identifier): Client[C, A] = {
    new Client[C, A](this.address, credentials, Map[String, AtomicObjectState[A]]())
  }

  def createSession(credentials: C, identifier: Session[C,A]#Identifier): Session[C, A] = {
    new Session[C, A](credentials, this, clientFromCredentials(identifier))
  }

  def getAtomicStateWithId(stateId: String): AtomicObjectState[A] = {
        return mapToStates(stateId)
  }

  def deliveredState(stateId: String, atomicState: AtomicObjectState[A]): Unit ={
    this.mapToStates = this.mapToStates + (stateId -> atomicState)
  }

  def receivedState(stateId: String, state: AtomicObjectState[A]): Unit ={
    val clientRDO = new RdObject[A](state)
    val serverRDO = new RdObject[A](mapToStates(stateId))
    val ancestor = new CommonAncestor[A](serverRDO, clientRDO)
    if (ancestor == serverRDO) deliveredState(stateId, state)
    else {
      //FiXME: Conflict resolution and history diff stuff
    }
  }
}

class HTTPServer[A](
  val port: Int = 8888,
  _mapToClients: Map[Session[OAuth2Credentials, A]#Identifier, Client[OAuth2Credentials, A]] = Map[Session[OAuth2Credentials, A]#Identifier, Client[OAuth2Credentials, A]](),
  _mapToStates: Map[String, AtomicObjectState[A]] = Map[String, AtomicObjectState[A]]()
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

  val oauthDataHandler = new OAuthDataHandler()
  lazy val OAuthApi: PartialService = {
    		// Nothing special here, but look how we handle the 404 case.
		case GET at "/auth" =>
      println(s"Handling auth!")
      IO.fromFuture[Response] {
        IO {
          async {
            val request = new AuthorizationRequest(
              Map("Authorization" -> Seq("Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU=")),
              Map("grant_type" -> Seq("password"), "username" -> Seq("steffan"), "password" -> Seq("pass"), "scope" -> Seq("all")))

    //        val dataHandler =
            val f = new TokenEndpoint {
              override val handlers = Map(
                "password" -> new Password())
            }.handleRequest(request, oauthDataHandler)

    //        async {
              await(f) match {
                case Left(x) => InternalServerError(Error(x.description))
                case Right(x) => Ok(x.accessToken)
    //          }

      //        whenReady(f) { result => result should be('right) }
            }          }
        }
      }

//			IO {
//        val request = new AuthorizationRequest(
//          Map("Authorization" -> Seq("Basic Y2xpZW50X2lkX3ZhbHVlOmNsaWVudF9zZWNyZXRfdmFsdWU=")),
//          Map("grant_type" -> Seq("password"), "username" -> Seq("steffan"), "password" -> Seq("pass"), "scope" -> Seq("all")))
//
////        val dataHandler =
//        val f = new TokenEndpoint {
//          override val handlers = Map(
//            "password" -> new Password())
//        }.handleRequest(request, oauthDataHandler)
//
////        async {
//          await(f) match {
//            case Left(x) => InternalServerError(Error(x.description))
//            case Right(x) => Ok(x.accessToken)
////          }
//
//  //        whenReady(f) { result => result should be('right) }
//        }
//
//        Ok("Test")
//			}

  }

  lazy val Api: PartialService = {
		// Nothing special here, but look how we handle the 404 case.
		case GET at url"/api/rdo/$id" =>
      println(mapToStates)
      println(s"Getting $id from $mapToStates")
			IO {
				Try(id).toOption.flatMap(mapToStates.get).map { state =>
					Ok(state.asJson)
				}.getOrElse {
					NotFound(Error(s"No rdo found for id: `$id'"))
				}
			}

    case request @ POST at url"/api/rdo/$id" =>
      println(s"Handling POST request for $id")
      Try(id).toOption.flatMap(mapToStates.get).map { state =>
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
            deliveredState(id, updated)
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
		lol.http.Server.listen(port)(Hello.orElse(OAuthApi.orElse(Api)).orElse { case _ => NotFound })

//		async {
//			while(true) {}
//		}
	}
}

object Server {
//  val CHAT_STATE = AtomicObjectState.initial(List[Any]())
  def fromAddress[C, A](address: String): Server[C, A] = {
    return new Server[C, A](address, Map[Session[C, A]#Identifier, Client[C,A]](), Map[String, AtomicObjectState[A]]())
  }

  def getMapOfServer[C, A](server: Server[C, A]): Map[String, AtomicObjectState[A]] ={
    return server.mapToStates
  }
}

object Crypto {
  def generateToken(): String = {
    return "token"
  }
}

class OAuthDataHandler[User](identifier: OAuthDataHandler[User]#UserIdentifier = (user: User) => user.toString) extends DataHandler[User] {
  type ID = String
  type UserIdentifier = User => ID

  val _user: User = ChatUser.Steffan.asInstanceOf[User]
  val _access_token: scalaoauth2.provider.AccessToken = scalaoauth2.provider.AccessToken("access_token!", Some("refresh_token"), Some("scope"), Some(3600.toLong), new Date())
  val _auth_info: scalaoauth2.provider.AuthInfo[User] = AuthInfo[User](_user, Some("chat"), Some("scope"), Some(""))

  override def validateClient(maybeCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Boolean] = {
    async { true }
  }

  def deleteAuthCode(code: String): scala.concurrent.Future[Unit] = {
    async {

    }
  }

  def findUser(maybeCredential: Option[scalaoauth2.provider.ClientCredential],request: scalaoauth2.provider.AuthorizationRequest): scala.concurrent.Future[Option[User]] = {
    async {
      Some(_user)
    }
  }

//  def validateClient(clientId: String, clientSecret: String, grantType: String): Boolean = {
//    true
//    //    Clients.validate(clientId, clientSecret, grantType)
//  }

//  override def findUser(maybeCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Option[User]] =
  def findUser(username: String, password: String): Future[Option[User]] = {
    async {
      Some {
        if (username == "steffan") ChatUser.Steffan.asInstanceOf[User]
        else if (username == "giannis") ChatUser.Giannis.asInstanceOf[User]
        else new ChatUser(username).asInstanceOf[User]
      }
    }
    //    Users.findUser(username, password)
  }

  def createAccessToken(authInfo: AuthInfo[User]): Future[scalaoauth2.provider.AccessToken] = {
//    val accessTokenExpiresIn = 60 * 60 // 1 hour
//    val now = new Date()
//    val createdAt = new Timestamp(now.getTime)
//    val refreshToken = Some(Crypto.generateToken())
//    val accessToken = Crypto.generateToken()
//    val userId = identifier(authInfo.user)

    async {
    _access_token
    }

//    val tokenObject = models.AccessToken(accessToken, refreshToken, userId, authInfo.scope, accessTokenExpiresIn, createdAt, authInfo.clientId)
//    AccessTokens.deleteExistingAndCreate(tokenObject, userId, authInfo.clientId)
//    scalaoauth2.provider.AccessToken(accessToken, refreshToken, authInfo.scope, Some(accessTokenExpiresIn.toLong), now)
  }

  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[scalaoauth2.provider.AccessToken]] = {
    val userId = identifier(authInfo.user)
    async {
      Some(_access_token)
    }
    //    AccessTokens.findToken(userId, authInfo.clientId) map { a =>
//      scalaoauth2.provider.AccessToken(a.accessToken, a.refreshToken, a.scope, Some(a.expiresIn.toLong), a.createdAt)
//    }
  }

  def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[scalaoauth2.provider.AccessToken] = {
//    createAccessToken(authInfo)
    async {
      _access_token
    }
  }

  def findClientUser(clientId: String, clientSecret: String, scope: Option[String]): Option[User] = {
    None // Not implemented yet
  }

  def findAccessToken(token: String): Future[Option[scalaoauth2.provider.AccessToken]] = {
    async { Some(_access_token) }
    //    AccessTokens.findAccessToken(token) map { a =>
//      scalaoauth2.provider.AccessToken(a.accessToken, a.refreshToken, a.scope, Some(a.expiresIn.toLong), a.createdAt)
//    }
  }

  def findAuthInfoByAccessToken(accessToken: scalaoauth2.provider.AccessToken): Future[Option[AuthInfo[User]]] = {
    async {
      Some(_auth_info)
    }
//    AccessTokens.findAccessToken(accessToken.token) map { a =>
//      val user = Users.getById(a.userId).get
//      AuthInfo(user, a.clientId, a.scope, Some(""))
//    }
  }

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] = {
    async {
      Some(_auth_info)
    }
    //    AccessTokens.findRefreshToken(refreshToken) map { a =>
//      val user = Users.getById(a.userId).get
//      AuthInfo(user, a.clientId, a.scope, Some(""))
//    }
  }

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = {
    async { Some(_auth_info) }
    //    AuthCodes.find(code) map { a =>
//      val user = Users.getById(a.userId).get
//      AuthInfo(user, a.clientId, a.scope, a.redirectUri)
//    }
  }

}