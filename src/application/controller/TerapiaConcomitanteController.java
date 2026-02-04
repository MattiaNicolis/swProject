package application.controller;

import java.io.IOException;

import application.model.TerapiaConcomitante;
import application.service.AdminService;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TerapiaConcomitanteController {
	
	private TerapiaConcomitante tc;
	
	@FXML private Label nomeLabel;
	@FXML private Label dataInizioLabel;
	@FXML private Label dataFineLabel;
	@FXML private Label modificatoLabel;
	
	@FXML
	private void initialize() {
		tc = Sessione.getInstance().getTerapiaConcomitanteSelezionata();
		
		nomeLabel.setText(tc.getNome());
		dataInizioLabel.setText(tc.getDataInizio().format(AdminService.dateFormatter));
		dataFineLabel.setText(tc.getDataFine().format(AdminService.dateFormatter));
		modificatoLabel.setText(AdminService.getNomeDiabetologoByCf(tc.getModificato()) + " (" + tc.getModificato() + ")");
	}
	
	// NAVIGAZIONE
	@FXML
	private void switchToMostraDatiPaziente(ActionEvent event) throws IOException {
		Sessione.getInstance().setTerapiaConcomitanteSelezionata(null);
		Navigator.getInstance().switchToMostraDatiPaziente(event);
	}
}
