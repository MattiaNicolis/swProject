package application.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import application.model.Glicemia;
import application.model.Mail;
import application.model.Paziente;
import application.model.Questionario;
import application.model.Terapia;
import application.model.Utente;
import application.service.AdminService;
import application.utils.MessageUtils;
import application.utils.Sessione;
import application.utils.DiabetologoUtils;
import application.view.Navigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class DiabetologoController {
		
	// VARIABILI
	private Utente d;
	private List<Glicemia> glicemia = new ArrayList<>();
	private List<Questionario> questNonConformi = new ArrayList<>();
	private List<Mail> mailRicevute = new ArrayList<>();
	private List<Paziente> pazienti = new ArrayList<>();
	private Map<String, Integer> pazientiNonCompilano = new HashMap<>();
	
	// FXML
	@FXML private Label welcomeLabel;
	@FXML private ListView<Paziente> listaNomiPazienti;
	@FXML private ListView<Questionario> listaNotificheQuestionario;
	@FXML private ListView<Glicemia> listaGlicemieSballate;
	@FXML private ListView<Questionario> listaQuestNonConformi;
	@FXML private Button mailButton;

	// LABEL - PROFILO
	@FXML private Label nomeLabel;
	@FXML private Label ddnLabel;
	@FXML private Label sessoLabel;
	@FXML private Label luogoLabel;

	
	@FXML
	private void initialize() {
		d = Sessione.getInstance().getDiabetologo();
		
		caricaDatiDiabetologo();
		setUpInterfaccia();

		setupListaPazienti();
		setupListaGlicemieSballate();
		setupListaQuestionariNonConformi();

	} // FINE INITIALIZE

	private void caricaDatiDiabetologo() {
		glicemia = AdminService.loadAllGlicemia();
		questNonConformi = AdminService.loadQuestionariNonConformi();
		mailRicevute = AdminService.loadMailRicevute(d);
		pazienti = AdminService.getPazienti();
	}
	
	private void setUpInterfaccia() {
		welcomeLabel.setText("Ciao, " + d.getNome() + " " + d.getCognome());
		welcomeLabel.setFocusTraversable(true);

		if("M".equals(d.getSesso()))
			nomeLabel.setText("Dr. " + d.getCognome());
		else if("F".equals(d.getSesso()))
			nomeLabel.setText("Dr.ssa " + d.getCognome());

		ddnLabel.setText(d.getDataDiNascita().format(AdminService.dateFormatter));
		luogoLabel.setText(d.getLuogoDiNascita());
		sessoLabel.setText(d.getSesso());

		mailButton.setText(AdminService.contatoreMailNonLette(mailRicevute) > 0 ? AdminService.contatoreMailNonLette(mailRicevute) + " Mail" : "🖂 Mail");

		mailButton.getStyleClass().removeAll("btn-mail", "btn-mail-alert");

		if (AdminService.contatoreMailNonLette(mailRicevute) > 0) {
			mailButton.getStyleClass().add("btn-mail-alert");
		} else {
			mailButton.getStyleClass().add("btn-mail");
		}
	}

	private void setupListaPazienti(){
		for(Utente p : pazienti) {
			pazientiNonCompilano.put(p.getCf(), DiabetologoUtils.calcolaGiorniNonCompilati(p));
		}


		ObservableList<Paziente> listaNomiPazientiAsObservable = FXCollections.observableArrayList(
				pazienti.stream()
			        .sorted(Comparator.comparing(Paziente::getCognome)) // ordine alfabetico
			        .toList()
			);
		listaNomiPazienti.setItems(listaNomiPazientiAsObservable);
		
		listaNomiPazienti.setCellFactory(e -> new ListCell<Paziente>() {
		    protected void updateItem(Paziente paziente, boolean empty) {
		        super.updateItem(paziente, empty);
		        
		        if (empty || paziente == null) {
		            setText(null);
		            setStyle("");
		        } else {
		        	int giorni = pazientiNonCompilano.get(paziente.getCf());

					if(giorni >= 3) {
						setText(paziente.getNome() + " " + paziente.getCognome() + " (" + paziente.getCf() + ")\n(Non compila da 3+ giorni!)");
						// Rosso vivo + Grassetto
						setStyle("-fx-background-color: #ffcccc; -fx-text-fill: black; -fx-font-weight: bold;");
					}
					else {
						Text nomeCognomePaziente = new Text(paziente.getNome() + " " + paziente.getCognome());
						nomeCognomePaziente.setStyle("-fx-font-weight: bold;");

						Text cfPaziente = new Text(" (" + paziente.getCf() + ")");

						TextFlow cellFactoryPaziente = new TextFlow(nomeCognomePaziente, cfPaziente);

						setText(null);
						setGraphic(cellFactoryPaziente);
					}
		        }
		    }
		});
		
		listaNomiPazienti.setOnMouseClicked(e -> {
			Utente selectedPaziente = listaNomiPazienti.getSelectionModel().getSelectedItem();
			if(selectedPaziente != null) {
				String cf = selectedPaziente.getCf();
				pazienti.stream()
					.filter(paziente -> paziente.getCf().equals(cf))
					.findFirst()
					.ifPresent(paziente -> {
						Sessione.getInstance().setPaziente(paziente);
					});
				
				try {
					Navigator.getInstance().switchToMostraDatiPaziente(e);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
	}
	
	private void setupListaGlicemieSballate() {
		ObservableList<Glicemia> listaGlicemieSballateAsObservable = FXCollections.observableArrayList(
			glicemia.stream()
				// Filtra: Oggi o Ieri
				.filter(g -> g.getGiorno().isEqual(LocalDate.now()) || g.getGiorno().isEqual(LocalDate.now().minusDays(1)))
				// Usa il metodo helper per filtrare! Se ritorna un colore, vuol dire che è sballata.
				.filter(g -> DiabetologoUtils.getColoreSeverita(g) != null) 
				.toList()
		);
		
		listaGlicemieSballate.setItems(listaGlicemieSballateAsObservable);
		
		listaGlicemieSballate.setCellFactory(e -> new ListCell<Glicemia>() {
			protected void updateItem(Glicemia g, boolean empty) {
				super.updateItem(g, empty);
				
				if (empty || g == null) {
					setText(null);
					setStyle("");
				} else {
					// Recupero nome paziente (codice invariato)
					pazienti.stream()
						.filter(p -> p.getCf().equals(g.getCf()))
						.findFirst()
						.ifPresent(p -> setText(p.getNome() + " " + p.getCognome() + " (" + p.getCf() + "): " + g.getValore()));

					// RIUSO LA LOGICA DEL COLORE QUI
					String color = DiabetologoUtils.getColoreSeverita(g);
					if(color != null) {
						setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
					} else {
						setStyle("");
					}
				}
			}
		});
	}
	
	private void setupListaQuestionariNonConformi() {

    	listaQuestNonConformi.setItems(FXCollections.observableArrayList(questNonConformi));
    
		listaQuestNonConformi.setCellFactory(e -> new ListCell<Questionario>() {
		    protected void updateItem(Questionario quest, boolean empty) {
		        super.updateItem(quest, empty);
		        
		        if (empty || quest == null) {
		            setText(null);
		            setStyle("");
		        } else {
		            setText("Questionario del " + quest.getGiornoCompilazione().format(AdminService.dateFormatter) +
		                    "\nnon conforme alla terapia di\n" +
		                    AdminService.getNomePazienteByCf(pazienti, quest.getCf()));
		            
		            if (!quest.getControllato()) {
		                // NON CONTROLLATO: grassetto + sfondo azzurrino
		                setStyle("-fx-font-weight: bold; -fx-background-color: #f0f8ff;");
		            } else {
		                setStyle("");
		            }
		        }
		    }
		});
		
		listaQuestNonConformi.setOnMouseClicked(e -> {
			Questionario selectedQuest = listaQuestNonConformi.getSelectionModel().getSelectedItem();
			
			// Evita click su celle vuote o null
			if (selectedQuest == null) return; 

			String nomePaziente = AdminService.getNomePazienteByCf(pazienti, selectedQuest.getCf());
			Terapia terapia = AdminService.getTerapiaById(selectedQuest.getTerapiaId());
			Optional<ButtonType> result = MessageUtils.showAlertQuest(selectedQuest, terapia, nomePaziente);

			if (result.isPresent()) {
				if (result.get() == MessageUtils.BTN_VISTO) {
					boolean ok = AdminService.segnaComeControllato(selectedQuest);
					
					if (ok) {
						selectedQuest.setControllato(true);
        				listaQuestNonConformi.refresh();
						MessageUtils.showSuccess("Questionario segnato come controllato.");
					} else {
						MessageUtils.showError("Errore durante l'aggiornamento del database.");
					}

				} else if (result.get() == MessageUtils.BTN_MAIL) {
					// Impostiamo comunque il paziente e questionario selezionato in sessione
					Paziente paziente = AdminService.getPazienteByCf(pazienti, selectedQuest.getCf());

					try {
						Navigator.getInstance().switchToRispondi(e, paziente.getMail(), "Questionario del " + selectedQuest.getGiornoCompilazione().format(AdminService.dateFormatter) + " non conforme");
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
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
}