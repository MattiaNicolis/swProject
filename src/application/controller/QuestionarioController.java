package application.controller;

import java.io.IOException;

import application.admin.Amministratore;
import application.admin.MessageUtils;
import application.admin.Sessione;
import application.model.Questionario;
import application.model.Utente;
import application.view.Navigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class QuestionarioController {

	private Utente u;
	
	//TEXTFIELD
	@FXML private TextField nomeFarmacoField;
	@FXML private TextField dosiGiornaliereField;
	@FXML private TextField quantitàField;
	@FXML private TextArea sintomiArea;
	
	int dosiGiornaliere;
	int quantità;
	String sintomi;
	
	@FXML
	private void initialize() {
		u = Sessione.getInstance().getUtente();
	}
	
	public enum QuestResult {
		EMPTY_FIELDS,
		INVALID_DATA,
		SUCCESS,
		FAILURE
	}
	public QuestResult tryCreateQuestionario(String nomeFarmaco, String dosiGiornaliere, String quantità, String sintomi) {
		if(nomeFarmaco == null || nomeFarmaco.isBlank() || 
		dosiGiornaliere == null || dosiGiornaliere.isBlank() || 
		quantità == null || quantità.isBlank()) {
			return QuestResult.EMPTY_FIELDS;
		}

		int dosiGiornaliereInt, quantitàInt;
		try {
			dosiGiornaliereInt = Integer.parseInt(dosiGiornaliereField.getText());
	        quantitàInt = Integer.parseInt(quantitàField.getText());
		} catch (NumberFormatException n) {
			return QuestResult.INVALID_DATA;
		}

		if(dosiGiornaliereInt < 1 || quantitàInt < 1) {
			return QuestResult.INVALID_DATA;
		}

		Questionario quest = new Questionario(u.getCf(), null, nomeFarmaco, dosiGiornaliereInt, quantitàInt, sintomi, false);
		boolean ok = Amministratore.questDAO.creaQuestionario(quest);
		if(ok) {
			return QuestResult.SUCCESS;
		}
		else {
			return QuestResult.FAILURE;
		}
	}
	@FXML
	private void handleQuestionario(ActionEvent event) throws IOException {
		QuestResult result = tryCreateQuestionario(nomeFarmacoField.getText(), dosiGiornaliereField.getText(), quantitàField.getText(), sintomiArea.getText());

		switch(result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Compilare tutti i campi.");
			case INVALID_DATA -> MessageUtils.showError("Compilare i campi correttamente.");
			case FAILURE -> MessageUtils.showError("Errore durante il salvataggio del questionario.");
			case SUCCESS -> {
				MessageUtils.showSuccess("Questionario compilato!");
				Navigator.getInstance().switchToPazientePage(event);
			}
		}
	}
	
	// NAVIGAZIONE
	@FXML
	private void switchToPazientePage(ActionEvent event) throws IOException {
		Navigator.getInstance().switchToPazientePage(event);
	}
}