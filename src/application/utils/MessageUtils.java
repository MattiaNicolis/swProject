package application.utils;

import java.util.Optional;

import application.model.Questionario;
import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;

public class MessageUtils {

    public static final ButtonType BTN_VISTO = new ButtonType("Segna come Visto");
    public static final ButtonType BTN_MAIL = new ButtonType("Invia Mail");
    public static final ButtonType BTN_ANNULLA = new ButtonType("Annulla", ButtonData.CANCEL_CLOSE);

    public static void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();

        // Dopo tot secondi chiude automaticamente la finestra
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> alert.close());
        delay.play();
    }
    
    public static Optional<ButtonType> showConferma(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showAlertQuest(Questionario q, String nomePaziente) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Gestione Non Conformità");
        alert.setHeaderText("Questionario di " + nomePaziente + " del " + q.getGiornoCompilazione());
        alert.setContentText("Il paziente ha segnalato valori non conformi alla terapia.\n\nCosa vuoi fare?");

        alert.getButtonTypes().setAll(BTN_VISTO, BTN_MAIL, BTN_ANNULLA);

        return alert.showAndWait();
    }
}
