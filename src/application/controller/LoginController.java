package application.controller;

import java.io.IOException;

import application.model.Utente;
import application.service.AdminService;
import application.utils.MessageUtils;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

	// --- FIELD ---
	@FXML private TextField cfField;
	@FXML private PasswordField passwordField;

	// --- LABEL ---
	@FXML private Label firstLabel;
	
	@FXML
	private void initialize() {
		firstLabel.setFocusTraversable(true);
	}
	
	private enum LoginResult {
		SUCCESS_DIABETOLOGO,
		SUCCESS_PAZIENTE,
		WRONG_CREDENTIALS,
		EMPTY_FIELDS
	}

	private LoginResult tryLogin(String cf, String password) {
		if(cf == null || cf.isBlank() || password == null || password.isBlank())
			return LoginResult.EMPTY_FIELDS;

		Utente utente = AdminService.login(cf, password);

		if(utente != null) {
			Sessione.getInstance().setUtente(utente);

			if(utente.isDiabetologo()) return LoginResult.SUCCESS_DIABETOLOGO;
			else if(utente.isPaziente()) return LoginResult.SUCCESS_PAZIENTE;
		}

		return LoginResult.WRONG_CREDENTIALS;
	}

	@FXML 
	private void handleLogin(ActionEvent event) throws IOException {
		LoginResult result = tryLogin(cfField.getText(), passwordField.getText());

		switch(result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Inserire codice fiscale e password.");
			case WRONG_CREDENTIALS -> MessageUtils.showError("Codice fiscale o password errati.");
			case SUCCESS_DIABETOLOGO -> Navigator.getInstance().switchToDiabetologoPage(event);
			case SUCCESS_PAZIENTE -> Navigator.getInstance().switchToPazientePage(event);
		}
	}
}