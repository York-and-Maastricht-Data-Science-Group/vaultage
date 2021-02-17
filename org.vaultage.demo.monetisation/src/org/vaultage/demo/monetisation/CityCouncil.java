package org.vaultage.demo.monetisation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.vaultage.wallet.PaymentInformation;
import org.vaultage.wallet.Wallet;

import com.fasterxml.jackson.databind.JsonNode;

public class CityCouncil extends CityCouncilBase {

	private List<Questionnaire> questionnaires = new ArrayList<Questionnaire>();

	public CityCouncil() throws Exception {
		super();
	}

	public CityCouncil(String address, int port) throws Exception {
		super(address, port);
	}

	public void getQuestionnaire(String requesterPublicKey, String requestToken, String pollId) throws Exception {
		RemoteCityCouncil confirmer = new RemoteCityCouncil(this, requesterPublicKey);
		Questionnaire questionnaire = questionnaires.stream().filter(p -> p.getId().equals(pollId)).findFirst()
				.orElse(null);
		questionnaire = createInstance(questionnaire);
		confirmer.respondToGetQuestionnaire(questionnaire, requestToken);
	}

	private Questionnaire createInstance(Questionnaire questionnaire) {
		Questionnaire instance = new Questionnaire();
		instance.setId(questionnaire.getId());
		instance.setAnswers(new ArrayList<>(questionnaire.getAnswers()));
		instance.setDescription(new String(questionnaire.getDescription()));
		instance.setInstanceId(UUID.randomUUID().toString());
		instance.setQuestions(new ArrayList<>(questionnaire.getQuestions()));
		instance.setTitle(new String(questionnaire.getTitle()));
		instance.setPrice(questionnaire.getPrice());
		return instance;
	}

	public void submitQuestionnaire(String requesterPublicKey, String requestToken, Questionnaire questionnaire,
			PaymentInformation paymentInformation) throws Exception {

		// check if a local payment wallet, that has the same type as the respondent's type, exists
		Wallet wallet = this.getDefaultWallet();
		if (wallet == null || paymentInformation.getType() != wallet.getType()) {

			wallet = this.getWallets().stream(). //
					filter(w -> w.getType().equals(paymentInformation.getType())). //
					findFirst().orElse(null);

			// if the same payment wallet doesn't exist then send an error message
			if (wallet == null) {
				RemoteCityCouncil confirmer = new RemoteCityCouncil(this, requesterPublicKey);
				confirmer.respondToSubmitQuestionnaire("Internal Server Error!", requestToken);
			}
		}
		
		// should have checked the questionnaire first but skipped to keep this code short
		// if the payment information and returned questionnaire are okay, progress with 
		// transferring money to client

		// get the price
		Questionnaire local = this.questionnaires.stream().filter(q -> q.getId().equals(questionnaire.getId())).findFirst().orElse(null);
		double price = (local != null) ? local.getPrice() : 0.0; 
		
		// get access token using the application's clientId and secret
		String accessToken = wallet.getAccessToken(wallet.getClientId(), wallet.getClientSecret());
		
		// do the transfer
		String reference = questionnaire.getInstanceId();
		String message =  "Questionnaire's Title: " + questionnaire.getTitle() + "; " +
				"instance id: " + reference;
		JsonNode node = wallet.transfer(wallet.getAccountNumber(), paymentInformation.getAccountNumber(),
				paymentInformation.getCurrency(), price, accessToken, message, reference);

		// return the result
		RemoteCityCouncil confirmer = new RemoteCityCouncil(this, requesterPublicKey);
		confirmer.respondToSubmitQuestionnaire(node.toString(), requestToken);
	}

	public List<Questionnaire> getQuestionnaires() {
		return questionnaires;
	}

	public void setQuestionnaires(List<Questionnaire> polls) {
		this.questionnaires = polls;
	}

}