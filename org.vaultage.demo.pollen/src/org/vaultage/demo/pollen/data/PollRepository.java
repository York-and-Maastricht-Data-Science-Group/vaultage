package org.vaultage.demo.pollen.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.vaultage.demo.pollen.gen.MultivaluedPoll;
import org.vaultage.demo.pollen.gen.NumberPoll;

public class PollRepository {

	public static NumberPoll createSalaryPoll() {
		NumberPoll poll = new NumberPoll();
		poll.setQuestion("What is your current gross salary?");
		poll.setAccumValue(ThreadLocalRandom.current().nextDouble(0, 1000000.0));
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

		List<Integer> optionValues = new ArrayList<>();
		for (int i = 0; i < poll.getOptions().size(); i++) {
			optionValues.add(ThreadLocalRandom.current().nextInt(0, 1000000));
		}
		poll.setAccumOptionValues(optionValues);

		return poll;
	}
}
