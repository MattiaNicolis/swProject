package application.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import application.admin.Amministratore;
import application.admin.Database;
import application.admin.MessageUtils;
import application.admin.Sessione;
import application.model.Glicemia;
import application.model.Utente;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PazienteController {

	private Utente u;
	
	// FXML
	@FXML private LineChart<String, Number> graficoGlicemia;
	@FXML private TextField valoreField;
	@FXML private TextField oraField;
	@FXML private TextField minutiField;
	@FXML private ComboBox<String> indicazioniBox;
	@FXML private Label welcomeLabel;
	@FXML private Label statoQuestionarioOdierno;
	@FXML private Button mailButton;
	@FXML private Button questButton;

	// LABEL - PROFILO
	@FXML private Label nomeLabel;
	@FXML private Label ddnLabel;
	@FXML private Label sessoLabel;
	@FXML private Label diabetologoLabel;
	@FXML private ImageView fotoProfilo;

	// LABEL - TERAPIA CORRENTE
	@FXML private Label terapiaCorrente;
	@FXML private Label nomeFarmacoLabel;
	@FXML private Label dosiGiornaliereLabel;
	@FXML private Label quantitàLabel;
	@FXML private Label periodoLabel;
	@FXML private Label indicazioniLabel;
	@FXML private Label modificatoLabel;

	// VARIABILI
	LocalDate giorno;
	boolean compilato = false;
	boolean terapiaInCorso = false;

	
	@FXML
	private void initialize() throws IOException {
		u = Sessione.getInstance().getUtente();
		
		welcomeLabel.setText("Ciao, " + u.getNomeCognome());

		nomeLabel.setText("Nome e cognome: " + u.getNomeCognome());
		ddnLabel.setText("Data di nascita: " + u.getDataDiNascita());
		sessoLabel.setText("Sesso: " + u.getSesso());
		diabetologoLabel.setText("Diabetologo di riferimento: " + Amministratore.getNomeUtenteByCf(u.getDiabetologoRif()));
		Image image = new Image(u.getFoto());
		fotoProfilo.setImage(image);

		Amministratore.getTerapieByCF(u.getCf()).stream()
			.filter(t -> !t.getDataInizio().isAfter(LocalDate.now()) && !t.getDataFine().isBefore(LocalDate.now()))
			.findFirst()
			.ifPresentOrElse(t -> {
				terapiaCorrente.setText("Terapia corrente:");
				nomeFarmacoLabel.setText(t.getNomeFarmaco());
				dosiGiornaliereLabel.setText(String.valueOf(t.getDosiGiornaliere()));
				quantitàLabel.setText(String.valueOf(t.getQuantità()));
				periodoLabel.setText(t.getDataInizio().format(Amministratore.dateFormatter) + " - " + t.getDataFine().format(Amministratore.dateFormatter));
				if(t.getIndicazioni() != null && !t.getIndicazioni().isBlank())
					indicazioniLabel.setText(t.getIndicazioni());
				else
					indicazioniLabel.setText("Nessuna indicazione.");
				modificatoLabel.setText(Amministratore.getNomeUtenteByCf(t.getDiabetologo()));
			}, () -> {
				terapiaCorrente.setText("Nessuna terapia in corso");
				nomeFarmacoLabel.setText("-");
				dosiGiornaliereLabel.setText("-");
				quantitàLabel.setText("-");
				periodoLabel.setText("----/--/--" + " - " + "----/--/--");
				indicazioniLabel.setText("-");
				modificatoLabel.setText("-");
			});

		graficoGlicemia.setFocusTraversable(true);
		
		
		Amministratore.questionari.stream()
			.filter(q -> q.getCf().equals(u.getCf())
						&& q.getGiornoCompilazione().equals(LocalDate.now()))
			.findFirst()
			.ifPresent(e -> {
				statoQuestionarioOdierno.setText("Questionario odierno compilato!");
				compilato = true;
				questButton.setDisable(true);
			});
		
		Amministratore.getTerapieByCF(u.getCf()).stream()
			.filter(t -> ((t.getDataInizio().isBefore(LocalDate.now()) || t.getDataInizio().isEqual(LocalDate.now())) 
						&& ((t.getDataFine().isAfter(LocalDate.now())) || t.getDataFine().isEqual(LocalDate.now()))))
			.findAny()
			.ifPresent(e -> {
				if(compilato == false) {
					statoQuestionarioOdierno.setText("Questionario odierno da compilare!");
				}
				terapiaInCorso = true;
			});
		
		if(terapiaInCorso == false) {
			questButton.setDisable(true);
			statoQuestionarioOdierno.setText("Nessun questionario da compilare!");
		}
			
		mailButton.setText(Amministratore.contatoreMailNonLette() > 0 ? Amministratore.contatoreMailNonLette() + " Mail" : "Mail");
	    mailButton.setStyle(Amministratore.contatoreMailNonLette() > 0 ? "-fx-text-fill: red;" : "-fx-text-fill: black;");
	    
		indicazioniBox.getItems().clear();
		indicazioniBox.getItems().addAll("Pre pasto", "Post pasto");

	    visualizzaGrafico();
	    javafx.application.Platform.runLater(() -> notificaTerapia());
	}
	
	private void notificaTerapia() {
		Amministratore.getTerapieByCF(u.getCf()).stream()
			.filter(t -> !t.getVisualizzata() && !t.getDataInizio().isAfter(LocalDate.now()))
			.findFirst()
			.ifPresent(t -> {
				Optional<ButtonType> result = MessageUtils.showConferma("Inizio terapia", "È iniziata una nuova terapia: " + t.getNomeFarmaco());

				if (result.isPresent() && result.get() == ButtonType.OK) {
					String query = "UPDATE terapie SET visualizzata = ? WHERE id = ?";
					try (Connection conn = Database.getConnection();
						PreparedStatement stmt = conn.prepareStatement(query)) {

						stmt.setBoolean(1, true);
						stmt.setInt(2, t.getId());

						int rows = stmt.executeUpdate();

						if (rows > 0) {
							Amministratore.loadTerapieFromDatabase();
						} else {
							MessageUtils.showError("Errore: nessuna terapia trovata da aggiornare.");
						}

					} catch (SQLException e) {
						e.printStackTrace();
						MessageUtils.showError("Errore nel salvataggio della notifica nel database.");
					}
				}
			});
	}
	
	private void visualizzaGrafico() {
	    XYChart.Series<String, Number> serie = new XYChart.Series<>();
	    serie.setName("Glicemia giornaliera");

	    for(Glicemia glicemia : Amministratore.glicemia) {
	    	if(glicemia.getCf().equals(u.getCf()) && glicemia.getGiorno().isEqual(LocalDate.now())) {
	    		
	    		final int valore = glicemia.getValore();
	            final String orario = glicemia.getOrario();
	            final String indicazioni = glicemia.getIndicazioni();
	            //final LocalDate giorno = glicemia.getGiorno();
	    		
	    		XYChart.Data<String, Number> punto = new XYChart.Data<>(orario, valore);
	    		
	    		punto.nodeProperty().addListener((obs, oldNode, newNode) -> {
	                if (newNode != null) {
	                    if(indicazioni.equals("Pre pasto")) {
	                    	if(valore < 80 || valore > 130)
		                        newNode.setStyle("-fx-background-color: red;");
		                    else
		                        newNode.setStyle("-fx-background-color: green;");
	                    } else if(indicazioni.equals("Post pasto")) {
	                    	if(valore > 180)
	                    		newNode.setStyle("-fx-background-color: red;");
	                    	else
	                    		newNode.setStyle("-fx-background-color: green;");
	                    }
	                }
	            });
	    		
	    		serie.getData().add(punto);
	    	}
	    }
	    
	    graficoGlicemia.getData().clear(); //cancella la precedente
        graficoGlicemia.getData().add(serie);
	}
	
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
		try {
			oraInt = Integer.parseInt(ora);
	        minutiInt = Integer.parseInt(minuti);
	        valoreInt = Integer.parseInt(valore);
		} catch (NumberFormatException e) {
	        return GlicemiaResult.INVALID_DATA;
	    }

		if (oraInt < 0 || oraInt > 23) {
	        return GlicemiaResult.INVALID_DATA;
	    }
		if (ora.length() == 1) ora = "0" + ora;

	    if (minutiInt < 0 || minutiInt > 59) {
			return GlicemiaResult.INVALID_DATA;
	    }
	    if (minuti.length() == 1) minuti = "0" + minuti;

		String orario = ora + ":" + minuti;
	    giorno = LocalDate.now();

	    Glicemia glicemia = new Glicemia(u.getCf(), valoreInt, giorno, orario, indicazioni);
		boolean ok = Amministratore.glicemiaDAO.creaGlicemia(glicemia);
		if(ok) {
			if (!graficoGlicemia.getData().isEmpty()) {
				XYChart.Series<String, Number> serie = graficoGlicemia.getData().get(0);

				XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(orario, valoreInt);

				dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
					if (newNode != null) {
						if("Pre pasto".equals(indicazioni)) {
							if(valoreInt < 80 || valoreInt > 130)
								newNode.setStyle("-fx-background-color: red;");
							else
								newNode.setStyle("-fx-background-color: green;");
						} else if("Post pasto".equals(indicazioni)) {
							if(valoreInt > 180)
								newNode.setStyle("-fx-background-color: red;");
							else
								newNode.setStyle("-fx-background-color: green;");
						}
					}
				});

				serie.getData().add(dataPoint);
			}
			valoreField.clear();
			oraField.clear();
			minutiField.clear();
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
				indicazioniBox.getItems().clear();
				indicazioniBox.getItems().addAll("Pre pasto", "Post pasto");
			}
		}
	}
	
	// NAVIGAZIONE
	@FXML
	private void switchToLogin(ActionEvent event) throws IOException {
		Sessione.getInstance().logout();
		Navigator.getInstance().switchToLogin(event);
	}
	
	@FXML
	private void switchToMailPage(ActionEvent event) throws IOException {
		Navigator.getInstance().switchToMailPage(event);
	}
	
	@FXML
	private void switchToQuestionarioPage(ActionEvent event) throws IOException {
		Navigator.getInstance().switchToQuestionarioPage(event);
	}
}