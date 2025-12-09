package application.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import application.model.Dato;
import application.model.Patologia;
import application.model.TerapiaConcomitante;
import application.model.Utente;
import application.service.AdminService;
import application.utils.MessageUtils;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class StoriaDatiPazienteController {
	
	// VARIABILI
	private Utente u;
	private Utente p;
	private List<Dato> fattori = new ArrayList<>();
	private List<Dato> comorbidità = new ArrayList<>();
	private List<Dato> allergie = new ArrayList<>();
	private List<TerapiaConcomitante> terapieConcomitanti = new ArrayList<>();
	private List<Patologia> patologie = new ArrayList<>();
	
	@FXML private ComboBox<String> tipologia;
	@FXML private TextField nomeField;
	@FXML private TextField nomePatologiaField;
	@FXML private DatePicker dataPatologiaField;
	@FXML private TextArea indicazioniPatologiaArea;
	@FXML private TextField nomeTerapiaField;
	@FXML private DatePicker dataInizioTerapiaField;
	@FXML private DatePicker dataFineTerapiaField;
	
	@FXML
	private void initialize() {
		u = Sessione.getInstance().getUtente();
		p = Sessione.getInstance().getPazienteSelezionato();

		caricaDati();
		
		tipologia.getItems().addAll("Fattore Di Rischio", "Comorbidità", "Allergia");
	}

	private void caricaDati() {
		fattori = AdminService.loadFattoriByPaziente(p);
		comorbidità = AdminService.loadComorbiditàByPaziente(p);
		allergie = AdminService.loadAllergieByPaziente(p);
		terapieConcomitanti = AdminService.loadTerapieConcomitantiByPaziente(p);
		patologie = AdminService.loadPatologieByPaziente(p);
	}
	
	public enum StoriaDatiPazienteResult {
		SUCCESS,
		FAILURE,
		DATA_ALREADY_EXISTS,
		INVALID_DATE,
		EMPTY_FIELDS,
	}

	// GESTIONE FATTORI DI RISCHIO, COMORBIDITÀ E ALLERGIE ----------------------------------------
	public StoriaDatiPazienteResult tryCreateFattoreComorbiditàAllergie(String tipo, String nome) {
		if(nome == null || nome.isBlank() || tipo == null) {
			return StoriaDatiPazienteResult.EMPTY_FIELDS;
		}

		Dato dato = new Dato(p.getCf(), nome, u.getCf());

		boolean esisteGia = false;
    	boolean operazioneRiuscita = false;

		switch(tipo) {
			case "Fattore Di Rischio":
				esisteGia = fattori.stream().anyMatch(f -> f.getNome().equalsIgnoreCase(nome));
				if(!esisteGia) {
					operazioneRiuscita = AdminService.creaFattore(dato);
				}
				break;
			case "Comorbidità":
				esisteGia = comorbidità.stream().anyMatch(c -> c.getNome().equalsIgnoreCase(nome));
				if(!esisteGia) {
					operazioneRiuscita = AdminService.creaComorbidità(dato);
				}
				break;
			case "Allergia":
				esisteGia = allergie.stream().anyMatch(a -> a.getNome().equalsIgnoreCase(nome));
				if(!esisteGia) {
					operazioneRiuscita = AdminService.creaAllergia(dato);
				}
				break;
			default:
				return StoriaDatiPazienteResult.FAILURE;
		}

		if(esisteGia) {
			return StoriaDatiPazienteResult.DATA_ALREADY_EXISTS;
		} else if(operazioneRiuscita) {
			return StoriaDatiPazienteResult.SUCCESS;
		} else {
			return StoriaDatiPazienteResult.FAILURE;
		}
	}
	@FXML
	private void aggiungiFattoreComorbiditàAllergia(ActionEvent event) throws IOException { 
		StoriaDatiPazienteResult result = tryCreateFattoreComorbiditàAllergie(tipologia.getValue(), nomeField.getText());

		switch (result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Inserire tutti i dati.");
			case DATA_ALREADY_EXISTS -> MessageUtils.showError("Dato già presente.");
			case FAILURE -> MessageUtils.showError("Errore nell'inserimento del dato.");
			case INVALID_DATE -> {} // Caso non interessante per questo dato
			case SUCCESS -> {
				MessageUtils.showSuccess("Dato paziente inserito.");
				switchToMostraDatiPaziente(event);
			}
		}
	}
	public StoriaDatiPazienteResult tryRemoveFattoreComorbiditàAllergie(String tipo, String nome) {
		if(nome == null || nome.isBlank() || tipo == null) {
			return StoriaDatiPazienteResult.EMPTY_FIELDS;
		}
		
		Dato dato = new Dato(p.getCf(), nome, u.getCf());
		boolean operazioneRiuscita = false;

		switch(tipo) {
			case "Fattore Di Rischio":
				operazioneRiuscita = AdminService.eliminaFattore(dato);
				break;
			case "Comorbidità":
				operazioneRiuscita = AdminService.eliminaComorbidità(dato);
				break;
			case "Allergia":
				operazioneRiuscita = AdminService.eliminaAllergia(dato);
				break;
			default:
				return StoriaDatiPazienteResult.FAILURE;
		}

		if(!operazioneRiuscita) {
			return StoriaDatiPazienteResult.FAILURE;
		} else {
			return StoriaDatiPazienteResult.SUCCESS;
		}
	}
	@FXML
	private void rimuoviFattoreComorbiditàAllergia(ActionEvent event) throws IOException {
		StoriaDatiPazienteResult result = tryRemoveFattoreComorbiditàAllergie(tipologia.getValue(), nomeField.getText());

		switch (result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Inserire tutti i dati.");
			case FAILURE -> MessageUtils.showError("Errore nella rimozione del dato.\nDato non trovato.");
			case DATA_ALREADY_EXISTS -> {} // Caso non interessante per la rimozione
			case INVALID_DATE -> {} // Caso non interessante per questo dato
			case SUCCESS -> {
				MessageUtils.showSuccess("Dato paziente rimosso.");
				switchToMostraDatiPaziente(event);
			}
		}
	}
	
	// GESTIONE PATOLOGIE PREGRESSE ---------------------------------------------------------------------------
	public StoriaDatiPazienteResult tryCreatePatologia(String nome, LocalDate dataInizio, String indicazioni) {
		if(nome == null || nome.isBlank() || indicazioni == null || indicazioni.isBlank() || dataInizio == null) {
			return StoriaDatiPazienteResult.EMPTY_FIELDS;
		}
		else if (patologie.stream()
				.anyMatch(patologia -> patologia.getNome().equalsIgnoreCase(nome))) {
			return StoriaDatiPazienteResult.DATA_ALREADY_EXISTS;
		}
		else if(dataInizio.isAfter(LocalDate.now())) {
			return StoriaDatiPazienteResult.INVALID_DATE;
		}
		
		Patologia patologia = new Patologia(
				p.getCf(),
				nome,
				dataInizio,
				indicazioni,
				u.getCf()
			);
		boolean ok = AdminService.creaPatologia(patologia);
		if(ok) {
			return StoriaDatiPazienteResult.SUCCESS;
		} else {
			return StoriaDatiPazienteResult.FAILURE;
		}
	}
	@FXML
	private void aggiungiPatologia(ActionEvent event) throws IOException {
		StoriaDatiPazienteResult result = tryCreatePatologia(
				nomePatologiaField.getText(),
				dataPatologiaField.getValue(),
				indicazioniPatologiaArea.getText()
			);

		switch (result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Inserire tutti i dati.");
			case DATA_ALREADY_EXISTS -> MessageUtils.showError("Patologia già presente.");
			case INVALID_DATE -> MessageUtils.showError("La data di inizio non può essere futura.");
			case FAILURE -> MessageUtils.showError("Errore nell'inserimento della patologia.");
			case SUCCESS -> {
				AdminService.loadPatologieByPaziente(p);
				MessageUtils.showSuccess("Patologia paziente inserita.");
				switchToMostraDatiPaziente(event);
			}
		}
	}
	public StoriaDatiPazienteResult tryRemovePatologia(String nome, LocalDate dataInizio, String indicazioni) {
		if(nome == null || nome.isBlank()) {
			return StoriaDatiPazienteResult.EMPTY_FIELDS;
		}
		
		Patologia patologia = new Patologia(
				p.getCf(),
				nome,
				dataInizio,
				indicazioni,
				u.getCf()
			);
		boolean ok = AdminService.eliminaPatologia(patologia);
		if(ok) {
			return StoriaDatiPazienteResult.SUCCESS;
		} else {
			return StoriaDatiPazienteResult.FAILURE;
		}
	}
	@FXML
	private void rimuoviPatologia(ActionEvent event) throws IOException {
		StoriaDatiPazienteResult result = tryRemovePatologia(
				nomePatologiaField.getText(),
				dataPatologiaField.getValue(),
				indicazioniPatologiaArea.getText()
			);

		switch (result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Inserire il nome della patologia.");
			case FAILURE -> MessageUtils.showError("Errore nella rimozione della patologia.\nPatologia non trovata.");
			case DATA_ALREADY_EXISTS -> {} // Caso non interessante per la rimozione
			case INVALID_DATE -> {} // Caso non interessante per questo dato
			case SUCCESS -> {
				AdminService.loadPatologieByPaziente(p);
				MessageUtils.showSuccess("Patologia paziente rimossa.");
				switchToMostraDatiPaziente(event);
			}
		}
	}

	// GESTIONE TERAPIE CONCOMITANTI ------------------------------------------------------------------------------------
	public StoriaDatiPazienteResult tryCreateTerapiaConcomitante(String nome, LocalDate dataInizio, LocalDate dataFine) {
		if(nome == null || nome.isBlank() || dataInizio == null || dataFine == null) {
			return StoriaDatiPazienteResult.EMPTY_FIELDS;
		}
		else if (terapieConcomitanti.stream()
				.anyMatch(terapia -> terapia.getNome().equalsIgnoreCase(nome)
						&& terapia.getDataInizio().equals(dataInizio))) {
			return StoriaDatiPazienteResult.DATA_ALREADY_EXISTS;
		}
		else if(dataFine.isBefore(dataInizio)) {
			return StoriaDatiPazienteResult.INVALID_DATE;
		}
		
		TerapiaConcomitante terapiaConcomitante = new TerapiaConcomitante(
				p.getCf(),
				nome,
				dataInizio,
				dataFine,
				u.getCf()
			);
		boolean ok = AdminService.creaTerapiaConcomitante(terapiaConcomitante);
		if(ok) {
			return StoriaDatiPazienteResult.SUCCESS;
		} else {
			return StoriaDatiPazienteResult.FAILURE;
		}
	}
	@FXML
	private void aggiungiTerapia(ActionEvent event) throws IOException {
		StoriaDatiPazienteResult result = tryCreateTerapiaConcomitante(
				nomeTerapiaField.getText(),
				dataInizioTerapiaField.getValue(),
				dataFineTerapiaField.getValue()
			);

		switch (result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Inserire tutti i dati.");
			case DATA_ALREADY_EXISTS -> MessageUtils.showError("Terapia concomitante già presente.");
			case INVALID_DATE -> MessageUtils.showError("La data di fine non può essere precedente alla data di inizio.");
			case FAILURE -> MessageUtils.showError("Errore nell'inserimento della terapia concomitante.");
			case SUCCESS -> {
				AdminService.loadTerapieConcomitantiByPaziente(p);
				MessageUtils.showSuccess("Terapia concomitante paziente inserita.");
				switchToMostraDatiPaziente(event);
			}
		}
	}
	public StoriaDatiPazienteResult tryRemoveTerapia(String nome, LocalDate dataInizio, LocalDate dataFine) {
		if(nome == null || nome.isBlank() || dataInizio == null) {
			return StoriaDatiPazienteResult.EMPTY_FIELDS;
		}
		else if(dataFine.isBefore(dataInizio)) {
			return StoriaDatiPazienteResult.INVALID_DATE;
		}
		
		TerapiaConcomitante terapiaConcomitante = new TerapiaConcomitante(
				p.getCf(),
				nome,
				dataInizio,
				dataFine,
				u.getCf()
			);
		boolean ok = AdminService.eliminaTerapiaConcomitante(terapiaConcomitante);
		if(ok) {
			return StoriaDatiPazienteResult.SUCCESS;
		} else {
			return StoriaDatiPazienteResult.FAILURE;
		}
	}
	@FXML
	private void rimuoviTerapia(ActionEvent event) throws IOException {
		StoriaDatiPazienteResult result = tryRemoveTerapia(
				nomeTerapiaField.getText(),
				dataInizioTerapiaField.getValue(),
				dataFineTerapiaField.getValue()
			);

		switch (result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Inserire il nome e la data di inizio della terapia.");
			case INVALID_DATE -> MessageUtils.showError("La data di fine non può essere precedente alla data di inizio.");
			case FAILURE -> MessageUtils.showError("Errore nella rimozione della terapia concomitante.\nTerapia non trovata.");
			case DATA_ALREADY_EXISTS -> {} // Caso non interessante per la rimozione
			case SUCCESS -> {
				AdminService.loadTerapieConcomitantiByPaziente(p);
				MessageUtils.showSuccess("Terapia concomitante paziente rimossa.");
				switchToMostraDatiPaziente(event);
			}
		}
	}
	
	// SVUOTA LISTE
	private void clearAll() {
		terapieConcomitanti.clear();
		patologie.clear();
		fattori.clear();
		comorbidità.clear();
		allergie.clear();
	}

	// NAVIGAZIONE
	@FXML
	private void switchToMostraDatiPaziente(ActionEvent event) throws IOException {
		clearAll();
		Navigator.getInstance().switchToMostraDatiPaziente(event);
	}
}
