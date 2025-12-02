package application.controller;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import application.model.Glicemia;
import application.model.Mail;
import application.model.Peso;
import application.model.Questionario;
import application.model.Terapia;
import application.model.Utente;
import application.service.AdminService;
import application.utils.MessageUtils;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PazienteController {

	// VARIABILI
	private Utente p;
	private int compilato = 0;
	private int terapieAttive = 0;
	private boolean aggiorna = false;
	private List<Glicemia> glicemia = new ArrayList<>();
	private List<Terapia> terapie = new ArrayList<>();
	private List<Questionario> questionari = new ArrayList<>();
	private List<Mail> mailRicevute = new ArrayList<>();
	private List<Peso> peso = new ArrayList<>();
	
	// GRAFICO GLICEMIA
	@FXML private LineChart<String, Number> graficoGlicemia;
	@FXML private TextField valoreField;
	@FXML private TextField oraField;
	@FXML private TextField minutiField;
	@FXML private ComboBox<String> indicazioniBox;
	@FXML private Button oggiGlicemia;
	@FXML private Button settimanaGlicemia;
	@FXML private Button meseGlicemia;

	// GRAFICO PESO
	@FXML private LineChart<String, Number> graficoPeso;
	@FXML private TextField pesoField;

	// FXML PAGINA
	@FXML private Label welcomeLabel;
	@FXML private Button mailButton;
	@FXML private Button questButton;

	// PROFILO
	@FXML private Label nomeLabel;
	@FXML private Label ddnLabel;
	@FXML private Label sessoLabel;
	@FXML private Label diabetologoLabel;
	@FXML private ImageView fotoProfilo;

	// TERAPIE CORRENTI
	@FXML private Label terapiaCorrente;
	@FXML private ListView<Terapia> terapieCorrenti;

	// QUESTIONARI
	@FXML private ListView<Questionario> listaQuestionari;
	
	@FXML 
	private void initialize() throws IOException {
		p = Sessione.getInstance().getUtente();
		
		caricaDatiPaziente();
		setUpInterfaccia();
		setUpTerapieInCorso();
		setUpQuestionari();
		setUpCompilazioneQuest();
	    visualizzaGraficoGlicemia(1);
		visualizzaGraficoPeso();
	    javafx.application.Platform.runLater(() -> notificaTerapia());
	} // FINE INITIALIZE ---------------------------------------------
	
	// CARICAMENTO E VISUALIZZAZIONE DATI
	private void caricaDatiPaziente() {
		glicemia = AdminService.loadGlicemiaByPaziente(p);
		terapie = AdminService.loadTerapieByPaziente(p);
		questionari = AdminService.loadQuestionariByPaziente(p);
		mailRicevute = AdminService.loadMailRicevute(p);
		peso = AdminService.loadPesoByCf(p.getCf());
	}

	private void setUpInterfaccia() {
		welcomeLabel.setText("Ciao, " + p.getNomeCognome());
		welcomeLabel.setFocusTraversable(true);

		nomeLabel.setText(p.getNomeCognome());
		ddnLabel.setText(p.getDataDiNascita().format(AdminService.dateFormatter));
		sessoLabel.setText(p.getSesso());
		diabetologoLabel.setText(AdminService.getNomeUtenteByCf(p.getDiabetologoRif()));
		Image image = new Image(p.getFoto());
		fotoProfilo.setImage(image);

		mailButton.setText(AdminService.contatoreMailNonLette(mailRicevute) > 0 ? AdminService.contatoreMailNonLette(mailRicevute) + " Mail" : "Mail");
	    mailButton.setStyle(AdminService.contatoreMailNonLette(mailRicevute) > 0 ? "-fx-text-fill: red;" : "-fx-text-fill: black;");
	    
		indicazioniBox.getItems().addAll("Pre pasto", "Post pasto");

		// IMPOSTAZIONE BOTTONI PER VISUALIZZAZIONE GRAFICO GLICEMIA
		oggiGlicemia.setOnMouseClicked(event -> visualizzaGraficoGlicemia(1));
		settimanaGlicemia.setOnMouseClicked(event -> visualizzaGraficoGlicemia(7));
		meseGlicemia.setOnMouseClicked(event -> visualizzaGraficoGlicemia(30));
	}

	private void notificaTerapia() {
		List<Terapia> terapieDaNotificare = terapie.stream()
			.filter(t -> !t.getVisualizzata() 
					&& !t.getDataInizio().isAfter(LocalDate.now()) 
					&& !t.getDataFine().isBefore(LocalDate.now()))
			.toList();

		boolean serveRicaricare = false;

		for (Terapia t : terapieDaNotificare) {
			Optional<ButtonType> result = MessageUtils.showConferma("Inizio terapia", "È iniziata una nuova terapia: " + t.getNomeFarmaco());

			if(result.isPresent() && result.get() == ButtonType.OK) {
				boolean ok = AdminService.notificaTerapia(t);
				if (ok) {
					serveRicaricare = true;
				} else {
					MessageUtils.showError("Errore nella lettura della notifica.");
				}
			}
		}

		if (serveRicaricare) {
			terapie = AdminService.loadTerapieByPaziente(Sessione.getInstance().getUtente());
			setUpTerapieInCorso();
		}
	}
	
	private void visualizzaGraficoGlicemia(int giorni) {
		graficoGlicemia.getData().clear();

		if(giorni == 1) {
			XYChart.Series<String, Number> serie = new XYChart.Series<>();
			serie.setName("Glicemia giornaliera");
			for(Glicemia glicemia : glicemia) {
				if(glicemia.getGiorno().isEqual(LocalDate.now())) {
					
					final int valore = glicemia.getValore();
					final String orario = glicemia.getOrario();
					final String indicazioni = glicemia.getIndicazioni();
					
					XYChart.Data<String, Number> punto = new XYChart.Data<>(orario, valore);
					
					punto = AdminService.proprietàPunto(punto, valore, indicazioni);
					serie.getData().add(punto);
				}
			}
			graficoGlicemia.getData().add(serie);
		}
		else if(giorni == 7) {
			XYChart.Series<String, Number> serie = new XYChart.Series<>();
			serie.setName("Glicemia settimanale");
			LocalDate dataLimite = LocalDate.now().minusDays(giorni);
			for(Glicemia glicemia : glicemia) {
				if(!glicemia.getGiorno().isBefore(dataLimite)) {
					final int valore = glicemia.getValore();
					final String giorno = glicemia.getGiorno().format(DateTimeFormatter.ofPattern("dd/MM")) + "\n" + glicemia.getOrario();
					final String indicazioni = glicemia.getIndicazioni();

					XYChart.Data<String, Number> punto = new XYChart.Data<>(giorno, valore);

					punto = AdminService.proprietàPunto(punto, valore, indicazioni);
					serie.getData().add(punto);
				}
			}
			graficoGlicemia.getData().add(serie);
		}
		else if(giorni == 30) {
			LocalDate dataLimite = LocalDate.now().minusMonths(1);

			XYChart.Series<String, Number> serieMax = new XYChart.Series<>();
			serieMax.setName("Massime");

			XYChart.Series<String, Number> serieMin = new XYChart.Series<>();
			serieMin.setName("Minime");

			LocalDate giornoCorrente = null;
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;

			// Iteriamo sulla lista (che sappiamo essere già ordinata dal DB/query)
			for (Glicemia g : glicemia) {
				if (g.getGiorno().isBefore(dataLimite)) continue; // Salta i vecchi

				// Se cambia il giorno (e non è il primo giro), salviamo i dati del giorno PRECEDENTE
				if (giornoCorrente != null && !g.getGiorno().equals(giornoCorrente)) {
					// ATTENZIONE: Usiamo giornoCorrente per l'etichetta, non g.getGiorno()!
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

			// Aggiungi l'ultimo giorno rimasto "in sospeso" dopo la fine del ciclo
			if (giornoCorrente != null) {
				String etichetta = giornoCorrente.format(DateTimeFormatter.ofPattern("dd/MM"));
				
				serieMax.getData().add(new XYChart.Data<>(etichetta, max));
				serieMin.getData().add(new XYChart.Data<>(etichetta, min));
			}

			graficoGlicemia.getData().add(serieMax);
			graficoGlicemia.getData().add(serieMin);
		}
	}
	
	private void visualizzaGraficoPeso() {
		graficoPeso.getData().clear();

		XYChart.Series<String, Number> serie = new XYChart.Series<>();
		serie.setName("Andamento del peso corporeo");
		for(Peso peso : peso) {
			final double valore = peso.getValore();
			final String giorno = peso.getGiorno().format(DateTimeFormatter.ofPattern("dd/MM"));

			XYChart.Data<String, Number> punto = new XYChart.Data<>(giorno, valore);
			serie.getData().add(punto);
		}
		graficoPeso.getData().add(serie);
	}

	private void setUpTerapieInCorso() {
		List<Terapia> listaFiltrata = terapie.stream()
				.filter(t -> !t.getDataInizio().isAfter(LocalDate.now()) && !t.getDataFine().isBefore(LocalDate.now()))
				.collect(Collectors.toList());
		terapieCorrenti.setItems(FXCollections.observableArrayList(listaFiltrata));
		AdminService.setCustomCellFactory(terapieCorrenti, t -> 
			"Farmaco: " + t.getNomeFarmaco() + 
			"\nData inizio: " + t.getDataInizio().format(AdminService.dateFormatter) +
			"\nData fine: " + t.getDataFine().format(AdminService.dateFormatter)
		);
		terapieCorrenti.setOnMouseClicked(e -> {
			Terapia selectedTerapia = terapieCorrenti.getSelectionModel().getSelectedItem();
			if(selectedTerapia != null) {
				Sessione.getInstance().setTerapiaSelezionata(selectedTerapia);
				try {
					Navigator.getInstance().switchToMostraDettagliTerapia(e);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		terapieAttive = listaFiltrata.size();
	}

	private void setUpQuestionari() {
		listaQuestionari.setItems(FXCollections.observableArrayList(questionari));
		AdminService.setCustomCellFactory(listaQuestionari, q -> q.getNomeFarmaco() + " (" + q.getGiornoCompilazione() + ")");
		listaQuestionari.setOnMouseClicked(e -> {
			Questionario selectedQuestionario = listaQuestionari.getSelectionModel().getSelectedItem();
			if(selectedQuestionario != null) {
				Sessione.getInstance().setQuestionarioSelezionato(selectedQuestionario);
				try {
					Navigator.getInstance().switchVediQuestionario(e);
				} catch (IOException ex) {
					ex.printStackTrace();
				}	
			}
		});
	}

	private void setUpCompilazioneQuest() {
		for(Questionario q : questionari) {
			if(q.getGiornoCompilazione().equals(LocalDate.now())) {
				compilato++;
			}
		}

		if(terapieAttive > 0) {
			if(compilato < terapieAttive)
				questButton.setText("Questionario odierno da compilare!");
			else if(compilato == terapieAttive) {
				questButton.setText("Questionari odierni compilati!");
				questButton.setDisable(true);
			}
		}
		else {
			questButton.setDisable(true);
			questButton.setText("Nessun questionario da compilare!");
		}
	}

	// GESTIONE GLICEMIA PAZIENTE
	public enum GlicemiaResult {
		EMPTY_FIELDS,
		INVALID_DATA,
		SUCCESS,
		FAILURE
	}
	public GlicemiaResult tryCreateGlicemia(String valore, String ora, String minuti, String indicazioni) {
		if (valore.isEmpty() || ora.isEmpty() || minuti.isEmpty() || indicazioni == null) {
	    	return GlicemiaResult.EMPTY_FIELDS;
	    }
		
		int oraInt, minutiInt, valoreInt;
		String orario;
		try {
			oraInt = Integer.parseInt(ora);
	        minutiInt = Integer.parseInt(minuti);
	        valoreInt = Integer.parseInt(valore);

			LocalTime orarioObj = LocalTime.of(oraInt, minutiInt);
			orario = orarioObj.format(DateTimeFormatter.ofPattern("HH:mm"));
		} catch (NumberFormatException| DateTimeException e) {
	        return GlicemiaResult.INVALID_DATA;
	    }

	    Glicemia g = new Glicemia(p.getCf(), valoreInt, LocalDate.now(), orario, indicazioni);
		boolean ok = AdminService.creaGlicemia(g);
		if(ok) {
			valoreField.clear();
			oraField.clear();
			minutiField.clear();
			glicemia.add(g); // aggiungo la glicemia appena creata alla lista, senza andare a richiamare il db per caricarle tutte nuovamente
			visualizzaGraficoGlicemia(1);
			return GlicemiaResult.SUCCESS;
		}
		else {
			return GlicemiaResult.FAILURE;
		}
	}
	@FXML
	private void handleGlicemia(ActionEvent event) throws IOException {
		GlicemiaResult result = tryCreateGlicemia(valoreField.getText().trim(), oraField.getText().trim(), minutiField.getText().trim(), indicazioniBox.getValue());

		switch(result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Per favore, compila tutti i campi.");
			case INVALID_DATA -> MessageUtils.showError("Compila i dati correttamente.");
			case FAILURE -> MessageUtils.showError("Errore durante l'inserimento della glicemia.");
			case SUCCESS -> {
				MessageUtils.showSuccess("Glicemia aggiunta con successo!");
			}
		}
	}

	// GESTIONE PESO PAZIENTE
	public enum PesoResult {
		EMPTY_FIELDS,
		INVALID_DATA,
		ALREADY_INSERT,
		SUCCESS,
		FAILURE
	}
	public PesoResult tryCreatePeso(String pesoString, boolean aggiorna) {
		if(pesoString == null || pesoString.isBlank()) {
			return PesoResult.EMPTY_FIELDS;
		}

		double pesoDouble;
		try {
			pesoDouble = Double.parseDouble(pesoString);
		} catch (NumberFormatException e) {
	        return PesoResult.INVALID_DATA;
	    }

		// posso inserire una misurazione alla settimana
		Optional<Peso> misurazioneRecente = peso.stream()
			.filter(p -> !p.getGiorno().isBefore(LocalDate.now().minusDays(7)))
			.findFirst(); // Restituisce un Optional contenente l'oggetto Peso se trovato

		if (misurazioneRecente.isPresent()) {
			// Se non vuoi aggiornare, ritorna l'errore
			if (!aggiorna) {
				return PesoResult.ALREADY_INSERT;
			} 
			
			Peso pesoDaAggiornare = misurazioneRecente.get();
			int idEsistente = pesoDaAggiornare.getId();

			Peso nuovoPeso = new Peso(idEsistente, p.getCf(), pesoDouble, LocalDate.now());
			boolean ok = AdminService.aggiornaPeso(nuovoPeso);
			
			if (ok) {
				peso.remove(peso.size() - 1);
				peso.add(nuovoPeso);
				pesoField.clear();
				visualizzaGraficoPeso();
				return PesoResult.SUCCESS;
			} else {
				return PesoResult.FAILURE;
			}

		} else {
			Peso misurazione = new Peso(0, p.getCf(), pesoDouble, LocalDate.now());
			boolean ok = AdminService.creaPeso(misurazione);

			if(ok) {
				peso.add(misurazione);
				pesoField.clear();
				visualizzaGraficoPeso();
				return PesoResult.SUCCESS;
			}
			else return PesoResult.FAILURE; 
		}
	}
	@FXML
	private void handlePeso(ActionEvent event) throws IOException {
		PesoResult result = tryCreatePeso(pesoField.getText(), aggiorna);

		switch(result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Per favore, inserisci la misurazione del peso corporeo.");
			case INVALID_DATA -> MessageUtils.showError("Inserire come valore un numero con al massimo 2 cifre decimali.");
			case ALREADY_INSERT -> {
				Optional<ButtonType> conferma = MessageUtils.showConferma("Aggiorna misurazione", "Hai già inserito una misurazione per questa settimana.\nVuoi aggiornarla?");
				if(conferma.isPresent() && conferma.get() == ButtonType.OK) {
					aggiorna = true;
					handlePeso(event);
				}
				else pesoField.clear();
			}	
			case FAILURE -> MessageUtils.showError("Errore durante l'inserimento del peso corporeo.");
			case SUCCESS ->  {
				aggiorna = false;
				MessageUtils.showSuccess("Peso aggiunto con successo!");
			}
		}
	}

	// SVUOTA LISTE
	private void clearAll() {
		glicemia.clear();
		terapie.clear();
		questionari.clear();
		mailRicevute.clear();
		peso.clear();
	}

	// NAVIGAZIONE
	@FXML
	private void switchToLogin(ActionEvent event) throws IOException {
		clearAll();
		Sessione.getInstance().logout();
		Navigator.getInstance().switchToLogin(event);
	}
	
	@FXML
	private void switchToMailPage(ActionEvent event) throws IOException {
		clearAll();
		Navigator.getInstance().switchToMailPage(event);
	}
	
	@FXML
	private void switchToQuestionarioPage(ActionEvent event) throws IOException {
		clearAll();
		Navigator.getInstance().switchToQuestionarioPage(event);
	}
}