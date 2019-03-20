package rover.rdo

import java.util

import com.google.gson.Gson
import org.apache.http.message.BasicNameValuePair
import rover.rdo.client.{LogRecord, StateLog}

trait AtomicObjectState[A] extends ObjectState {
	type Op = A => A

	def immutableState: A

	protected[rdo] def log: StateLog[A]
	def applyOp(operation: Op): AtomicObjectState[A]

	def serializeSelf: util.ArrayList[BasicNameValuePair]

	override def equals(obj: Any): Boolean
}

// TODO: make ctor private
class BasicAtomicObjectState[A](val immutableState: A, protected[rdo] val log: StateLog[A]) extends AtomicObjectState[A] {

	def applyOp(operation: Op): AtomicObjectState[A] = {
		// Operation must apply itself to the state
		// but we want the state to take in the operations
		// so that the framework can record the op
		val result = operation.apply(this.immutableState)

		// Record the operation in the Log
		val updatedLog = log.appended(LogRecord(immutableState, operation, result))

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

	override def serializeSelf: util.ArrayList[BasicNameValuePair] = {
		val logAsJson = new Gson().toJson(this.log)
		val logNameValuePair = new util.ArrayList[BasicNameValuePair]()
		logNameValuePair.add(new BasicNameValuePair("JSON", logAsJson))
		return logNameValuePair
	}
}

object AtomicObjectState {
	def initial[A](value: A): AtomicObjectState[A] = {
		return new BasicAtomicObjectState[A](value, StateLog.withInitialState(value))
	}

	def fromLog[A](log: StateLog[A]): AtomicObjectState[A] = {
		return new BasicAtomicObjectState[A](log.asList.last.stateResult, log)
	}
}