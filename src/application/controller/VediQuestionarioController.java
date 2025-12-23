package application.controller;

import java.io.IOException;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import application.model.Questionario;
import application.model.Utente;
import application.service.AdminService;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class VediQuestionarioController {
    
    private Questionario q;
    private Utente u;

    // --- SEZIONE BOTTONI ---
	@FXML private Button bottoneIndietro;

    // LABEL
    @FXML private Label nomeFarmacoLabel;
    @FXML private Label dosiGiornaliereLabel;
    @FXML private Label quantitàLabel;
    @FXML private Label sintomiLabel;
    @FXML private Label dataQuestionarioLabel;

    @FXML
    private void initialize() {
        q = Sessione.getInstance().getQuestionarioSelezionato();
        u = Sessione.getInstance().getUtente();

        // Inizializza i campi della UI con i dati del questionario
        dataQuestionarioLabel.setText("Questionario del giorno: " + q.getGiornoCompilazione().format(AdminService.dateFormatter));
        nomeFarmacoLabel.setText(q.getNomeFarmaco());
        dosiGiornaliereLabel.setText(String.valueOf(q.getDosiGiornaliere()));
        quantitàLabel.setText(String.valueOf(q.getQuantità()));
        if(q.getSintomi() != null) {
            sintomiLabel.setText(q.getSintomi());
        }
        else {
            sintomiLabel.setText("---");
        }
    }

    // NAVIGAZIONE
    @FXML
    private void indietro(ActionEvent event) throws IOException {
        Sessione.getInstance().setQuestionarioSelezionato(null);
        if(u.isDiabetologo()) {
            Navigator.getInstance().switchToMostraDatiPaziente(event);
        }
        else if(u.isPaziente()) {
            Navigator.getInstance().switchToPazientePage(event);
        }
    }
}
