package application.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import application.model.Terapia;
import application.model.Utente;
import application.service.AdminService;
import application.utils.MessageUtils;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class TerapiaController {
	
	// VARIABILI
	private Utente u;
	private Utente p;
	private Terapia t;
	private Terapia terapia; // oggetto creato per l'inserimento nel database
	private List<Terapia> terapie = new ArrayList<>();
	private StringBuilder msg;
	
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
	@FXML private Label title;
	@FXML private Label label1;

	// BUTTON
	@FXML private Button conferma;
	
	@FXML
	private void initialize() {
		u = Sessione.getInstance().getUtente();
		p = Sessione.getInstance().getPazienteSelezionato();
		
		t = Sessione.getInstance().getTerapiaSelezionata(); // se sto creando una nuova terapia sarà null, se la sto modificando non sarà null

		terapie = AdminService.loadTerapieByPaziente(p);

		labelPaziente.setText(p.getNomeCognome() + " (" + p.getCf() + ")");
		labelPaziente.setFocusTraversable(true);
		nomeFarmacoLabel.setVisible(false);
		if(t != null) {
			title.setText("Modifica Terapia");
			label1.setText("Aggiorna dettagli terapia");
			farmacoField.setVisible(false);
			nomeFarmacoLabel.setVisible(true);
			nomeFarmacoLabel.setText(t.getNomeFarmaco());
			conferma.setText("Salva Modifiche");

			dosiGiornaliereField.setText(String.valueOf(t.getDosiGiornaliere()));
			quantitàField.setText(String.valueOf(t.getQuantità()));
			dataInizioField.setValue(t.getDataInizio());
			dataFineField.setValue(t.getDataFine());
			if (t.getIndicazioni() != null) {
				indicazioniField.setText(t.getIndicazioni());
			}
		}
	}

	private enum TerapiaResult {
		SUCCESS,
		FAILURE,
		INVALID_DATA,
		INVALID_DATE_RANGE,
		EMPTY_FIELDS,
	}

	private TerapiaResult manageTerapia(String nomeFarmaco, String dosiGiornaliere, String quantità, LocalDate dataInizio, LocalDate dataFine, String indicazioni) {
		if(t == null) {
			if(nomeFarmaco == null || nomeFarmaco.isBlank())
				return TerapiaResult.EMPTY_FIELDS;
		}
		
		if(dataInizio == null || dataFine == null ||
		   dosiGiornaliere == null || dosiGiornaliere.isBlank() ||
		   quantità == null || quantità.isBlank()){
			return TerapiaResult.EMPTY_FIELDS;
		}

		int dosiGiornaliereInt;
		int quantitàInt;
		try{
			dosiGiornaliereInt = Integer.parseInt(dosiGiornaliere);
			quantitàInt = Integer.parseInt(quantità);
		} catch (NumberFormatException n) {
			return TerapiaResult.INVALID_DATA;
		}

		if(dataInizio.isBefore(LocalDate.now()) ||
				dataFine.isBefore(dataInizio) ||
				dosiGiornaliereInt < 1 || quantitàInt < 1) {
			return TerapiaResult.INVALID_DATA;
		}

		if(t != null) {
			if(dataInizio.isBefore(LocalDate.now())) {
				return TerapiaResult.INVALID_DATA;
			}
		}
		
		List<Terapia> conflitti = new ArrayList<>();
		if(t != null) {
			conflitti = terapie.stream()
				.filter(terapia -> terapia.getId() != t.getId()) // esclusione della terapia che sto modificando
				.filter(terapia -> terapia.getNomeFarmaco().equalsIgnoreCase(t.getNomeFarmaco())) // filtro sul farmaco
				.filter(terapia -> { // filtro sulla sovrapposizione delle date
					LocalDate esistenteInizio = terapia.getDataInizio();
					LocalDate esistenteFine = terapia.getDataFine();

					boolean startOverlap = !dataInizio.isAfter(esistenteFine);
					boolean endOverlap = !dataFine.isBefore(esistenteInizio);

					return startOverlap && endOverlap;
				})
				.collect(Collectors.toList());
		} 
		else {
			conflitti = terapie.stream()
				.filter(terapia -> terapia.getNomeFarmaco().equalsIgnoreCase(nomeFarmaco)) // filtro sul farmaco
				.filter(terapia -> { // filtro sulla sovrapposizione delle date
					LocalDate esistenteInizio = terapia.getDataInizio();
					LocalDate esistenteFine = terapia.getDataFine();

					boolean startOverlap = !dataInizio.isAfter(esistenteFine);
					boolean endOverlap = !dataFine.isBefore(esistenteInizio);

					return startOverlap && endOverlap;
				})
				.collect(Collectors.toList());
		}
		
		if(!conflitti.isEmpty()) {
			msg = new StringBuilder("Terapie in conflitto:\n");
			conflitti.forEach(terapia ->
					msg.append("- ").append(terapia.getNomeFarmaco()).append(": ")
					   .append(terapia.getDataInizio().format(AdminService.dateFormatter)).append(" -> ")
					   .append(terapia.getDataFine().format(AdminService.dateFormatter)).append("\n")
			);
			return TerapiaResult.INVALID_DATE_RANGE;
		}

		boolean ok;
		if(t!= null) {
			// modifica della terapia
			terapia = new Terapia(
				t.getId(), 
				t.getCf(), 
				t.getNomeFarmaco(), 
				dosiGiornaliereInt, 
				quantitàInt, 
				dataInizio,
				dataFine, 
				indicazioniField.getText(), 
				u.getCf(), 
				false,
				0);
			ok = AdminService.modificaTerapia(terapia);
		}
		else {
			// Creazione della terapia nel database
			terapia = new Terapia(
				0, 
				p.getCf(), 
				nomeFarmaco, 
				dosiGiornaliereInt, 
				quantitàInt, 
				dataInizio, 
				dataFine, 
				indicazioni,
				u.getCf(), 
				false,
				0);
			ok = AdminService.creaTerapia(terapia);
		}

		if(ok) {
			return TerapiaResult.SUCCESS;
		} else {
			return TerapiaResult.FAILURE;
		}
	}
	
	@FXML
	private void handleTerapia(ActionEvent event) throws IOException {
		TerapiaResult result = manageTerapia(farmacoField.getText(), dosiGiornaliereField.getText(), quantitàField.getText(), dataInizioField.getValue(), dataFineField.getValue(), indicazioniField.getText());

		switch(result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Per favore, compila tutti i campi.");
			case INVALID_DATA -> MessageUtils.showError("Dati non validi. Controlla le date e i numeri inseriti.");
			case INVALID_DATE_RANGE -> MessageUtils.showError(msg.toString());
			case FAILURE -> MessageUtils.showError("Errore durante l'inserimento della terapia nel database.");
			case SUCCESS -> {
				if(t == null) {
					MessageUtils.showSuccess("Terapia creata con successo.");
					Navigator.getInstance().switchToMostraDatiPaziente(event);
				}
				else {
					Sessione.getInstance().setTerapiaSelezionata(terapia);
					MessageUtils.showSuccess("Terapia modificata con successo.");
					Navigator.getInstance().switchToMostraDettagliTerapia(event);
				}
			}
		} 
	}
	
	// NAVIGAZIONE
	@FXML
	private void indietro(ActionEvent event) throws IOException {
		if(t != null) {
			Navigator.getInstance().switchToMostraDettagliTerapia(event);
		}
		else {
			Navigator.getInstance().switchToMostraDatiPaziente(event);
		}
	}
}