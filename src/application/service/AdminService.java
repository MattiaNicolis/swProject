package application.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import application.dao.impl.DatiDAO;
import application.dao.impl.GlicemiaDAO;
import application.dao.impl.MailDAO;
import application.dao.impl.PatologiaDAO;
import application.dao.impl.PesoDAO;
import application.dao.impl.QuestionarioDAO;
import application.dao.impl.TerapiaConcomitanteDAO;
import application.dao.impl.TerapiaDAO;
import application.dao.impl.UtenteDAO;
import application.model.Dato;
import application.model.Glicemia;
import application.model.Mail;
import application.model.Patologia;
import application.model.Peso;
import application.model.Questionario;
import application.model.Terapia;
import application.model.TerapiaConcomitante;
import application.model.Utente;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class AdminService {
	
	public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // DAO
    private static TerapiaDAO terapiaDAO = new TerapiaDAO();
    private static UtenteDAO utenteDAO = new UtenteDAO();
	private static DatiDAO datiDAO = new DatiDAO();
	private static PatologiaDAO patologiaDAO = new PatologiaDAO();
	private static TerapiaConcomitanteDAO terapiaConcomitanteDAO = new TerapiaConcomitanteDAO();
	private static GlicemiaDAO glicemiaDAO = new GlicemiaDAO();
	private static MailDAO mailDAO = new MailDAO();
	private static QuestionarioDAO questDAO = new QuestionarioDAO();
	private static PesoDAO pesoDAO = new PesoDAO();

	// DAO SETTER PER TEST
	public static void setUtenteDAO(UtenteDAO dao) {
        utenteDAO = dao;
    }

	public static void setTerapiaDAO(TerapiaDAO dao) {
		terapiaDAO = dao;
	}

	public static void setQuestionarioDAO(QuestionarioDAO dao) {
		questDAO = dao;
	}

	public static void setMailDAO(MailDAO dao) {
		mailDAO = dao;
	}

	public static void setGlicemiaDAO(GlicemiaDAO dao) {
		glicemiaDAO = dao;
	}

	public static void setPesoDAO(PesoDAO dao) {
		pesoDAO = dao;
	}

	public static void setDatiDAO(DatiDAO dao) {
		datiDAO = dao;
	}

	public static void setPatologiaDAO(PatologiaDAO dao) {
		patologiaDAO = dao;
	}

	public static void setTerapiaConcomitanteDAO(TerapiaConcomitanteDAO dao) {
		terapiaConcomitanteDAO = dao;
	}

	// ---------------------------------
	// CERCA UTENTE PER LOGIN
	public static Utente login(String cf, String password) {
		return utenteDAO.login(cf, password);
	}
	// CREA LISTE PAZIENTI E DIABETOLOGI
	public static List<Utente> getPeopleByRole(String role) {
		return utenteDAO.getPeopleByRole(role);
	}
	
	// -------------------------------------------
	// CREA TERAPIA
	public static boolean creaTerapia(Terapia t) {
		return terapiaDAO.creaTerapia(t);
	}
	// MODIFICA TERAPIA
	public static boolean modificaTerapia(Terapia t) {
		return terapiaDAO.modificaTerapia(t);
	}
	// ELIMINA TERAPIA
	public static boolean eliminaTerapia(Terapia t) {
		return terapiaDAO.eliminaTerapia(t);
	}
	// NOTIFICA TERAPIA
	public static boolean notificaTerapia(Terapia t) {
		return terapiaDAO.notificaTerapia(t);
	}
	// CARICA TERAPIE DI UN PAZIENTE DAL DATABASE
	public static List<Terapia> loadTerapieByPaziente(Utente paziente) {
		return terapiaDAO.getTerapieByPaziente(paziente);
	}
	// CARICA NUMERO DI TERAPIE ATTIVE PER PAZIENTE IN UNA CERTA DATA
	public static int loadTerapieAttiveByCfAndData(String cf, LocalDate data) {
		return terapiaDAO.getNumeroTerapieAttive(cf, data);
	}
	// CARICA NUMERO DI TERAPIE SODDISFATTE PER PAZIENTE IN UNA CERTA DATA
	public static int loadTerapieSoddisfatteByCfAndData(String cf, LocalDate data) {
		return terapiaDAO.getTerapieSoddisfatte(cf, data);
	}
	// AGGIORNA NUMERO QUESTIONARI PER TERAPIA
	public static boolean loadAggiornaNumQuestionari(Terapia t) {
		return terapiaDAO.aggiornaNumQuestionari(t);
	}
	// -----------------------------------------------
	// CARICA MISURAZIONI PESO DI UN PAZIENTE
	public static List<Peso> loadPesoByCf(String cf) {
		return pesoDAO.getPesoByCf(cf);
	}
	// AGGIORNA PESO
	public static boolean aggiornaPeso(Peso p) {
		return pesoDAO.aggiornaPeso(p);
	}
	// CREA PESO
	public static boolean creaPeso(Peso p) {
		return pesoDAO.creaPeso(p);
	}

	// -------------------------------------------------------------------------------------------------------
	// CARICA FATTORI DAL DATABASE
	public static List<Dato> loadFattoriByPaziente(Utente paziente) {
		return datiDAO.getDatiByPaziente(paziente, "fattori");
	}
	// CREA FATTORE
	public static boolean creaFattore(Dato fattore) {
		return datiDAO.creaDato(fattore, "fattori");
	}
	// ELIMINA FATTORE
	public static boolean eliminaFattore(Dato fattore) {
		return datiDAO.eliminaDato(fattore, "fattori");
	}

	// ------------------------------------------------------------------
	// CARICA COMORBIDITA' DAL DATABASE
	public static List<Dato> loadComorbiditàByPaziente(Utente paziente) {
		return datiDAO.getDatiByPaziente(paziente, "comorbidità");
	}
	// CREA COMORBIDITA'
	public static boolean creaComorbidità(Dato comorbidità) {
		return datiDAO.creaDato(comorbidità, "comorbidità");
	}
	// ELIMINA COMORBIDITA'
	public static boolean eliminaComorbidità(Dato comorbidità) {
		return datiDAO.eliminaDato(comorbidità, "comorbidità");
	}

	// ----------------------------------------------------------------
	// CARICA ALLERGIE DAL DATABASE
	public static List<Dato> loadAllergieByPaziente(Utente paziente) {
		return datiDAO.getDatiByPaziente(paziente, "allergie");
	}
	// CREA ALLERGIA
	public static boolean creaAllergia(Dato allergia) {
		return datiDAO.creaDato(allergia, "allergie");
	}
	// ELIMINA ALLERGIA
	public static boolean eliminaAllergia(Dato allergia) {
		return datiDAO.eliminaDato(allergia, "allergie");
	}

	// -------------------------------------------------------------------
	// CARICA GLICEMIA DI UN PAZIENTE DAL DATABASE
	public static List<Glicemia> loadGlicemiaByPaziente(Utente paziente) {
		return glicemiaDAO.getGlicemiaByPaziente(paziente);
	}
	// CREA GLICEMIA
	public static boolean creaGlicemia(Glicemia g) {
		return glicemiaDAO.creaGlicemia(g);
	}
	// CARICA TUTTE LE GLICEMIE DAL DATABASE
	public static List<Glicemia> loadAllGlicemia() {
		return glicemiaDAO.getAllGlicemia();
	}
	
	// -------------------------------------------------------
	// CARICA MAIL RICEVUTE DAL DATABASE
	public static List<Mail> loadMailRicevute(Utente utente) {
		return mailDAO.getMailRicevute(utente);
	}
	// CARICA MAIL INVIATE DAL DATABASE
	public static List<Mail> loadMailInviate(Utente utente) {
		return mailDAO.getMailInviate(utente);
	}
	// SEGNA MAIL COME LETTA
	public static boolean vediMail(Mail m) {
		return mailDAO.vediMail(m);
	}
	// SCRIVI MAIL
	public static boolean scriviMail(Mail m) {
		return mailDAO.scriviMail(m);
	}

	// ---------------------------------------------------------------------
	// CARICA PATOLOGIE DAL DATABASE
	public static List<Patologia> loadPatologieByPaziente(Utente paziente) {
		return patologiaDAO.getPatologieByPaziente(paziente);
	}
	// CREA PATOLOGIA
	public static boolean creaPatologia(Patologia p) {
		return patologiaDAO.creaPatologia(p);
	}
	// ELIMINA PATOLOGIA
	public static boolean eliminaPatologia(Patologia p) {
		return patologiaDAO.eliminaPatologia(p);
	}

	// -----------------------------------------------------------------------------------------
	// CARICA TERAPIE CONCOMITANTI DAL DATABASE
	public static List<TerapiaConcomitante> loadTerapieConcomitantiByPaziente(Utente paziente) {
		return terapiaConcomitanteDAO.getTerapieConcomitantiByPaziente(paziente);
	}
	// CREA TERAPIA CONCOMITANTE
	public static boolean creaTerapiaConcomitante(TerapiaConcomitante tc) {
		return terapiaConcomitanteDAO.creaTerapiaConcomitante(tc);
	}
	// ELIMINA TERAPIA CONCOMITANTE
	public static boolean eliminaTerapiaConcomitante(TerapiaConcomitante tc) {
		return terapiaConcomitanteDAO.eliminaTerapiaConcomitante(tc);
	}

	// --------------------------------------------------------------------------
	// CARICA QUESTIONARI DI UN PAZIENTE DAL DATABASE
	public static List<Questionario> loadQuestionariByPaziente(Utente paziente) {
		return questDAO.getQuestionariByPaziente(paziente);
	}
	// CARICA TUTTI I QUESTIONARI NON CONFORMI DAL DATABASE
	public static List<Questionario> loadQuestionariNonConformi() {
		return questDAO.getQuestionariNonConformi();
	}
	// SEGNA QUESTIONARIO COME CONTROLLATO
	public static boolean segnaComeControllato(Questionario q) {
		return questDAO.segnaComeControllato(q);
	}
	// CREA QUESTIONARIO
	public static boolean creaQuestionario(Questionario q) {
		return questDAO.creaQuestionario(q);
	}
	// ESISTE QUESTIONARIO OGGI
	public static boolean esisteQuestionarioOggi(int terapiaId) {
		return questDAO.esisteQuestionarioOggi(terapiaId);
	}

	//---------------------------------------------
	// METODI DI ACCESSO RAPIDO
	//---------------------------------------------	
	
	// RITORNA UTENTE
	public static Utente getUtenteByCf(List<Utente> utenti, String cf) {
		return utenti.stream()
				.filter(utente -> utente.getCf().equals(cf))
				.findFirst()
				.orElse(null);
	}
	
	// RITORNA NOME UTENTE
	public static String getNomeUtenteByCf(List<Utente> utenti, String cf) {
		Utente u = getUtenteByCf(utenti, cf);
		if(u != null) {
			return u.getNomeCognome();
		}
		return null;
	}

	// CONTA LE MAIL NON LETTE RELAVITE A UN CERTO DESTINATARIO
	public static long contatoreMailNonLette(List<Mail> lista) {
		return lista.stream()
				.filter(mail -> !mail.getLetta())
				.count();
	}

	// PROPRIETA' GRAFICO GLICEMIA
	public static XYChart.Data<String, Number> proprietàPunto(XYChart.Data<String, Number> punto, int valore, String indicazioni) {
		punto.nodeProperty().addListener((obs, oldNode, newNode) -> {
			if (newNode != null) {
				if(indicazioni.equals("Pre pasto")) {
					if(valore < 80 || valore > 130)
						newNode.setStyle("-fx-background-color: red;");
					else
						newNode.setStyle("-fx-background-color: green;");
				} else if(indicazioni.equals("Post pasto")) {
					if(valore > 180)
						newNode.setStyle("-fx-background-color: red;");
					else
						newNode.setStyle("-fx-background-color: green;");
				}
			}
		});
					
		return punto;
	}

	// CELL FACTORY GENERICO
	public static <T> void setCustomCellFactory(ListView<T> listView, Function<T, String> textExtractor) {
		listView.setCellFactory(param -> new ListCell<T>() {
			protected void updateItem(T item, boolean empty) {
				super.updateItem(item, empty);
				
				if (empty || item == null) {
					setText(null);
					setStyle("");
				} else {
					setText(textExtractor.apply(item)); 
				}
			}
		});
	}

}
