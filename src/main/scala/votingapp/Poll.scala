package votingapp

import rover.rdo.AtomicObjectState

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

class Votes(val asMap: Map[PollChoice, Int]) extends AtomicObjectState[Votes] {
	/**
	  * Adds the given poll choice to the votes cast. Also: immutable object pattern.
	  * @param vote The vote-choice to cast
	  * @return New state with the vote added
	  */
	def add(vote: PollChoice): Votes = {
		return applyOp(new CastVoteOp(vote))
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

//trait VoteOp extends AtomicObjectState#Op

// app code
class CastVoteOp(private val vote: PollChoice) extends Votes#Op {
	final override def apply(state: Votes): Votes = {
		if (state.asMap contains vote) {
			Votes(state.asMap updated (vote, state.asMap(vote) + 1))
		}
		else {
			Votes(state.asMap updated (vote, 1))
		}
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