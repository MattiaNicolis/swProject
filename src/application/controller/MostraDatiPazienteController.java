package application.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;

import application.admin.Amministratore;
import application.admin.MessageUtils;
import application.admin.Sessione;
import application.model.Glicemia;
import application.model.Utente;
import application.view.Navigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MostraDatiPazienteController {

	private Utente p;
	
	//VARIABILI
	private String scelta;
	private LocalDate date;
	private LocalDate date2;
	
	// GRAFICO
	@FXML private LineChart<String, Number> grafico;
	XYChart.Series<String, Number> serie = new XYChart.Series<>();

	
	//LABEL
	@FXML private Label labelPaziente;
	@FXML private Label dataDiNascitaDato;
	@FXML private Label sessoDato;
	@FXML private Label mailDato;
	@FXML private Label medicoRifLabel;
	@FXML private ComboBox<String> sceltaVisualizza;
	@FXML private DatePicker dataVisualizza;
	@FXML private ImageView fotoProfilo;
	
	//LISTE
	@FXML public ListView<String> listaTerapiePaziente;
	ObservableList<String> listaTerapiePazienteAsObservable = FXCollections.observableArrayList();
	
	@FXML public ListView<String> listaFattori;
	ObservableList<String> listaFattoriAsObservable = FXCollections.observableArrayList();
	
	@FXML public ListView<String> listaComorbidità;
	ObservableList<String> listaComorbiditàAsObservable = FXCollections.observableArrayList();
	
	@FXML public ListView<String> listaAllergie;
	ObservableList<String> listaAllergieAsObservable = FXCollections.observableArrayList();
	
	@FXML public ListView<String> listaPatologie;
	ObservableList<String> listaPatologieAsObservable = FXCollections.observableArrayList();
	
	@FXML public ListView<String> listaTerapieConcomitanti;
	ObservableList<String> listaTerapieConcomitantiAsObservable = FXCollections.observableArrayList();
	
	@FXML public ListView<String> listaQuestionari;
	ObservableList<String> listaQuestionariAsObservable = FXCollections.observableArrayList();
	
	@FXML
	private void initialize() {
		p = Sessione.getInstance().getPazienteSelezionato();
		
		labelPaziente.setText("Profilo clinico di " + p.getNomeCognome());
		labelPaziente.setFocusTraversable(true);
		dataDiNascitaDato.setText(p.getDataDiNascita().format(Amministratore.dateFormatter));
		sessoDato.setText(p.getSesso());
		mailDato.setText(p.getMail());

		Image image = new Image(p.getFoto());
		fotoProfilo.setImage(image);
		
		medicoRifLabel.setText("Diabetologo di riferimento: " + Amministratore.getNomeUtenteByCf(p.getDiabetologoRif()) + " (" + p.getDiabetologoRif() + ")");
			
		sceltaVisualizza.getItems().addAll("Settimana", "Mese");
		
		try {
			visualizzaDati();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void visualizzaDati() throws IOException {
		// TERAPIE
		listaTerapiePazienteAsObservable.addAll(
			Amministratore.getTerapieByCF(p.getCf()).stream()
				.map(terapia -> terapia.getNomeFarmaco() + " (" + terapia.getDataInizio() + ")")
				.toList()
		);
		listaTerapiePaziente.setItems(listaTerapiePazienteAsObservable);
		
		// ENTRA IN UNA SPECIFICA TERAPIA
		listaTerapiePaziente.setOnMouseClicked(e -> {
			String selectedTerapia = listaTerapiePaziente.getSelectionModel().getSelectedItem();
			if(selectedTerapia != null) {
				Amministratore.getTerapieByCF(p.getCf()).stream()
					.filter(terapia -> (terapia.getNomeFarmaco() + " (" + terapia.getDataInizio() + ")").equals(selectedTerapia))
					.findAny()
					.ifPresent(terapia -> {
						Sessione.getInstance().setTerapiaSelezionata(terapia);
					});
				
				try {
					Navigator.getInstance().switchToMostraDettagliTerapia(e);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
	
		// FATTORI DI RISCHIO
		listaFattoriAsObservable.addAll(
			Amministratore.getFattoriDiRischioByCF(p.getCf()).stream()
				.map(fattore -> {
		            return fattore.getNome() + " (Aggiunto da: " + Amministratore.getNomeUtenteByCf(fattore.getModificato()) + ")";
		        })
		        .toList()
		);
		listaFattori.setItems(listaFattoriAsObservable);
		
		// COMORBIDITÀ
		listaComorbiditàAsObservable.addAll(
			Amministratore.getComorbiditàByCF(p.getCf()).stream()
				.map(c -> {
		            return c.getNome() + " (Aggiunto da: " + Amministratore.getNomeUtenteByCf(c.getModificato()) + ")";
		        })
		        .toList()
		);
		listaComorbidità.setItems(listaComorbiditàAsObservable);
		
		// ALLERGIE
		listaAllergieAsObservable.addAll(
			Amministratore.getAllergieByCF(p.getCf()).stream()
				.map(a -> {
		            return a.getNome() + " (Aggiunto da: " + Amministratore.getNomeUtenteByCf(a.getModificato()) + ")";
		        })
		        .toList()
		);
		listaAllergie.setItems(listaAllergieAsObservable);
		
		// PATOLOGIE
		listaPatologieAsObservable.addAll(
			Amministratore.getPatologieByCF(p.getCf()).stream()
				.map(patologia -> patologia.getNome())
				.toList()
		);
		listaPatologie.setItems(listaPatologieAsObservable);
		
		// ENTRA IN UNA SPECIFICA PATOLOGIA
		listaPatologie.setOnMouseClicked(e -> {
			String selectedPatologia = listaPatologie.getSelectionModel().getSelectedItem();
			if(selectedPatologia != null) {
				Amministratore.getPatologieByCF(p.getCf()).stream()
					.filter(patologia -> patologia.getNome().equals(selectedPatologia))
					.findFirst()
					.ifPresent(patologia -> {
						Sessione.getInstance().setPatologiaSelezionata(patologia);
					});
				
				try {
					Navigator.getInstance().switchToMostraPatologia(e);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		// TERAPIE CONCOMITANTI
		listaTerapieConcomitantiAsObservable.addAll(
			Amministratore.getTerapieConcomitantiByCF(p.getCf()).stream()
				.map(tc -> tc.getNome() + " (" + tc.getDataInizio() + ")")
				.toList()
		);
		listaTerapieConcomitanti.setItems(listaTerapieConcomitantiAsObservable);
		
		// ENTRA IN UNA SPECIFICA TERAPIA CONCOMITANTE
		listaTerapieConcomitanti.setOnMouseClicked(e -> {
			String selectedTerapiaConcomitante = listaTerapieConcomitanti.getSelectionModel().getSelectedItem();
			if(selectedTerapiaConcomitante != null) {
				Amministratore.getTerapieConcomitantiByCF(p.getCf()).stream()
					.filter(tc -> (tc.getNome() + " (" + tc.getDataInizio() + ")").equals(selectedTerapiaConcomitante))
					.findAny()
					.ifPresent(tc -> {
						Sessione.getInstance().setTerapiaConcomitanteSelezionata(tc);
					});
				
				try {
					Navigator.getInstance().switchToMostraTerapiaConcomitante(e);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		// LISTA QUESTIONARI
		listaQuestionariAsObservable.addAll(
			Amministratore.questionari.stream()
				.filter(quest -> quest.getCf().equals(p.getCf()))
				.map(quest -> quest.getNomeFarmaco() + " (" + quest.getGiornoCompilazione() + ")")
				.toList()
			);
		listaQuestionari.setItems(listaQuestionariAsObservable);
		
		// ENTRA IN UNO SPECIFICO QUESTIONARIO
		listaQuestionari.setOnMouseClicked(e -> {
			String selectedQuestionario = listaQuestionari.getSelectionModel().getSelectedItem();
			if(selectedQuestionario != null) {
				Amministratore.questionari.stream()
					.filter(quest -> (quest.getNomeFarmaco() + " (" + quest.getGiornoCompilazione() + ")").equals(selectedQuestionario))
					.findAny()
					.ifPresent(quest -> {
						Sessione.getInstance().setQuestionarioSelezionato(quest);
					});
				
				try {
					Navigator.getInstance().switchVediQuestionario(e);
				} catch (IOException ex) {
					ex.printStackTrace();
				}	
			}
		});
	}
	
	public enum SceltaResult {
		EMPTY_FIELD,
		DATE_IN_FUTURE,
		OK
	}

	public SceltaResult tryScelta(String scelta, LocalDate date) {
		if(date == null || scelta == null) {
			return SceltaResult.EMPTY_FIELD;
		} else if(date.isAfter(LocalDate.now())) {
			return SceltaResult.DATE_IN_FUTURE;
		}
		return SceltaResult.OK;
	}

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
				} else if("Mese".equals(scelta)) {
					date2 = date.plusMonths(1);
					if(date2.isAfter(LocalDate.now()))
						date2 = LocalDate.now();
				}
					
				grafico.getData().clear(); // svuota il grafico
				serie.getData().clear();   // svuota la serie
				
				serie.getData().addAll(
					Amministratore.glicemia.stream()
					.filter(g -> g.getCf().equals(p.getCf())
								&& g.getGiorno().isAfter(date.minusDays(1))
								&& g.getGiorno().isBefore(date2.plusDays(1)))
					.sorted(Comparator.comparing(Glicemia::getGiorno))
					.map(g -> new XYChart.Data<String, Number>(g.getGiorno().toString(), g.getValore()))
					.toList()
				);
				
				grafico.getData().add(serie);
			}
		}
	}
	
	@FXML
	private void switchToDiabetologoPage(ActionEvent event) throws IOException {
		Sessione.getInstance().nullPazienteSelezionato();
		Navigator.getInstance().switchToDiabetologoPage(event);
	}
	
	@FXML
	private void switchToNuovaTerapia(ActionEvent event) throws IOException {
		Navigator.getInstance().switchToNuovaTerapia(event);
	}
	
	@FXML
	private void switchToStoriaDatiPaziente(ActionEvent event) throws IOException {
		Navigator.getInstance().switchToStoriaDatiPaziente(event);
	}
}