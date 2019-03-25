package rover.rdo

import java.util.UUID

case class ObjectId(asString: String) {

	override def equals(that: Any): Boolean = {
		that match {
			case that: ObjectId => return this.asString == that.asString
			case _ => return false
		}
	}
}

object ObjectId {
	def generateNew(): ObjectId = {
		val uuid = UUID.randomUUID()
		return ObjectId(uuid.toString())
	}

	def generateFromString(asString: String): ObjectId ={
		return new ObjectId(asString)
	}
}
