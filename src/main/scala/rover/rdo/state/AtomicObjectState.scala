package rover.rdo.state

import io.circe._
import io.circe.syntax._
import rover.rdo.ObjectState

trait AtomicObjectState[A] extends ObjectState {
	type Op = A => A

	def immutableState: A

	def log: StateLog[A]
	def applyOp(operation: Op): AtomicObjectState[A]

	override def equals(obj: Any): Boolean

//	def asJson()(implicit encodeA: Encoder[A]): Json = {
//
//	}
}

class InitialAtomicObjectState[A](identity: A)(implicit val encodeA: Encoder[A], implicit val decodeA: Decoder[A]) extends AtomicObjectState[A] {
	override def immutableState: A = identity
	
	override def log: StateLog[A] = StateLog.empty
	
	override def applyOp(operation: Op): AtomicObjectState[A] = {
		val resultingState = operation.apply(immutableState)
		return new BasicAtomicObjectState[A](resultingState, log)
	}
}

// TODO: make ctor private
class BasicAtomicObjectState[A](val immutableState: A, val log: StateLog[A])(implicit val encodeA: Encoder[A], implicit val decodeA: Decoder[A]) extends AtomicObjectState[A] {

	def applyOp(operation: Op): AtomicObjectState[A] = {
		// Operation must apply itself to the state
		// but we want the state to take in the operations
		// so that the framework can record the op
		val result = operation.apply(this.immutableState)

		// Record the operation in the Log
		val updatedLog = log.appended(OpAppliedRecord(operation, this))

		return new BasicAtomicObjectState[A](result, updatedLog)
	}

	override def equals(that: Any): Boolean = {
		that match{
			case that: AtomicObjectState[A] => this.immutableState == that.immutableState
			case _ => false
		}
	}

	override def toString: String = {
		immutableState.toString
	}

//	def asJson = new Encoder[AtomicObjectState[Any]] {
//		final def apply(s: AtomicObjectState[Any]): Json = Json.obj(
//			("test", Json.fromString("test")),
////			("immutableState", s.immutableState.asJson)
//			//			("log", s.log)
//		)
//	}
}

object AtomicObjectState {
//		implicit val encodeAtomicObjectState = new Encoder[AtomicObjectState[Any]] {
//		final def apply(s: AtomicObjectState[Any]): Json = Json.obj(
//			("test", Json.fromString("test")),
////			("immutableState", s.immutableState.asJson)
//			//			("log", s.log)
//		)
//	}
//
//	implicit val encodeAtomicObjectState = new Encoder[AtomicObjectState[Any]] {
//		final def apply[A](s: AtomicObjectState[A])(implicit encodeA: Encoder[A]): Json = Json.obj(
//			("test", Json.fromString("test")),
//			("immutableState", Json.fromString(encodeA(s.immutableState).toString()))
//			//			("log", s.log)
//		)
//	}

//	implicit val encodeAtomicObjectState = new Encoder[AtomicObjectState[Any]] {
//		final def apply(s: AtomicObjectState[Any]): Json = Json.obj(
//			("immutableState", s.immutableState.asJson)
//		)
//	}




//		implicit val encodeUser: Encoder[AtomicObjectState[A]] = new Encoder[AtomicObjectState[A]] {
//			final def apply(u: AtomicObjectState[A]): Json = Json.obj(
//				immutableState <- c.downField("immutableState").as[A]
//			)
//		}

	//		implicit val encodeAtomicObjectState: Encoder[AtomicObjectState[_] {}] = new Encoder[AtomicObjectState[_]] {
//			final def apply[A](m: AtomicObjectState[A] {}): Json = Json.obj(
////				("author", m.author.asJson),
////	//			("author", Json.fromString(m.author.username)),
////				("body", Json.fromString(m.body)),
////				("timestamp", Json.fromLong(m.timestamp))
//			)
//	}

//	implicit val decodeAtomicObjectState: Decoder[AtomicObjectState[_] {}] =

//	implicit val decodeAtomicObjectState: Decoder[AtomicObjectState[_] {}] = new Decoder[AtomicObjectState[_]] {
//		final def apply[A](c: HCursor)(implicit encodeA: Encoder[A], decodeA: Decoder[A]): Decoder.Result[AtomicObjectState[A] {}] =
//			for {
//				immutableState <- c.downField("immutableState").as[A]
////				body <- c.downField("body").as[String]
////				timestamp  <- c.downField("timestamp").as[Long]
//			} yield {
//				AtomicObjectState.initial(immutableState)
//			}
//	}

	def initial[A](value: A)(implicit encodeA: Encoder[A], decodeA: Decoder[A]): AtomicObjectState[A] = {
		return new BasicAtomicObjectState[A](value, StateLog.withInitialState(value))
	}

//	def fromLog[A](log: StateLog[A]): AtomicObjectState[A] = {
//		return log.latestState.resultingAtomic
//	}

	def byApplyingOp[A](stateFrom: AtomicObjectState[A], op: AtomicObjectState[A]#Op)(implicit encodeA: Encoder[A], decodeA: Decoder[A]): AtomicObjectState[A] = {
		val resultingState = op.apply(stateFrom.immutableState)
		val appendedLog = stateFrom.log.appended(new OpAppliedRecord[A](op, stateFrom))

		val resultingAtomicState = new BasicAtomicObjectState[A](resultingState, appendedLog)

		return resultingAtomicState
	}

	def toJson[A](state: AtomicObjectState[A])(implicit encodeA: Encoder[A], decodeA: Decoder[A]): Json = {
		implicit val encodeAtomicObjectState: Encoder[AtomicObjectState[A]] = new Encoder[AtomicObjectState[A]] {
			override def apply(a: AtomicObjectState[A]): Json = {
				Json.obj(
					("immutableState", a.immutableState.asJson),
					("log", a.log.asJson)
				)
			}
		}

		state.asJson
	}

	def fromJson[A](json: Json)(implicit encodeA: Encoder[A], decodeA: Decoder[A]): Option[AtomicObjectState[A]] = {
	  implicit val decodeAtomicObjectState: Decoder[AtomicObjectState[A]] = new Decoder[AtomicObjectState[A]] {
			final def apply(c: HCursor): Decoder.Result[AtomicObjectState[A]] =
				for {
					immutableState <- c.downField("immutableState").as[A]
				} yield {
					AtomicObjectState.initial(immutableState)
				}
		}

		val res = json.as[AtomicObjectState[A]]
		res.right.toOption
		//		decodeAtomicObjectState()
	}
}