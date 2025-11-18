package application.controller;

import java.io.IOException;
import java.util.Optional;

import application.admin.Amministratore;
import application.admin.MessageUtils;
import application.admin.Sessione;
import application.model.Terapia;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

public class MostraDettagliTerapiaController {
	
	private Terapia t;
	
	// LABEL
	@FXML private Label nomeFarmacoLabel;
	@FXML private Label dosiGiornaliereLabel;
	@FXML private Label quantitàLabel;
	@FXML private Label dataInizioLabel;
	@FXML private Label dataFineLabel;
	@FXML private Label indicazioniLabel;
	@FXML private Label modificatoDaLabel;
	
	
	@FXML
	private void initialize() {
		t = Sessione.getInstance().getTerapiaSelezionata();
		
		nomeFarmacoLabel.setText(t.getNomeFarmaco());
		dosiGiornaliereLabel.setText(String.valueOf(t.getDosiGiornaliere()));
		quantitàLabel.setText(String.valueOf(t.getQuantità()));
		dataInizioLabel.setText(t.getDataInizio().format(Amministratore.dateFormatter));
		dataFineLabel.setText(t.getDataFine().format(Amministratore.dateFormatter));
		
		if(t.getIndicazioni() != null && !t.getIndicazioni().isBlank())
			indicazioniLabel.setText(t.getIndicazioni());
		else
			indicazioniLabel.setText("Nessuna indicazione.");
		
		modificatoDaLabel.setText(t.getDiabetologo());
	}
	
	@FXML
	private void switchToMostraDatiPaziente(ActionEvent event) throws IOException {
		Sessione.getInstance().nullTerapiaSelezionata();
		Navigator.getInstance().switchToMostraDatiPaziente(event);
	}
	
	@FXML
	private void switchToModificaTerapia(ActionEvent event) throws IOException {
		Navigator.getInstance().switchToModificaTerapia(event);
	}

	@FXML
	private void eliminaTerapia(ActionEvent event) throws IOException {
		Optional<ButtonType> result = MessageUtils.showConferma("Eliminazione terapia", "Sei sicuro di voler eliminare questa terapia?");
		if (result.isPresent() && result.get() == ButtonType.OK) {
			boolean ok = Amministratore.terapiaDAO.eliminaTerapia(t);

			if (ok) {
			Amministratore.terapie.removeIf(terapia -> terapia.getId() == t.getId());
			Sessione.getInstance().nullTerapiaSelezionata();
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