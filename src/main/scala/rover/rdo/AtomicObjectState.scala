package rover.rdo

import rover.rdo.client.{Log, LogRecord}

import io.circe._, io.circe.syntax._
import io.circe.{Encoder, Decoder, Json}


// TODO: make ctor private
class AtomicObjectState[A](private val value: A, private[rdo] val log: Log[A])(implicit val encodeA: Encoder[A], implicit val decodeA: Decoder[A])  extends ObjectState {
	type Op = A => A

	def encoded: Json = encodeA(value)
//	def decoded: A = decodeA()

	def immutableState: A = value

	def applyOp(operation: Op): AtomicObjectState[A] = {
		// Operation must apply itself to the state
		// but we want the state to take in the operations
		// so that the framework can record the op
		val result = operation.apply(this.value)

		// Record the operation in the Log
		val updatedLog = log.appended(LogRecord(value, operation, result))

		return new AtomicObjectState[A](result, updatedLog)
	}

	override def equals(that: Any): Boolean = {
		that match{
			case that: AtomicObjectState[A] => this.immutableState == that.immutableState
			case _ => false
		}
	}

	def getLog: Log[A] = {
		return this.log
	}

	override def toString: String = {
		value.toString
	}

	implicit val decodeAtomicObjectState: Decoder[AtomicObjectState[A]] = new Decoder[AtomicObjectState[A]] {
		final def apply(c: HCursor): Decoder.Result[AtomicObjectState[A]] =
			for {
				immutableState <- c.downField("immutableState").as[A]
			} yield {
				AtomicObjectState.initial(immutableState)
			}
	}
//	implicit val decodeAtomicObjectState: Decoder[AtomicObjectState[A]] = new Decoder[AtomicObjectState[A]] {
//		final def apply(c: HCursor)(implicit encodeA: Encoder[A], decodeA: Decoder[A]): Decoder.Result[AtomicObjectState[A]] =
//			for {
//				immutableState <- c.downField("immutableState").as[A]
//			} yield {
//				AtomicObjectState.initial(immutableState)
//			}
//	}
}

object AtomicObjectState {
	def initial[A](value: A)(implicit encodeA: Encoder[A], decodeA: Decoder[A]) : AtomicObjectState[A] = {
		return new AtomicObjectState[A](value, Log.withInitialState(value))
	}

//	def initial[A](value: List[A]): AtomicObjectState[A] = {
//		return new AtomicObjectState[A](value.last, Log.withInitialStates(value.map(v => LogRecord[A](v, null, v))))
//	}

	def fromLog[A](log: Log[A])(implicit encodeA: Encoder[A], decodeA: Decoder[A]): AtomicObjectState[A] = {
		return new AtomicObjectState[A](log.asList.last.stateResult, log)
	}

//	implicit val encodeAtomicObjectState: Encoder[AtomicObjectState[_]] = new Encoder[AtomicObjectState[_]] {
//		final def apply(s: AtomicObjectState[_]): Json = Json.obj(
//			("", s.asJson)
//		)
//	}

//	implicit val decodeAtomicObjectState: Decoder[AtomicObjectState[_]] = new Decoder[AtomicObjectState[_]] {
//		final def apply[A](c: HCursor)(implicit encodeA: Encoder[A], decodeA: Decoder[A]): Decoder.Result[AtomicObjectState[A]] =
//			for {
//				immutableState <- c.downField("immutableState").as[A]
//			} yield {
//				AtomicObjectState.initial(immutableState)
//			}
//	}

}