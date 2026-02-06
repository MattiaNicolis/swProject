package application.controller;

import java.io.IOException;
import java.util.Optional;

import application.model.Diabetologo;
import application.model.Paziente;
import application.model.Terapia;
import application.model.Utente;
import application.service.AdminService;
import application.utils.MessageUtils;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

public class MostraDettagliTerapiaController {
	
	// VARIABILI
	private Utente u;
	private Terapia t;

	// LABEL
	@FXML private Label nomeFarmacoLabel;
	@FXML private Label dosiGiornaliereLabel;
	@FXML private Label quantitàLabel;
	@FXML private Label dataInizioLabel;
	@FXML private Label dataFineLabel;
	@FXML private Label indicazioniLabel;
	@FXML private Label modificatoDaLabel;

	// BUTTON
	@FXML private Button deleteButton;
	@FXML private Button modifyButton;
	
	@FXML
	private void initialize() {
		// Recupero utente e terapia dalla sessione
		if(Sessione.getInstance().getDiabetologo() != null) {
			u = Sessione.getInstance().getDiabetologo();
		} else if (Sessione.getInstance().getPaziente() != null) {
			u = Sessione.getInstance().getPaziente();
		}
		if (u == null) {
            MessageUtils.showError("Errore di sessione: Utente non trovato.");
            return;
        }
		t = Sessione.getInstance().getTerapiaSelezionata();
		
		// Disabilito i bottoni di modifica ed eliminazione se l'utente è un paziente
		if(u instanceof Paziente) {
			deleteButton.setDisable(true);
			modifyButton.setDisable(true);
		}

		// Disabilito il bottone di eliminazione se la terapia ha questionari associati
		if(t.getQuestionari() > 0)
			deleteButton.setDisable(true);

		setUpInterfaccia();
	}
	
	private void setUpInterfaccia() {
		nomeFarmacoLabel.setText(t.getNomeFarmaco());
		dosiGiornaliereLabel.setText(String.valueOf(t.getDosiGiornaliere()));
		quantitàLabel.setText(String.valueOf(t.getQuantità()));
		dataInizioLabel.setText(t.getDataInizio().format(AdminService.dateFormatter));
		dataFineLabel.setText(t.getDataFine().format(AdminService.dateFormatter));
		
		if(t.getIndicazioni() != null && !t.getIndicazioni().isBlank())
			indicazioniLabel.setText(t.getIndicazioni());
		else
			indicazioniLabel.setText("Nessuna indicazione.");
		
		modificatoDaLabel.setText(AdminService.getNomeDiabetologoByCf(t.getDiabetologo()) + " (" + t.getDiabetologo() + ")");
	}

	// NAVIGAZIONE
	@FXML
	private void indietro(ActionEvent event) throws IOException {
		Sessione.getInstance().setTerapiaSelezionata(null);
		
		if (u instanceof Diabetologo) {
			Navigator.getInstance().switchToMostraDatiPaziente(event);
        } else if (u instanceof Paziente) {
			Navigator.getInstance().switchToPazientePage(event);
        }
	}
	
	@FXML
	private void switchToModificaTerapia(ActionEvent event) throws IOException {
		Navigator.getInstance().switchToTerapia(event);
	}

	@FXML
	private void eliminaTerapia(ActionEvent event) throws IOException {
		Optional<ButtonType> result = MessageUtils.showConferma("Eliminazione terapia", "Sei sicuro di voler eliminare questa terapia?");
		if (result.isPresent() && result.get() == ButtonType.OK) {
			boolean ok = AdminService.eliminaTerapia(t);

			if (ok) {
				Sessione.getInstance().setTerapiaSelezionata(null);
				Navigator.getInstance().switchToMostraDatiPaziente(event);
			}
			else{
				MessageUtils.showError("Si è verificato un errore durante l'eliminazione della terapia.");
			}
		}
		else {
			MessageUtils.showError("Eliminazione della terapia annullata.");
		}
	}
}