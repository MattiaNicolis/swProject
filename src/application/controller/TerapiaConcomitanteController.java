package application.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import application.model.TerapiaConcomitante;
import application.model.Utente;
import application.service.AdminService;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TerapiaConcomitanteController {
	
	private TerapiaConcomitante tc;
	private List<Utente> diabetologi = new ArrayList<>();
	
	@FXML private Label nomeLabel;
	@FXML private Label dataInizioLabel;
	@FXML private Label dataFineLabel;
	@FXML private Label modificatoLabel;
	
	@FXML
	private void initialize() {
		tc = Sessione.getInstance().getTerapiaConcomitanteSelezionata();
		
		diabetologi = AdminService.getPeopleByRole("diabetologo");

		nomeLabel.setText(tc.getNome());
		dataInizioLabel.setText(tc.getDataInizio().format(AdminService.dateFormatter));
		dataFineLabel.setText(tc.getDataFine().format(AdminService.dateFormatter));
		
		diabetologi.stream()
			.filter(d -> d.getCf().equals(tc.getModificato()))
			.findFirst()
			.ifPresent(d -> {
				modificatoLabel.setText(d.getNomeCognome() + " (" + d.getCf() + ")");
			});
	}
	
	// NAVIGAZIONE
	@FXML
	private void switchToMostraDatiPaziente(ActionEvent event) throws IOException {
		Sessione.getInstance().setTerapiaConcomitanteSelezionata(null);
		Navigator.getInstance().switchToMostraDatiPaziente(event);
	}
}
