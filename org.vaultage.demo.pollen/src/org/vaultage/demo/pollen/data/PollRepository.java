package org.vaultage.demo.pollen.data;

import java.util.ArrayList;
import java.util.List;

import org.vaultage.demo.pollen.gen.MultivaluedPoll;
import org.vaultage.demo.pollen.gen.NumberPoll;

public class PollRepository {

	public static NumberPoll createSalaryPoll() {
		NumberPoll poll = new NumberPoll();
		poll.setQuestion("What is your current gross salary?");
		return poll;
	}

	public static MultivaluedPoll createElectionsPoll() {
		MultivaluedPoll poll = new MultivaluedPoll();
		poll.setQuestion("Who is going to win next USA elections?");

		List<String> options = new ArrayList<>();
		options.add("Democrats");
		options.add("Republicans");
		options.add("Green Party");
		poll.setOptions(options);

		return poll;
	}
}
