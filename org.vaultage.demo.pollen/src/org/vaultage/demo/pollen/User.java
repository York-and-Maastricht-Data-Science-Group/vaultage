package org.vaultage.demo.pollen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.vaultage.demo.pollen.cli.PollenCLI;

public class User extends UserBase {
	private String name = new String();
	// token to poll map
	private Map<String, NumberPoll> polls = new HashMap<>();
	
	// poll id to value maps
	private Map<String, Double> pollFakeValues = new HashMap<>();
	private Map<String, Double> pollRealValues = new HashMap<>();
	private Map<String, Double> numberPollResults = new HashMap<>();
	
	public long waitTime = 0;

	public User() throws Exception {
		super();
	}

	public double getPollFakeValue(String pollId) {
		return pollFakeValues.get(pollId);
	}
	
	public double getPollRealValue(String pollId) {
		return pollRealValues.get(pollId);
	}
	
	public void addPollFakeValue(String pollId, double value) {
		pollFakeValues.put(pollId, value);
	}
	
	public void addPollRealValue(String pollId, double value) {
		pollRealValues.put(pollId, value);
	}

	// getter
	public String getName() {
		return this.name;
	}

	// setter
	public void setName(String name) {
		this.name = name;
	}

	// operations
	public double sendNumberPoll(String requesterPublicKey, NumberPoll poll) throws Exception {
		return 1.0;
	}

	public List<Integer> sendMultivaluedPoll(String requesterPublicKey, MultivaluedPoll poll) throws Exception {
		throw new Exception();
	}

	public Map<String, NumberPoll> getPolls() {
		return polls;
	}

	public void setPolls(Map<String, NumberPoll> polls) {
		this.polls = polls;
	}

	public double sendNumberPoll(String from, NumberPoll poll, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setNumberPollResult(String pollId, double result) {
		numberPollResults.put(pollId, result);
	}

	public double getNumberPollResult(String pollId) {
		if (numberPollResults.containsKey(pollId)) {
			return numberPollResults.get(pollId);
		}
		return -288.0;
	}

	public double sendNumberPoll(User requester, NumberPoll poll) {
		Scanner s = new Scanner(System.in);
		PollenCLI.displayPoll(poll);
		return PollenCLI.readValue(s);
	}

}
