package application.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.model.Mail;
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
	
	// --- VARIABILI LOCALI ---	
	private Utente u;

	// --- LIST VIEW - MAPPE ---
	private List<Mail> mailRicevute = new ArrayList<>();
	private List<Mail> mailInviate = new ArrayList<>();
	private List<Utente> diabetologi = new ArrayList<>();
	private List<Utente> pazienti = new ArrayList<>();
	@FXML private ListView<Mail> listaMail;
	private Map<String, String> emailToNameMap = new HashMap<>();
	private FilteredList<Mail> currentFilteredList;

	// --- PAGINE ---
	@FXML private VBox scriviPanel;
	
	// --- BOTTONI ---
	@FXML private Button bottoneIndietro;
	
	// --- TEXTFIELD ---
	@FXML private TextField searchMailBar;
	@FXML private TextField destinatarioField;
	@FXML private TextField oggettoField;
	
	// --- TEXTAREA ---
	@FXML private TextArea corpoArea;
	
	// LABEL
	@FXML private Label mailNonLette;
	@FXML private Label mailDiabetologo;
	@FXML private Label labelDiabetologo;
	@FXML private Label headerLabel;
	
	@FXML public void initialize() throws IOException{
		u = Sessione.getInstance().getUtente();
		
		caricaDati();

		setupInterface();

		searchMailBar.textProperty().addListener((obs, oldVal, newVal) -> updateFilter(newVal));

		// MAIL RICEVUTE DI DEFAULT
		showMailRicevute(null);
		
		mailNonLette.setText("Non lette: " + AdminService.contatoreMailNonLette(mailRicevute));
		
		// VEDI UNA SPECIFICA MAIL
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
	} // FINE INITIALIZE ------------

	private void caricaDati() {
		mailInviate = AdminService.loadMailInviate(u);
		mailRicevute = AdminService.loadMailRicevute(u);
		diabetologi = AdminService.getPeopleByRole("diabetologo");
		if(u.isDiabetologo())
			pazienti = AdminService.getPeopleByRole("paziente");

		populateNameMap(diabetologi);
        populateNameMap(pazienti);
	}

	private void populateNameMap(List<Utente> utenti) {
        if(utenti == null) return;
        for(Utente utente : utenti) {
            emailToNameMap.put(utente.getMail(), utente.getNomeCognome());
        }
    }
	
	private void setupInterface() {
        if (u.isPaziente()) {
            diabetologi.stream()
                .filter(d -> d.getCf().equals(u.getDiabetologoRif()))
                .findFirst()
                .ifPresent(d -> {
                    destinatarioField.setText(d.getMail());
                    mailDiabetologo.setText(d.getMail());
                });
        } else {
            mailDiabetologo.setVisible(false);
            labelDiabetologo.setVisible(false);
        }
    }

	private enum MailResult {
		EMPTY_FIELDS,
		INVALID_DATA,
		SUCCESS,
		FAILURE
	}
	
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
	
	@FXML
    private void showMailRicevute(ActionEvent e) {
        setupListView(mailRicevute, false);
		headerLabel.setText("Posta arrivata");
    }

    @FXML
    private void showMailInviate(ActionEvent e) {
        setupListView(mailInviate, true);
		headerLabel.setText("Posta inviata");
    }

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
                    
                    String displayName = emailToNameMap.getOrDefault(targetEmail, targetEmail);

                    String preview = mail.getCorpo().split("\n")[0];
                    if (preview.length() > 30) preview = preview.substring(0, 30) + "...";

                    String stato = "";
                    String stile = "";

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

	@FXML
    public void showCompose() {
        scriviPanel.setVisible(true);
        scriviPanel.setManaged(true);
    }

    @FXML
    private void hideCompose() {
        scriviPanel.setVisible(false);
        scriviPanel.setManaged(false);
        destinatarioField.clear();
        oggettoField.clear();
        corpoArea.clear();
    }
    
    public void rispondi(String mail, String oggetto) {
		destinatarioField.clear();
		destinatarioField.setText(mail);
		oggettoField.clear();
		String nuovoOggetto = oggetto;
    
		if (nuovoOggetto != null && !nuovoOggetto.trim().toUpperCase().startsWith("RE:")) {
			nuovoOggetto = "Re: " + nuovoOggetto;
		}
		
		oggettoField.setText(nuovoOggetto);
		showCompose();
    }

	// NAVIGAZIONE
	@FXML
	private void indietro(ActionEvent event) throws IOException {
		if (u.isDiabetologo()) {
			Navigator.getInstance().switchToDiabetologoPage(event);
        } else if (u.isPaziente()) {
			Navigator.getInstance().switchToPazientePage(event);
        }
	}
}