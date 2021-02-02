package org.vaultage.demo.monetisation;

public class Respondent extends RespondentBase {

	private Questionnaire retrievedQuestionnaire;

	public Respondent() throws Exception {
		super();
	}

	public Respondent(String address, int port) throws Exception {
		super(address, port);
	}

	public Questionnaire getRetrievedQuestionnaire() {
		return retrievedQuestionnaire;
	}

	public void setRetrievedQuestionnaire(Questionnaire retrievedQuestionnaire) {
		this.retrievedQuestionnaire = retrievedQuestionnaire;
	}

	public void getQuestionnaireFromCouncil(String pollId, CityCouncil council) throws Exception {
		RemoteCityCouncil requester = new RemoteCityCouncil(this, council.getPublicKey());
		requester.getQuestionnaire(pollId);
	}

	public void submitQuestionnaire(Questionnaire poll, CityCouncil council) throws Exception {
		RemoteCityCouncil requester = new RemoteCityCouncil(this, council.getPublicKey());
		requester.submitQuestionnaire(poll);
	}

}