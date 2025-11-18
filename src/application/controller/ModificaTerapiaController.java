package application.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import application.admin.Amministratore;
import application.admin.MessageUtils;
import application.admin.Sessione;
import application.controller.NuovaTerapiaController.TerapiaResult;
import application.model.Terapia;
import application.model.Utente;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ModificaTerapiaController {
	
	private Utente u;
	private Utente p;
	private Terapia t;
	
	// FIELD
	@FXML private TextField farmacoField;
	@FXML private TextField dosiGiornaliereField;
	@FXML private TextField quantitàField;
	@FXML private DatePicker dataInizioField;
	@FXML private DatePicker dataFineField;
	@FXML private TextArea indicazioniField;
	
	// LABEL
	@FXML private Label labelPaziente;
	@FXML private Label nomeFarmacoLabel;

	// VARIABILI
	int dosiGiornaliere;
	int quantità;
	LocalDate dataInizio;
	LocalDate dataFine;
	StringBuilder msg;
	
	@FXML
	private void initialize() {
		u = Sessione.getInstance().getUtente();
		p = Sessione.getInstance().getPazienteSelezionato();
		t = Sessione.getInstance().getTerapiaSelezionata();
		
		labelPaziente.setText(p.getNomeCognome() + " (" + p.getCf() + ")");
		nomeFarmacoLabel.setText(t.getNomeFarmaco());
	}

	/*public enum ModificaTerapiaResult {
		SUCCESS,
		FAILURE,
		INVALID_DATA,
		INVALID_DATE_RANGE,
		EMPTY_FIELDS,
	}*/

	public TerapiaResult tryModificaTerapia(String dosiGiornaliere, String quantità, LocalDate dataInizio, LocalDate dataFine, String indicazioni) {
		if(dataInizio == null || dataFine == null) {
			return TerapiaResult.EMPTY_FIELDS;
		}

		try {
	        this.dosiGiornaliere = Integer.parseInt(dosiGiornaliere);
	        this.quantità = Integer.parseInt(quantità);
	    } catch (NumberFormatException n) {
	    	return TerapiaResult.INVALID_DATA;
	    }

		if(dataInizio.isBefore(LocalDate.now()) ||
				dataFine.isBefore(dataInizio) ||
				dataFine.isEqual(dataInizio) ||
				dataFine.isBefore(LocalDate.now()) ||
				this.dosiGiornaliere < 1 || this.quantità < 1) {
			return TerapiaResult.INVALID_DATA;
		}
		
		// LISTA TERAPIA IN CONFLITTO
		List<Terapia> conflitti = Amministratore.getTerapieByCF(p.getCf()).stream()
			.filter(terapia -> !terapia.getNomeFarmaco().equals(t.getNomeFarmaco())
					&& !terapia.getDataInizio().equals(dataInizioField.getValue()))
			.filter(terapia -> {
					LocalDate inizio = terapia.getDataInizio();
					LocalDate fine = terapia.getDataFine();
					
					return (dataInizioField.getValue().isAfter(inizio) && dataInizioField.getValue().isBefore(fine) || 
							dataInizioField.getValue().isEqual(fine) || dataInizioField.getValue().isEqual(inizio)) || 
						   (dataFineField.getValue().isBefore(fine) && dataFineField.getValue().isAfter(inizio) || 
								   dataFineField.getValue().isEqual(fine) || dataFineField.getValue().isEqual(inizio)) ||
						   (dataInizioField.getValue().isBefore(inizio) && dataFineField.getValue().isAfter(fine));
			})
			.collect(Collectors.toList());
		
		if(!conflitti.isEmpty()) {
			msg = new StringBuilder("Terapie in conflitto:\n");
			conflitti.forEach(terapia ->
					msg.append("- ").append(terapia.getNomeFarmaco()).append(": ")
					   .append(terapia.getDataInizio()).append(" -> ")
					   .append(terapia.getDataFine()).append("\n")
			);
			return TerapiaResult.INVALID_DATE_RANGE;
		}
		
		// Modifica della terapia nel database
		Terapia terapia = new Terapia(t.getId(), t.getCf(), null, this.dosiGiornaliere, this.quantità, dataInizio, dataFine, indicazioniField.getText(), u.getCf(), false);
		boolean ok = Amministratore.terapiaDAO.modificaTerapia(terapia);

		if(ok) {
			return TerapiaResult.SUCCESS;
		} else {
			return TerapiaResult.FAILURE;
		}
	}
	
	@FXML
	private void handleModificaTerapia(ActionEvent event) throws IOException {
		TerapiaResult result = tryModificaTerapia(dosiGiornaliereField.getText(), quantitàField.getText(), dataInizioField.getValue(), dataFineField.getValue(), indicazioniField.getText());

		switch(result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Per favore, compila tutti i campi.");
			case INVALID_DATA -> MessageUtils.showError("Dati non validi. Controlla le date e i numeri inseriti.");
			case INVALID_DATE_RANGE -> MessageUtils.showError(msg.toString());
			case FAILURE -> MessageUtils.showError("Errore durante la creazione della terapia.");
			case SUCCESS -> {
				MessageUtils.showSuccess("Terapia modificata con successo.");
				Navigator.getInstance().switchToMostraDatiPaziente(event);
			}
		}
	}
	
	@FXML
	private void switchToMostraDatiPaziente(ActionEvent event) throws IOException {
		Sessione.getInstance().nullTerapiaSelezionata();
		Navigator.getInstance().switchToMostraDatiPaziente(event);
	}
}