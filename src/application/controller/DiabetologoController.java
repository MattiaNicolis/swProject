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
	private List<Terapia> terapie = new ArrayList<>();
	private List<Questionario> questNonConformi = new ArrayList<>();
	private List<Mail> mailRicevute = new ArrayList<>();
	private List<Utente> pazienti = new ArrayList<>();
	private Map<String, Integer> pazientiNonCompilano = new HashMap<>();
	
	// FXML
	@FXML private Label welcomeLabel;
	@FXML private ListView<Utente> listaNomiPazienti;
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
		d = Sessione.getInstance().getUtente();
		
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
		pazienti = AdminService.getPeopleByRole("paziente");
	}
	
	private void setUpInterfaccia() {
		welcomeLabel.setText("Ciao, " + d.getNomeCognome());
		welcomeLabel.setFocusTraversable(true);

		if("M".equals(d.getSesso()))
			nomeLabel.setText("Dr. " + d.getNomeCognome());
		else if("F".equals(d.getSesso()))
			nomeLabel.setText("Dr.ssa " + d.getNomeCognome());

		//Creo la label ddnLabel fatta da "Data di nascita" (in grassetto) + d.getDataDiNascita (normale)
		Text textDataNascita = new Text("Data di nascita:");
		textDataNascita.setStyle("-fx-font-weight: bold;");
		Text dataNascita = new Text(" " + d.getDataDiNascita().format(AdminService.dateFormatter));
		TextFlow ddn = new TextFlow(textDataNascita, dataNascita);

		ddnLabel.setText(null);
		ddnLabel.setGraphic(ddn);

		//Creo la label luogoLabel fatta da "Luogo di nascita" (in grassetto) + d.getLuogoDiNascita (normale)
		Text textLuogoNascita = new Text("Luogo di nascita:");
		textLuogoNascita.setStyle("-fx-font-weight: bold;");
		Text luogoNascita = new Text(" " + d.getLuogoDiNascita());
		TextFlow luogo = new TextFlow(textLuogoNascita, luogoNascita);

		luogoLabel.setText(null);
		luogoLabel.setGraphic(luogo);

		//Creo la label sessoLabel fatta da "Sesso" (in grassetto) + d.getSesso (normale)
		Text textSesso = new Text("Sesso:");
		textSesso.setStyle("-fx-font-weight: bold;");
		Text sesso = new Text(" " + d.getSesso());
		TextFlow sex = new TextFlow(textSesso, sesso);

		sessoLabel.setText(null);
		sessoLabel.setGraphic(sex);

		mailButton.setText(AdminService.contatoreMailNonLette(mailRicevute) > 0 ? AdminService.contatoreMailNonLette(mailRicevute) + " Mail" : "🖂 Mail");
		mailButton.setStyle(AdminService.contatoreMailNonLette(mailRicevute) > 0 ? "-fx-text-fill: red;" : "-fx-text-fill: white;");
	}

	private void setupListaPazienti(){
		for(Utente p : pazienti) {
			pazientiNonCompilano.put(p.getCf(), DiabetologoUtils.calcolaGiorniNonCompilati(p));
		}


		ObservableList<Utente> listaNomiPazientiAsObservable = FXCollections.observableArrayList(
				pazienti.stream()
			        .sorted(Comparator.comparing(Utente::getNomeCognome)) // ordine alfabetico
			        .toList()
			);
		listaNomiPazienti.setItems(listaNomiPazientiAsObservable);
		
		listaNomiPazienti.setCellFactory(e -> new ListCell<Utente>() {
		    protected void updateItem(Utente paziente, boolean empty) {
		        super.updateItem(paziente, empty);
		        
		        if (empty || paziente == null) {
		            setText(null);
		            setStyle("");
		        } else {
		        	int giorni = pazientiNonCompilano.get(paziente.getCf());

					if(giorni >= 3) {
						setText(paziente.getNomeCognome() + "\n" + paziente.getCf() + "\n(Non compila da 3 + giorni!)");
						// Rosso vivo + Grassetto
						setStyle("-fx-background-color: #ffcccc; -fx-text-fill: black; -fx-font-weight: bold;");
					}
					else {
						Text nomeCognomePaziente = new Text(paziente.getNomeCognome() + "\n");
						nomeCognomePaziente.setStyle("-fx-font-weight: bold;");

						Text cfPaziente = new Text(paziente.getCf());

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
						Sessione.getInstance().setPazienteSelezionato(paziente);
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
						.ifPresent(p -> setText(p.getNomeCognome() + " (" + p.getCf() + "): " + g.getValore()));

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
		                    AdminService.getNomeUtenteByCf(pazienti, quest.getCf()));
		            
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

			String nomePaziente = AdminService.getNomeUtenteByCf(pazienti, selectedQuest.getCf());
			Optional<ButtonType> result = MessageUtils.showAlertQuest(selectedQuest, nomePaziente);

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
					Utente paziente = AdminService.getUtenteByCf(pazienti, selectedQuest.getCf());

					try {
						Navigator.getInstance().switchToRispondi(e, paziente.getMail(), "Questionario del " + selectedQuest.getGiornoCompilazione() + " non conforme");
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
	}
		
	private void clearAll() {
		glicemia.clear();
		terapie.clear();
		questNonConformi.clear();
		mailRicevute.clear();
	}

	// NAVIGAZIONE
	@FXML
	private void switchToLogin(ActionEvent event) throws IOException {
		clearAll();
		Sessione.getInstance().logout();
		Navigator.getInstance().switchToLogin(event);
	}
	
	@FXML
	private void switchToMailPage(ActionEvent event) throws IOException {
		clearAll();
		Navigator.getInstance().switchToMailPage(event);
	}
}