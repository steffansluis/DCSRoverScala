package rover.rdo

import java.util.UUID

case class ObjectId(asString: String) {
}

object ObjectId {
	def generateNew(): ObjectId = {
		val uuid = UUID.randomUUID()
		return ObjectId(uuid.toString())
	}
}
