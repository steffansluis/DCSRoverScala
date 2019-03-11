package rover.rdo.client

//import java.security.MessageDigest

trait RdObject {
	//FIXME: use hashes instead of Longs/Strings
	def version: Long
	def stableVersion: Long
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

	override def stableVersion: Long = {
		commonAncestor.stableVersion
	}

  def hasDiverged(other: RdObject): Long =  {
		if (this.version != other.version){
			commonAncestor.version
		}
		else{
			println("Non-divergent objects")
			this.version
		}
	}
}

class updateRDO(){
	//TODO: apply tentative updates to RDO

}

class revertRDO(){
	//TOD: also revert changes
}