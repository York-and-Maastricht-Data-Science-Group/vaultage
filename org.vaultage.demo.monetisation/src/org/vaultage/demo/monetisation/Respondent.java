package org.vaultage.demo.monetisation;

public class Respondent extends RespondentBase {

	private Poll retrievedPoll;
	
	public Respondent() throws Exception {
		super();
	}

	public Respondent(String address, int port) throws Exception {
		super(address, port);
	}

	public Poll getRetrievedPoll() {
		return retrievedPoll;
	}

	public void setRetrievedPoll(Poll retrievedPoll) {
		this.retrievedPoll = retrievedPoll;
	}
	
	public void getPollFromCouncil(String pollId, CityCouncil council) throws Exception {
		RemoteCityCouncil requester = new RemoteCityCouncil(this, council.getPublicKey());
		requester.getPoll(pollId);
	}

	public void submitPoll(Poll poll, CityCouncil council) throws Exception {
		RemoteCityCouncil requester = new RemoteCityCouncil(this, council.getPublicKey());
		requester.submitPoll(poll);	
	}

}