package application.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.model.Diabetologo;
import application.model.Mail;
import application.model.Paziente;
import application.model.Utente;
import application.service.AdminService;
import application.utils.MessageUtils;
import application.utils.Sessione;
import application.view.Navigator;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class MailController {
	
	// Variabili
	private Utente u;
	private List<Mail> mailRicevute = new ArrayList<>();
	private List<Mail> mailInviate = new ArrayList<>();
	private List<Diabetologo> diabetologi = new ArrayList<>();
	private List<Paziente> pazienti = new ArrayList<>();
	private Map<String, String> emailToNameMap = new HashMap<>();
	private FilteredList<Mail> currentFilteredList;

	// FXML
	@FXML private VBox scriviPanel;
	@FXML private Button bottoneIndietro;
	@FXML private TextField searchMailBar;
	@FXML private TextField destinatarioField;
	@FXML private TextField oggettoField;
	@FXML private TextArea corpoArea;
	@FXML private ListView<Mail> listaMail;
	
	// LABEL
	@FXML private Label mailNonLette;
	@FXML private Label mailDiabetologo;
	@FXML private Label labelDiabetologo;
	@FXML private Label headerLabel;
	
	@FXML private void initialize() throws IOException{
		// Recupero utente in sessione
		if(Sessione.getInstance().getPaziente() != null) {
			u = Sessione.getInstance().getPaziente();
		} else if (Sessione.getInstance().getDiabetologo() != null) {
			u = Sessione.getInstance().getDiabetologo();
		}

		if (u == null) {
            MessageUtils.showError("Errore di sessione: Utente non trovato.");
            return;
        }
		
		// Caricamento dati dal database
		caricaDati();

		// setup interfaccia
		setupInterface();
	}

	private void caricaDati() {
		mailInviate = AdminService.loadMailInviate(u);
		mailRicevute = AdminService.loadMailRicevute(u);
		// GetUtenteInfo per mappatura email-nome, senza prendere tutti i dati dal database
		diabetologi = AdminService.getDiabetologoInfo();
		// Se utente è diabetologo, carica anche i pazienti per la mappatura
		if(u instanceof Diabetologo) {
			pazienti = AdminService.getPazienteInfo();
			populateNameMapP(pazienti);
		}
		populateNameMapD(diabetologi);
	}

	private void populateNameMapP(List<Paziente> utenti) {
        if(utenti == null) return;
		// Associa mail a nome utente
        for(Paziente utente : utenti) {
            emailToNameMap.put(utente.getMail(), utente.getNome() + " " + utente.getCognome());
        }
    }

	private void populateNameMapD(List<Diabetologo> utenti) {
        if(utenti == null) return;
		// Associa mail a nome utente
        for(Diabetologo utente : utenti) {
            emailToNameMap.put(utente.getMail(), utente.getNome() + " " + utente.getCognome());
        }
    }
	
	private void setupInterface() {
		// Se utente è paziente, mostra mail diabetologo di riferimento
        if (u instanceof Paziente) {
			Paziente p = (Paziente) u; 
        
        	String mail = AdminService.getMailDiabetologoRif(p.getDiabetologoRif());
        
        	destinatarioField.setText(mail);
        	mailDiabetologo.setText(mail);
        } else {
            mailDiabetologo.setVisible(false);
            labelDiabetologo.setVisible(false);
        }

		searchMailBar.textProperty().addListener((obs, oldVal, newVal) -> updateFilter(newVal));

		// Mail ricevute di default nell'interfaccia
		showMailRicevute(null);
		
		mailNonLette.setText("Non lette: " + AdminService.contatoreMailNonLette(mailRicevute));
		
		// Visualizza Mail al click
		listaMail.setOnMouseClicked(e -> {
			Mail selectedMail = listaMail.getSelectionModel().getSelectedItem();
			if(selectedMail != null) {
				try {
					Sessione.getInstance().setMailSelezionata(selectedMail);
					Navigator.getInstance().switchToVediMail(e);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
    }

	private enum MailResult {
		EMPTY_FIELDS,
		INVALID_DATA,
		SUCCESS,
		FAILURE
	}
	
	// Logica invio mail
	private MailResult trySendMail(String destinatario, String oggetto, String corpo) {
		if(destinatario == null || destinatario.isBlank() || oggetto == null || oggetto.isBlank()
			|| corpo == null || corpo.isBlank()) {
			return MailResult.EMPTY_FIELDS;
		}

		if (!emailToNameMap.containsKey(destinatario)) {
             return MailResult.INVALID_DATA;
        }

		Mail mail = new Mail(0, u.getMail(), destinatario, oggetto, corpo, null, null, false);
		boolean ok = AdminService.scriviMail(mail);
		if(ok) {
			return MailResult.SUCCESS;
		}
		else {
			return MailResult.FAILURE;
		}
	}

	// Invio mail parte grafica
	@FXML
	private void handleMail(ActionEvent event) throws IOException {
		MailResult result = trySendMail(destinatarioField.getText(), oggettoField.getText(), corpoArea.getText());

		switch(result) {
			case EMPTY_FIELDS -> MessageUtils.showError("Compilare tutti i campi.");
			case INVALID_DATA -> {
				destinatarioField.clear();
				MessageUtils.showError("Mail destinatario non valida.");
			}
			case FAILURE -> MessageUtils.showError("Errore nell'invio della mail.");
			case SUCCESS -> {
				mailInviate = AdminService.loadMailInviate(u);
				MessageUtils.showSuccess("Mail inviata!");
				hideCompose();
			}
		}
		
	}
	
	// Visualizza mail ricevute
	@FXML
    private void showMailRicevute(ActionEvent e) {
        setupListView(mailRicevute, false);
		headerLabel.setText("Posta arrivata");
    }

	// Visualizza mail inviate
    @FXML
    private void showMailInviate(ActionEvent e) {
        setupListView(mailInviate, true);
		headerLabel.setText("Posta inviata");
    }

	// Configura ListView per visualizzare le mail
	private void setupListView(List<Mail> listaSorgente, boolean isInviata) {
        searchMailBar.clear();
        currentFilteredList = new FilteredList<>(FXCollections.observableArrayList(listaSorgente), p -> true);
        listaMail.setItems(currentFilteredList);

        listaMail.setCellFactory(event -> new ListCell<Mail>() {
            @Override
            protected void updateItem(Mail mail, boolean empty) {
                super.updateItem(mail, empty);
                
                if (empty || mail == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String targetEmail = isInviata ? mail.getDestinatario() : mail.getMittente();
                    
					// Ottieni nome tramite mappa mail - nome
                    String displayName = emailToNameMap.getOrDefault(targetEmail, targetEmail);

					// Anteprima del corpo della mail
                    String preview = mail.getCorpo().split("\n")[0];
                    if (preview.length() > 30) preview = preview.substring(0, 30) + "...";

                    String stato = "";
                    String stile = "";

					// Stile e stato mail
                    if (!mail.getLetta() && !isInviata) {
                        stile = "-fx-font-weight: bold; -fx-background-color: #f0f8ff;";
                    } else if (isInviata && !mail.getLetta()) {
                        stato = " (Non Letta)";
                    } else if (isInviata && mail.getLetta()) {
						stato = " (Letta)";
					}

                    setText(displayName + "\nOggetto: " + mail.getOggetto() + "\n" + preview + stato);
                    setStyle(stile);
                }
            }
        });
    }
	
	// Filtro ricerca mail
	private void updateFilter(String filterText) {
        if (currentFilteredList == null) return;
        
        currentFilteredList.setPredicate(mail -> {
            if (filterText == null || filterText.isBlank()) return true;

            String lowerFilter = filterText.toLowerCase();
            
            String mittente = emailToNameMap.getOrDefault(mail.getMittente(), "");
            String destinatario = emailToNameMap.getOrDefault(mail.getDestinatario(), "");
            
            return mittente.toLowerCase().contains(lowerFilter) 
                || destinatario.toLowerCase().contains(lowerFilter)
                || (mail.getOggetto() != null && mail.getOggetto().toLowerCase().contains(lowerFilter))
                || (mail.getCorpo() != null && mail.getCorpo().toLowerCase().contains(lowerFilter));
        });
    }

	// Mostra Pannello Scrivi mail
	@FXML
    public void showCompose() {
        scriviPanel.setVisible(true);
        scriviPanel.setManaged(true);
    }

	// Nascondi Pannello Scrivi mail
    @FXML
    private void hideCompose() {
        scriviPanel.setVisible(false);
        scriviPanel.setManaged(false);
        destinatarioField.clear();
        oggettoField.clear();
        corpoArea.clear();
    }
    
	// Rispondi metodo
    public void rispondi(String mail, String oggetto) {
		destinatarioField.clear();
		destinatarioField.setText(mail);
		oggettoField.clear();
		String nuovoOggetto = oggetto;
    
		// Aggiunta automatica "Re:" se non presente
		if (nuovoOggetto != null && !nuovoOggetto.trim().toUpperCase().startsWith("RE:")) {
			nuovoOggetto = "Re: " + nuovoOggetto;
		}
		
		oggettoField.setText(nuovoOggetto);
		showCompose();
    }

	// NAVIGAZIONE
	@FXML
	private void indietro(ActionEvent event) throws IOException {
		// Torna alla pagina precedente in base al tipo di utente
		if (u instanceof Diabetologo) {
			Navigator.getInstance().switchToDiabetologoPage(event);
        } else if (u instanceof Paziente) {
			Navigator.getInstance().switchToPazientePage(event);
        }
	}
}