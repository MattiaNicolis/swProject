package application.controller;

import java.io.IOException;

import application.admin.Amministratore;
import application.admin.MessageUtils;
import application.admin.Sessione;
import application.model.Utente;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

	@FXML public TextField cfField;
	@FXML public PasswordField passwordField;
	@FXML private Label firstLabel;
	
	@FXML
	private void initialize() {
		firstLabel.setFocusTraversable(true);
	}
	
	public enum LoginResult {
		SUCCESS_DIABETOLOGO,
		SUCCESS_PAZIENTE,
		USER_NOT_FOUND,
		WRONG_CREDENTIALS,
		EMPTY_FIELDS
	}

	public LoginResult tryLogin(String cf, String password) {
		if(cf == null || cf.isBlank() || password == null || password.isBlank())
			return LoginResult.EMPTY_FIELDS;

		if(!Amministratore.utenteEsiste(cf))
			return LoginResult.USER_NOT_FOUND;

		Utente utente = Amministratore.getUtenteByCf(cf);

		if(utente.checkPw(password)) {
			Sessione.getInstance().setUtente(utente);

			if(utente.getRuolo().equals("diabetologo")) return LoginResult.SUCCESS_DIABETOLOGO;
			else if(utente.getRuolo().equals("paziente")) return LoginResult.SUCCESS_PAZIENTE;
		}

		return LoginResult.WRONG_CREDENTIALS;
	}


	@FXML 
	private void handleLogin(ActionEvent event) throws IOException {
		
		LoginResult result = tryLogin(cfField.getText(), passwordField.getText());

		switch(result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Inserire codice fiscale e password.");
			case USER_NOT_FOUND -> MessageUtils.showError("Utente non esistente.");
			case WRONG_CREDENTIALS -> MessageUtils.showError("Codice fiscale o password errati.");
			case SUCCESS_DIABETOLOGO -> Navigator.getInstance().switchToDiabetologoPage(event);
			case SUCCESS_PAZIENTE -> Navigator.getInstance().switchToPazientePage(event);
		}
	}
}