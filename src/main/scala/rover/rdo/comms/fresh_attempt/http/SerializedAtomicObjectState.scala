package rover.rdo.comms.fresh_attempt.http

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import rover.rdo.state.AtomicObjectState

class SerializedAtomicObjectState[A <: Serializable] (state: AtomicObjectState[A]) {

	private lazy val asByteArrayOutputStream: ByteArrayOutputStream = {
		val byteOutputStream = new ByteArrayOutputStream()

		// write object into byte output stream
		val objectOutputStream = new ObjectOutputStream(byteOutputStream)
		objectOutputStream.writeObject(state)
		objectOutputStream.close()

		// return
		byteOutputStream
	}

	lazy val asBytes: Array[Byte] = {
		// return
		asByteArrayOutputStream.toByteArray
	}

	lazy val asString: String = {
		//return
		asByteArrayOutputStream.toString
	}
}

class DeserializedAtomicObjectState[A <: Serializable] (serializedState: Array[Byte]) {

	lazy val asAtomicObjectState: AtomicObjectState[A] = {
		val bytesInputStream = new ByteArrayInputStream(serializedState)
		val objectInputStream = new ObjectInputStream(bytesInputStream)

		val state = objectInputStream.readObject().asInstanceOf[AtomicObjectState[A]]
		objectInputStream.close()

		// return
		state
	}
}
