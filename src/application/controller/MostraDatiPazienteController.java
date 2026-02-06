package application.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import application.model.Dato;
import application.model.Glicemia;
import application.model.Patologia;
import application.model.Paziente;
import application.model.Peso;
import application.model.Questionario;
import application.model.Terapia;
import application.model.TerapiaConcomitante;
import application.service.AdminService;
import application.utils.MessageUtils;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class MostraDatiPazienteController {

	//VARIABILI
	private Paziente p;
	private String scelta;
	private LocalDate date;
	private LocalDate date2;
	private List<Glicemia> glicemia = new ArrayList<>();
	private List<Terapia> terapie = new ArrayList<>();
	private List<Questionario> questionari = new ArrayList<>();
	private List<Dato> fattori = new ArrayList<>();
	private List<Dato> comorbidità = new ArrayList<>();
	private List<Dato> allergie = new ArrayList<>();
	private List<TerapiaConcomitante> terapieConcomitanti = new ArrayList<>();
	private List<Patologia> patologie = new ArrayList<>();
	private List<Peso> peso = new ArrayList<>();
	
	// GRAFICO GLICEMIA
	@FXML private LineChart<String, Number> grafico;

	// GRAFICO PESO
	@FXML private LineChart<String, Number> graficoPeso;
	
	//LABEL
	@FXML private Label labelPaziente;
	@FXML private Label dataDiNascitaDato;
	@FXML private Label sessoDato;
	@FXML private Label mailDato;
	@FXML private Label medicoRifLabel;
	@FXML private ComboBox<String> sceltaVisualizza;
	@FXML private DatePicker dataVisualizza;
	@FXML private Label luogoLabel;

	//LISTE
	@FXML private ListView<Terapia> listaTerapiePaziente;
	@FXML public ListView<Dato> listaFattori;
	@FXML public ListView<Dato> listaComorbidità;
	@FXML public ListView<Dato> listaAllergie;
	@FXML public ListView<Patologia> listaPatologie;
	@FXML public ListView<TerapiaConcomitante> listaTerapieConcomitanti;
	@FXML public ListView<Questionario> listaQuestionari;
	
	@FXML
	private void initialize() {
		// Recupero paziente selezionato
		p = Sessione.getInstance().getPaziente();
		
		// Caricamento dati paziente dal database
		caricaDatiPaziente();

		// Set up interfaccia
		setUpInterfaccia();
		
		try {
			visualizzaDati();
		} catch (IOException e) {
			e.printStackTrace();
			MessageUtils.showError("Errore nel caricamento dati del paziente: " + p.getNome() + " " + p.getCognome());
		}
	}

	private void caricaDatiPaziente() {
		glicemia = AdminService.loadGlicemiaByPaziente(p);
		terapie = AdminService.loadTerapieByPaziente(p);
		questionari = AdminService.loadQuestionariByPaziente(p);
		fattori = AdminService.loadFattoriByPaziente(p);
		comorbidità = AdminService.loadComorbiditàByPaziente(p);
		allergie = AdminService.loadAllergieByPaziente(p);
		terapieConcomitanti = AdminService.loadTerapieConcomitantiByPaziente(p);
		patologie = AdminService.loadPatologieByPaziente(p);
		peso = AdminService.loadPesoByCf(p.getCf());
	}
	
	private void setUpInterfaccia() {
		labelPaziente.setText("Profilo clinico di " + p.getNome() + " " + p.getCognome());
		labelPaziente.setFocusTraversable(true);
		dataDiNascitaDato.setText(p.getDataDiNascita().format(AdminService.dateFormatter));
		sessoDato.setText(p.getSesso());
		mailDato.setText(p.getMail());
		luogoLabel.setText(p.getLuogoDiNascita());
		
		medicoRifLabel.setText(AdminService.getNomeDiabetologoByCf(p.getDiabetologoRif()) + " (" + p.getDiabetologoRif() + ")");
			
		sceltaVisualizza.getItems().addAll("Settimana", "Mese");
	}

	private void visualizzaDati() throws IOException {
		// TERAPIE
		listaTerapiePaziente.setItems(FXCollections.observableArrayList(terapie));
		listaTerapiePaziente.setCellFactory(e -> new ListCell<Terapia>() {
			protected void updateItem(Terapia t, boolean empty) {
				super.updateItem(t, empty);
				
				if (empty || t == null) {
					setText(null);
					setStyle("");
				} else {
					setText(t.getNomeFarmaco() + ": " + t.getDataInizio().format(AdminService.dateFormatter) + 
						" - " + t.getDataFine().format(AdminService.dateFormatter));

					// Evidenzia in rosso le terapie con questionari mancanti
					if(t.getDataFine().isBefore(LocalDate.now())) {
						long giorniDiDifferenza = ChronoUnit.DAYS.between(t.getDataInizio(), t.getDataFine()) + 1;
						if(giorniDiDifferenza != t.getQuestionari()) {
							setStyle("-fx-background-color: #e47171ff; -fx-font-weight: bold");
						} else {
							setStyle("");
						}
					}
				}
			}
		});
		listaTerapiePaziente.setOnMouseClicked(e -> {
			Terapia selectedTerapia = listaTerapiePaziente.getSelectionModel().getSelectedItem();
			if(selectedTerapia != null) {
				Sessione.getInstance().setTerapiaSelezionata(selectedTerapia);
				try {
					Navigator.getInstance().switchToMostraDettagliTerapia(e);
				} catch (IOException ev) {
					ev.printStackTrace();
				}
			}
		});
	
		// FATTORI DI RISCHIO
		listaFattori.setItems(FXCollections.observableArrayList(fattori));
		AdminService.setCustomCellFactory(listaFattori, f -> 
			f.getNome() + " - Aggiunto da: " + AdminService.getNomeDiabetologoByCf(f.getModificato()));
		
		// COMORBIDITÀ
		listaComorbidità.setItems(FXCollections.observableArrayList(comorbidità));
		AdminService.setCustomCellFactory(listaComorbidità, c -> 
			c.getNome() + " - Aggiunto da: " + AdminService.getNomeDiabetologoByCf(c.getModificato()));
		
		// ALLERGIE
		listaAllergie.setItems(FXCollections.observableArrayList(allergie));
		AdminService.setCustomCellFactory(listaAllergie, a -> 
			a.getNome() + " - Aggiunto da: " + AdminService.getNomeDiabetologoByCf(a.getModificato()));
		
		// PATOLOGIE
		listaPatologie.setItems(FXCollections.observableArrayList(patologie));
		AdminService.setCustomCellFactory(listaPatologie, p -> p.getNome());
		listaPatologie.setOnMouseClicked(e -> {
			Patologia selectedPatologia = listaPatologie.getSelectionModel().getSelectedItem();
			if(selectedPatologia != null) {
				Sessione.getInstance().setPatologiaSelezionata(selectedPatologia);
				try {
					Navigator.getInstance().switchToMostraPatologia(e);
				} catch (IOException ev) {
					ev.printStackTrace();
				}
			}
		});
		
		// TERAPIE CONCOMITANTI
		listaTerapieConcomitanti.setItems(FXCollections.observableArrayList(terapieConcomitanti));
		AdminService.setCustomCellFactory(listaTerapieConcomitanti, tc -> tc.getNome());
		listaTerapieConcomitanti.setOnMouseClicked(e -> {
			TerapiaConcomitante selectedTerapiaConcomitante = listaTerapieConcomitanti.getSelectionModel().getSelectedItem();
			if(selectedTerapiaConcomitante != null) {
				Sessione.getInstance().setTerapiaConcomitanteSelezionata(selectedTerapiaConcomitante);
				try {
					Navigator.getInstance().switchToMostraTerapiaConcomitante(e);
				} catch (IOException ev) {
					ev.printStackTrace();
				}
			}
		});
		
		// LISTA QUESTIONARI
		listaQuestionari.setItems(FXCollections.observableArrayList(questionari));
		AdminService.setCustomCellFactory(listaQuestionari, q -> q.getNomeFarmaco() + " (" + q.getGiornoCompilazione().format(AdminService.dateFormatter) + ")");
		listaQuestionari.setOnMouseClicked(e -> {
			Questionario selectedQuestionario = listaQuestionari.getSelectionModel().getSelectedItem();
			if(selectedQuestionario != null) {
				Sessione.getInstance().setQuestionarioSelezionato(selectedQuestionario);
				try {
					Navigator.getInstance().switchVediQuestionario(e);
				} catch (IOException ev) {
					ev.printStackTrace();
				}	
			}
		});

		// GRAFICO PESO
		XYChart.Series<String, Number> misurazioni = new XYChart.Series<>();
		graficoPeso.getData().clear();
		misurazioni.getData().clear();
		misurazioni.getData().addAll(
			peso.stream()
				.map(p -> new XYChart.Data<String, Number>(p.getGiorno().format(AdminService.dateFormatter), p.getValore()))
				.toList()
		);	
		graficoPeso.getData().add(misurazioni);
	}
	
	// GRAFICO GLICEMIA
	private enum SceltaResult {
		EMPTY_FIELD,
		DATE_IN_FUTURE,
		OK
	}

	// Logica di validazione della scelta
	private SceltaResult tryScelta(String scelta, LocalDate date) {
		if(date == null || scelta == null) {
			return SceltaResult.EMPTY_FIELD;
		} else if(date.isAfter(LocalDate.now())) {
			return SceltaResult.DATE_IN_FUTURE;
		}
		return SceltaResult.OK;
	}
	
	// Gestione grafica
	@FXML
	private void handleScelta() throws IOException {
		SceltaResult result = tryScelta(sceltaVisualizza.getValue(), dataVisualizza.getValue());

		switch(result) {
			case EMPTY_FIELD -> MessageUtils.showError("Scegli data e periodo.");
			case DATE_IN_FUTURE -> MessageUtils.showError("Scegliere una data antecedente a:\n" + LocalDate.now());
			case OK -> {
				scelta = sceltaVisualizza.getValue();
				date = dataVisualizza.getValue();
				if("Settimana".equals(scelta)) {
					date2 = date.plusDays(7);
					if(date2.isAfter(LocalDate.now()))
						date2 = LocalDate.now();

					grafico.getData().clear(); // svuota il grafico
					XYChart.Series<String, Number> serie = new XYChart.Series<>();
					for(Glicemia g : glicemia) {
						if(!g.getGiorno().isBefore(date) && !g.getGiorno().isAfter(date2)) {
							final int valore = g.getValore();
							final String giorno = g.getGiorno().format(DateTimeFormatter.ofPattern("dd/MM")) + "\n" + g.getOrario();
							final String indicazioni = g.getIndicazioni();

							XYChart.Data<String, Number> punto = new XYChart.Data<>(giorno, valore);
							punto = AdminService.proprietàPunto(punto, valore, indicazioni);
							serie.getData().add(punto);
						}
					}
					grafico.getData().add(serie);

				} else if("Mese".equals(scelta)) {
					date2 = date.plusMonths(1);
					if(date2.isAfter(LocalDate.now()))
						date2 = LocalDate.now();
				
					grafico.getData().clear();
					XYChart.Series<String, Number> serieMax = new XYChart.Series<>();
					XYChart.Series<String, Number> serieMin = new XYChart.Series<>();

					LocalDate giornoCorrente = null;
					int min = Integer.MAX_VALUE;
					int max = Integer.MIN_VALUE;

					for (Glicemia g : glicemia) {
						if(g.getGiorno().isBefore(date) || g.getGiorno().isAfter(date2)) continue;

						if (giornoCorrente != null && !g.getGiorno().equals(giornoCorrente)) {
							String etichetta = giornoCorrente.format(DateTimeFormatter.ofPattern("dd/MM"));
							
							serieMax.getData().add(new XYChart.Data<>(etichetta, max));
							serieMin.getData().add(new XYChart.Data<>(etichetta, min));
							
							// Reset per il nuovo giorno corrente
							min = Integer.MAX_VALUE;
							max = Integer.MIN_VALUE;
						}

						// Aggiorniamo il giorno corrente e i valori min/max
						giornoCorrente = g.getGiorno();
						if (g.getValore() > max) max = g.getValore();
						if (g.getValore() < min) min = g.getValore();
					}

					if (giornoCorrente != null) {
						String etichetta = giornoCorrente.format(DateTimeFormatter.ofPattern("dd/MM"));
						
						serieMax.getData().add(new XYChart.Data<>(etichetta, max));
						serieMin.getData().add(new XYChart.Data<>(etichetta, min));
					}

					grafico.getData().add(serieMax);
					grafico.getData().add(serieMin);
				}
			}
		}
	}

	// NAVIGAZIONE
	@FXML
	private void switchToDiabetologoPage(ActionEvent event) throws IOException {
		Sessione.getInstance().setPaziente(null);
		Navigator.getInstance().switchToDiabetologoPage(event);
	}
	
	@FXML
	private void switchToNuovaTerapia(ActionEvent event) throws IOException {
		Navigator.getInstance().switchToTerapia(event);
	}
	
	@FXML
	private void switchToStoriaDatiPaziente(ActionEvent event) throws IOException {
		Navigator.getInstance().switchToStoriaDatiPaziente(event);
	}
}