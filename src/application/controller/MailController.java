package application.controller;

import java.io.IOException;

import application.admin.Amministratore;
import application.admin.MessageUtils;
import application.admin.Sessione;
import application.model.Mail;
import application.model.Utente;
import application.view.Navigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
	
	// --- SEZIONE VARIABILI LOCALI ---	
	private Utente u;
	private String nome_cognome;
	
	// --- SEZIONE PAGINE ---
	@FXML private VBox scriviPanel;
	
	// --- SEZIONE BOTTONI ---
	@FXML private Button bottoneIndietro;
	
	// --- SEZIONE TEXTFIELD ---
	@FXML private TextField searchMailBar;
	@FXML private TextField destinatarioField;
	@FXML private TextField oggettoField;
	
	// --- SEZIONE TEXTAREA ---
	@FXML private TextArea corpoArea;
	
	// --- SEZIONI LISTEVIEW ---
	@FXML private ListView<Mail> listaMail;
	ObservableList<Mail> listaMailRicevuteAsObservable;
	ObservableList<Mail> listaMailInviateAsObservable;
	
	@FXML private Label mailNonLette;
	
	@FXML public void initialize() throws IOException{
		u = Sessione.getInstance().getUtente();
		
		//Impostazione bottoneIndietro
		if ("diabetologo".equals(u.getRuolo())) {
    			bottoneIndietro.setOnAction(e -> {
                try {
                	Navigator.getInstance().switchToDiabetologoPage(e);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        } else if ("paziente".equals(u.getRuolo())) {
        		Amministratore.diabetologi.stream()
        			.filter(d -> d.getCf().equals(u.getDiabetologoRif()))
        			.findFirst()
        			.ifPresent(d -> {
        				destinatarioField.setText(d.getMail());
        			});
        		bottoneIndietro.setOnAction(e -> {
                try {
                	Navigator.getInstance().switchToPazientePage(e);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
		
		// MAIL RICEVUTE DI DEFAULT
		showMailRicevute(null);
		
		mailNonLette.setText("Non lette: " + Amministratore.contatoreMailNonLette());
		
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
	}
	
	public enum MailResult {
		EMPTY_FIELDS,
		INVALID_DATA,
		SUCCESS,
		FAILURE
	}
	public MailResult trySendMail(String destinatario, String oggetto, String corpo) {
		if(destinatarioField.getText().isBlank() || oggettoField.getText().isBlank() || corpoArea.getText().isBlank()) {
			return MailResult.EMPTY_FIELDS;
		}

		boolean esiste = Amministratore.utenti.stream()
			.anyMatch(d -> d.getMail().equals(destinatarioField.getText()));
		if(!esiste) {
			destinatarioField.clear();
			return MailResult.INVALID_DATA;
		}

		if(u.getRuolo().equals("paziente")) {
			boolean pToP = Amministratore.pazienti.stream()
				.anyMatch(p -> p.getMail().equalsIgnoreCase(destinatarioField.getText()));
			
			if(pToP) {
				destinatarioField.clear();
				return MailResult.INVALID_DATA;
			}	
		}

		Mail mail = new Mail(0, u.getMail(), destinatario, oggetto, corpo, null, null, false);
		boolean ok = Amministratore.mailDAO.scriviMail(mail);
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
			case INVALID_DATA -> MessageUtils.showError("Mail destinatario non valida.");
			case FAILURE -> MessageUtils.showError("Errore nell'invio della mail.");
			case SUCCESS -> {
				MessageUtils.showSuccess("Mail inviata!");
				hideCompose();
			}
		}
		
	}
	
	@FXML
	private void showMailRicevute(ActionEvent e) throws IOException {
		//Quando viene schiacciato il bottone ricevute
		//rimane sulle mail ricevute
		// Mail ricevute
		listaMailRicevuteAsObservable = FXCollections.observableArrayList(
		    Amministratore.mail.stream()
		        .filter(m -> u.getMail().equals(m.getDestinatario()))
		        .toList()
		);

		listaMail.setItems(listaMailRicevuteAsObservable);

		// CELL FACTORY: mail non lette in grassetto + sfondo diverso
		listaMail.setCellFactory(event -> new ListCell<Mail>() {
		    protected void updateItem(Mail mail, boolean empty) {
		        super.updateItem(mail, empty);
		        
		        if (empty || mail == null) {
		            setText(null);
		            setStyle("");
		        } else {
		        	nome_cognome = Amministratore.utenti.stream()
		        		.filter(p -> p.getMail().equals(mail.getMittente()))
		        		.map(Utente::getNomeCognome)
		        		.findFirst()
		        		.orElse(null);
		        	String corpo = mail.getCorpo();
					String[] righe = corpo.split("\n");
					if (righe.length > 0) {
						corpo = righe[0];
					}	
		            setText(nome_cognome + "\nOggetto: " + mail.getOggetto() + "\nCorpo: " + corpo + "...");

		            if (!mail.getLetta()) {
		                // NON LETTA: grassetto + sfondo azzurrino
		                setStyle("-fx-font-weight: bold; -fx-background-color: #f0f8ff;");
		            } else {
		                setStyle("");
		            }
		        }
		    }
		});

		FilteredList<Mail> filteredMail = new FilteredList<>(listaMailRicevuteAsObservable, p -> true);
		setFilteredList(filteredMail);
	}

	@FXML
	private void showMailInviate(ActionEvent e) throws IOException {
		//Quando schiaccia il bottone inviate
		//passa alle mail inviate
		// Mail inviate
		listaMailInviateAsObservable = FXCollections.observableArrayList(
		    Amministratore.mail.stream()
		        .filter(m -> u.getMail().equals(m.getMittente()))
		        .toList()
		);

		listaMail.setItems(listaMailInviateAsObservable);

		// CELL FACTORY: mail non lette in grassetto + sfondo diverso
		listaMail.setCellFactory(event -> new ListCell<Mail>() {
		    protected void updateItem(Mail mail, boolean empty) {
		        super.updateItem(mail, empty);
		        
		        if (empty || mail == null) {
		            setText(null);
		            setStyle("");
		        } else {
		        	nome_cognome = Amministratore.utenti.stream()
		        		.filter(p -> p.getMail().equals(mail.getDestinatario()))
		        		.map(Utente::getNomeCognome)
		        		.findFirst()
		        		.orElse(null);
					
					String corpo = mail.getCorpo();
					String[] righe = corpo.split("\n");
					if (righe.length > 0) {
						corpo = righe[0];
					}
		        	
		            if (!mail.getLetta()) {
		                // NON LETTA: grassetto + sfondo azzurrino
						setText(nome_cognome + "\nOggetto: " + mail.getOggetto() + "\nCorpo: " + corpo + "...\n(Non letta)");
		            } else {
		                setText(nome_cognome + "\nOggetto: " + mail.getOggetto() + "\nCorpo: " + corpo + "...\n(Letta)");
		            }
		        }
		    }
		});

		FilteredList<Mail> filteredMail = new FilteredList<>(listaMailInviateAsObservable, p -> true);
		setFilteredList(filteredMail);
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
		oggettoField.setText(oggetto);
		showCompose();
    }

	private void setFilteredList(FilteredList<Mail> filteredMail) {
		// ✅ collega la lista filtrata alla ListView
		listaMail.setItems(filteredMail);

		searchMailBar.textProperty().addListener((obs, oldValue, newValue) -> {
		    filteredMail.setPredicate(mail -> {
		        if (newValue == null || newValue.isBlank())
		            return true;

		        String filtro = newValue.toLowerCase();

		        // 🔹 Nome mittente
		        String nomeMittente = Amministratore.utenti.stream()
					.filter(p -> p.getMail().equals(mail.getMittente()))
					.map(Utente::getNomeCognome)
					.findFirst()
					.orElse("");

		        // 🔹 Condizioni di ricerca
		        String oggetto = mail.getOggetto() != null ? mail.getOggetto().toLowerCase() : "";
		        String corpo = mail.getCorpo() != null ? mail.getCorpo().toLowerCase() : "";

		        return nomeMittente.toLowerCase().contains(filtro)
		            || oggetto.contains(filtro)
		            || corpo.contains(filtro);
		    });
		});	
	}
}