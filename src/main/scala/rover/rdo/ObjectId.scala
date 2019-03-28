package rover.rdo

import java.util.UUID

case class ObjectId(asString: String) {
	override def toString: String = {
		return asString
	}

	override def equals(o: Any): Boolean = {
		case o: String => this.asString == o
		case o: ObjectId => this.asString == o.asString

		case _ => false
	}
}

object ObjectId {
	// FIXME: temporary during dev
	val chatAppChat: ObjectId = {
		new ObjectId("chat")
	}

	def from(string: String): ObjectId = {
		return new ObjectId(string)
	}

	def generateNew(): ObjectId = {
		val uuid = UUID.randomUUID()
		return ObjectId(uuid.toString())
	}
}
