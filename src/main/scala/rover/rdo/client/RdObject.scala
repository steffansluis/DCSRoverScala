package rover.rdo.client

trait RdObject {
	def version: Long
}

class CommonAncestor(one: RdObject, other: RdObject) extends RdObject {

	// determine it once and defer all RdObject methods to it
	private val commonAncestor: RdObject = {
		// determine here... & probably cache or is that not needed in scala? :S
		null
	}

	override def version: Long = {
		commonAncestor.version
	}
}