package application.controller;

import java.io.IOException;
import java.time.LocalDate;

import application.admin.Amministratore;
import application.admin.MessageUtils;
import application.admin.Sessione;
import application.model.Terapia;
import application.model.Utente;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class NuovaTerapiaController {
	
	private Utente u;
	private Utente p;
	
	// FIELD
	@FXML private TextField farmacoField;
	@FXML private TextField dosiGiornaliereField;
	@FXML private TextField quantitàField;
	@FXML private DatePicker dataInizioField;
	@FXML private DatePicker dataFineField;
	@FXML private TextArea indicazioniField;
	
	// LABEL
	@FXML private Label labelPaziente;
	
	// VARIABILI
	private int dosiGiornaliere;
	private int quantità;
	private LocalDate fine;
	
	@FXML
	private void initialize() {
		u = Sessione.getInstance().getUtente();
		p = Sessione.getInstance().getPazienteSelezionato();
		
		labelPaziente.setText(p.getNomeCognome() + " (" + p.getCf() + ")");

		fine = Amministratore.getTerapieByCF(p.getCf()).stream()
			.map(Terapia::getDataFine)
			.max(LocalDate::compareTo)
			.orElse(null);
	}

	public enum TerapiaResult {
		SUCCESS,
		FAILURE,
		INVALID_DATA,
		INVALID_DATE_RANGE,
		EMPTY_FIELDS,
	}

	public TerapiaResult tryCreateTerapia(String nomeFarmaco, String dosiGiornaliere, String quantità, LocalDate dataInizio, LocalDate dataFine, String indicazioni) {
		if(nomeFarmaco == null || nomeFarmaco.isBlank() ||
		   dataInizio == null || dataFine == null ||
		   dosiGiornaliere == null || dosiGiornaliere.isBlank() ||
		   quantità == null || quantità.isBlank()){
			return TerapiaResult.EMPTY_FIELDS;
		}

		try{
			this.dosiGiornaliere = Integer.parseInt(dosiGiornaliere);
			this.quantità = Integer.parseInt(quantità);
		} catch (NumberFormatException n) {
			return TerapiaResult.INVALID_DATA;
		}

		if(dataInizio.isBefore(LocalDate.now()) ||
				!dataFine.isAfter(dataInizio) ||
				this.dosiGiornaliere < 1 || this.quantità < 1) {
			return TerapiaResult.INVALID_DATA;
		}
		else if(fine != null) {
			if(dataInizio.isBefore(fine) || dataInizio.isEqual(fine)) {
				return TerapiaResult.INVALID_DATE_RANGE;
			}
	    }
		
		// Creazione della terapia nel database
		Terapia terapia = new Terapia(0, p.getCf(), nomeFarmaco, this.dosiGiornaliere, this.quantità, dataInizio, dataFine, indicazioni, u.getCf(), false);
		boolean ok = Amministratore.terapiaDAO.creaTerapia(terapia);

		if(ok) {
			return TerapiaResult.SUCCESS;
		} else {
			return TerapiaResult.FAILURE;
		}
	}
	
	@FXML
	private void handleTerapia(ActionEvent event) throws IOException {
		TerapiaResult result = tryCreateTerapia(farmacoField.getText(), dosiGiornaliereField.getText(), quantitàField.getText(), dataInizioField.getValue(), dataFineField.getValue(), indicazioniField.getText());

		switch(result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Per favore, compila tutti i campi.");
			case INVALID_DATA -> MessageUtils.showError("Dati non validi. Controlla le date e i numeri inseriti.");
			case INVALID_DATE_RANGE -> MessageUtils.showError("La data di inizio deve essere successiva alla fine dell'ultima terapia.\nL'ultima terapia finisce: " + fine);
			case FAILURE -> MessageUtils.showError("Errore durante la creazione della terapia.");
			case SUCCESS -> {
				MessageUtils.showSuccess("Terapia creata con successo.");
				Navigator.getInstance().switchToMostraDatiPaziente(event);
			}
		} 
	}
	
	@FXML
	private void switchToMostraDatiPaziente(ActionEvent event) throws IOException {
		Navigator.getInstance().switchToMostraDatiPaziente(event);
	}
}