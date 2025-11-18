package application.controller;

import java.io.IOException;
import javafx.scene.control.Label;

import application.admin.Sessione;
import application.model.Questionario;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class VediQuestionarioController {
    
    private Questionario q;

    // LABEL
    @FXML private Label nomeFarmacoLabel;
    @FXML private Label dosiGiornaliereLabel;
    @FXML private Label quantitàLabel;
    @FXML private Label sintomiLabel;

    @FXML
    private void initialize() {
        q = Sessione.getInstance().getQuestionarioSelezionato();
        // Inizializza i campi della UI con i dati del questionario
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

    @FXML
    private void switchToMostraDatiPaziente(ActionEvent event)  throws IOException {
        Sessione.getInstance().nullQuestionarioSelezionato();
        Navigator.getInstance().switchToMostraDatiPaziente(event);
    }
}