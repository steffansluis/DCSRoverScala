	package votingapp

class Poll(val question: String, val choices: List[PollChoice]) {
	var votes: Votes = Votes(choices)

	def cast(vote: PollChoice): Unit = {
		println(s"Casting vote: $vote")
	}

	def result: PollResult = {
		return new PollResult(votes)
	}
}

class PollResult(private  val votes: Votes) {
	def winner: PollChoice = {
		votes.majorityChoice
	}
}

case class PollChoice(choice: String)

class Votes(val asMap: Map[PollChoice, Int]) {
	def add(vote: PollChoice): Votes = {
		if (this.asMap contains vote) {
			Votes(this.asMap updated (vote, this.asMap(vote) + 1))
		}
		else {
			Votes(this.asMap updated (vote, 1))
		}
	}

	def majorityChoice: PollChoice = {
		// FIXME: ties, idea: return a "poll-result" not the choice
		val winner = this.asMap.maxBy(_._2)._1
		return winner
	}

	override def toString: String = {
		asMap.toString
	}
}

object Votes {
	def apply(choices: List[PollChoice]) : Votes = {
		// TODO: maybe remove, not needed anymore... we accept any choice? Or enforce only valid choices
		return new Votes(choices.map(choice => (choice,0)).toMap)
	}

	def apply(votes: Map[PollChoice, Int]): Votes = {
		return new Votes(votes)
	}
}

object henk {
	def main(args: Array[String]): Unit = {
		var poll = new Poll("Does this work", List(PollChoice("Yes"), PollChoice("No"), PollChoice("I hope so"), PollChoice("Yes")))
		println(poll.votes)

		poll.cast(PollChoice("Yes"))
		poll.cast(PollChoice("No"))
		println(poll.votes)
		println(poll.result.winner)

	}
}