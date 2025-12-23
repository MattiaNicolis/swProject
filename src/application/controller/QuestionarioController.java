package application.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import application.model.Questionario;
import application.model.Terapia;
import application.model.Utente;
import application.service.AdminService;
import application.utils.MessageUtils;
import application.utils.Sessione;
import application.view.Navigator;

public class QuestionarioController {

    // --- Componenti FXML ---
    @FXML private ListView<Terapia> listaTerapie;
    
    @FXML private Label lblPlaceholder;
    @FXML private VBox formContainer;
    
    @FXML private TextField nomeFarmacoField;
    @FXML private TextField doseField;
    @FXML private TextField quantitàField;
    @FXML private TextArea sintomiArea;

    // --- Dati ---
    private Terapia t; // Terapia selezionata
    private Utente p;
    private int dose, quantità;
    
    // Liste
    private List<Terapia> terapie = new ArrayList<>();
    private List<Terapia> terapieAttive = new ArrayList<>();
    private List<Terapia> terapieMancanti = new ArrayList<>();

    @FXML
    private void initialize() {
        p = Sessione.getInstance().getUtente();
        
        caricamentoDati();

        terapieAttive = trovaTerapieAttive();
        terapieMancanti = terapieDaCompletare();

        listaTerapie.setItems(FXCollections.observableArrayList(terapieMancanti));

        listaTerapie.setCellFactory(param -> new ListCell<>() {
            protected void updateItem(Terapia item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNomeFarmaco() + " (Dose: " + item.getDosiGiornaliere() + ")");
                    // setStyle("-fx-font-weight: bold;");
                }
            }
        });

        listaTerapie.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostraDettagli(newVal);
            }
        });
    }

    private void caricamentoDati() {
        terapie = AdminService.loadTerapieByPaziente(p);
    }

    private List<Terapia> trovaTerapieAttive() {
        return terapie.stream()
            .filter(ter -> !ter.getDataInizio().isAfter(LocalDate.now()) && !ter.getDataFine().isBefore(LocalDate.now()))
            .collect(Collectors.toList());
    }

    private List<Terapia> terapieDaCompletare() {
        return terapieAttive.stream()
                .filter(terapia -> !AdminService.esisteQuestionarioOggi(terapia.getId()))
                .collect(Collectors.toList());
    }

    private void mostraDettagli(Terapia selezionata) {
        t = selezionata;
        Sessione.getInstance().setTerapiaSelezionata(t);
        
        lblPlaceholder.setVisible(false);
        formContainer.setVisible(true);
    }

    public enum QuestionarioResult {
        SUCCESS,
        FAILURE,
        EMPTY_FIELDS,
        INVALID_DATA
    }
    public QuestionarioResult tryCreateQuestionario(String nomeFarmaco, String dose, String quantità, String sintomi) {
        if(nomeFarmaco == null || nomeFarmaco.isBlank() ||
            dose == null || dose.isBlank() ||
            quantità == null || quantità.isBlank()) {
            return QuestionarioResult.EMPTY_FIELDS;
        }

        try{
			this.dose = Integer.parseInt(dose);
			this.quantità = Integer.parseInt(quantità);
		} catch (NumberFormatException n) {
			return QuestionarioResult.INVALID_DATA;
		}

        if(this.dose < 1 || this.quantità < 1) return QuestionarioResult.INVALID_DATA;

        // Creazione questionario
        Questionario quest = new Questionario(0, p.getCf(), LocalDate.now(), nomeFarmaco, this.dose, this.quantità, sintomi, false, t.getId());
        boolean ok = AdminService.creaQuestionario(quest);
        
        if(ok) {
            boolean numQuest = AdminService.loadAggiornaNumQuestionari(t);
            if(numQuest)
                return QuestionarioResult.SUCCESS;

            return QuestionarioResult.FAILURE;
        } 
        else return QuestionarioResult.FAILURE;
    }
    @FXML
    private void handleQuestionario() {
        if (t == null) return;

        QuestionarioResult result = tryCreateQuestionario(nomeFarmacoField.getText(), doseField.getText(), quantitàField.getText(), sintomiArea.getText());

        switch (result) {
            case EMPTY_FIELDS -> MessageUtils.showError("Compilare i campi obbligatori: farmaco, dosi, quantità.");
            case INVALID_DATA -> MessageUtils.showError("Compilare correttamente i campi per la dose e la quantità.");
            case FAILURE -> MessageUtils.showError("Errore nel salvataggio del questionario.");
            case SUCCESS -> {
                nomeFarmacoField.clear();
                doseField.clear();
                quantitàField.clear();
                sintomiArea.clear();
                
                listaTerapie.getItems().remove(t);
                listaTerapie.getSelectionModel().clearSelection();
                t = null;
                
                formContainer.setVisible(false);
                lblPlaceholder.setVisible(true);

                if (listaTerapie.getItems().isEmpty()) {
                    lblPlaceholder.setText("Hai completato tutte le terapie per oggi!");
                }
                else {
                    lblPlaceholder.setText("Salvato! Seleziona un'altra terapia.");
                }
            }
        }
    }

    // NAVIGAZIONE
    @FXML 
    private void switchToPazientePage(ActionEvent event) throws IOException {
        Sessione.getInstance().setTerapiaSelezionata(null); // Meglio usare set null esplicito
        Navigator.getInstance().switchToPazientePage(event);
    }
}