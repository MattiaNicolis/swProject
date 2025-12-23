package application.controller;

import java.io.IOException;
import application.model.Mail;
import application.model.Utente;
import application.service.AdminService;
import application.utils.MessageUtils;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class VediMailController {
	
	private Utente u;
	private Mail m;
	
	// FXML
	@FXML private Label mittenteLabel;
	@FXML private Label destinatarioLabel;
	@FXML private Label oggettoLabel;
	@FXML private Label corpoLabel;
	@FXML private Label giornoOraLabel;
	@FXML Button rispondiButton;
	
	@FXML
	private void initialize() throws IOException {
		u = Sessione.getInstance().getUtente();
		m = Sessione.getInstance().getMailSelezionata();

		if (m == null) {
            MessageUtils.showError("Errore nel caricamento della mail.");
            return;
        }
		
		setUpInterfaccia();
		gestioneLettura();
	}
	
	private void setUpInterfaccia() {
		mittenteLabel.setText(m.getMittente());
		destinatarioLabel.setText(m.getDestinatario());
		oggettoLabel.setText(m.getOggetto());
		corpoLabel.setText(m.getCorpo());
		giornoOraLabel.setText(m.getGiorno().format(AdminService.dateFormatter) + ", " + m.getOrario().format(AdminService.timeFormatter));

		if(m.getMittente().equals(u.getMail())) { // se inviata da me
			rispondiButton.setDisable(true);
		} 
	}

	private void gestioneLettura() {
		if(!m.getLetta() && m.getDestinatario().equalsIgnoreCase(u.getMail())) {
            boolean ok = AdminService.vediMail(m);
            if(ok) {
                m.setLetta(true);
            }
        }
	}

	// NAVIGAZIONE
	@FXML
	private void switchToMailPage(ActionEvent event) throws IOException {
		Sessione.getInstance().setMailSelezionata(null);
		Navigator.getInstance().switchToMailPage(event);
	}
	
	@FXML
	private void switchToRispondi(Event event) throws IOException {
		Sessione.getInstance().setMailSelezionata(null);
		Navigator.getInstance().switchToRispondi(event, m.getMittente(), m.getOggetto());
	}
}
