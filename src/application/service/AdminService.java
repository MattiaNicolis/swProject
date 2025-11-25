package application.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import application.dao.impl.FattoriComorbiditàAllergieDAO;
import application.dao.impl.GlicemiaDAO;
import application.dao.impl.MailDAO;
import application.dao.impl.PatologiaDAO;
import application.dao.impl.QuestionarioDAO;
import application.dao.impl.TerapiaConcomitanteDAO;
import application.dao.impl.TerapiaDAO;
import application.dao.impl.UtenteDAO;
import application.model.FattoriComorbiditàAllergie;
import application.model.Glicemia;
import application.model.Mail;
import application.model.Patologia;
import application.model.Questionario;
import application.model.Terapia;
import application.model.TerapiaConcomitante;
import application.model.Utente;

public class AdminService {
	
	public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	
	// LISTE
	public static List<Utente> utenti = new ArrayList<>();
	public static List<Utente> pazienti = new ArrayList<>();
	public static List<Utente> diabetologi = new ArrayList<>();

    // DAO
    public static final TerapiaDAO terapiaDAO = new TerapiaDAO();
    public static final UtenteDAO utenteDAO = new UtenteDAO();
	public static final FattoriComorbiditàAllergieDAO fattoriComorbiditàAllergieDAO = new FattoriComorbiditàAllergieDAO();
	public static final PatologiaDAO patologiaDAO = new PatologiaDAO();
	public static final TerapiaConcomitanteDAO terapiaConcomitanteDAO = new TerapiaConcomitanteDAO();
	public static final GlicemiaDAO glicemiaDAO = new GlicemiaDAO();
	public static final MailDAO mailDAO = new MailDAO();
	public static final QuestionarioDAO questDAO = new QuestionarioDAO();

	// CARICA UTENTI DAL DATABASE
	public static void loadAllUtenti() {
	    utenti.clear();
		utenti.addAll(utenteDAO.getAllUtenti());
		//System.out.println("[Amministratore] Utenti caricati: " + utenti.size());
		creaListe();
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

	// CARICA STORIA DATI DAL DATABASE
	public static List<FattoriComorbiditàAllergie> loadFattoriComorbiditàAllergieByPaziente(Utente paziente) {
		return fattoriComorbiditàAllergieDAO.getFattoriComorbiditàAllergieByPaziente(paziente);
	}
	
	// CARICA GLICEMIA DI UN PAZIENTE DAL DATABASE
	public static List<Glicemia> loadGlicemiaByPaziente(Utente paziente) {
		return glicemiaDAO.getGlicemiaByPaziente(paziente);
	}

	// CARICA TUTTE LE GLICEMIE DAL DATABASE
	public static List<Glicemia> loadAllGlicemia() {
		return glicemiaDAO.getAllGlicemia();
	}
	
	//CARICA MAIL RICEVUTE DAL DATABASE
	public static List<Mail> loadMailRicevute(Utente utente) {
		return mailDAO.getMailRicevute(utente);
	}

	//CARICA MAIL INVIATE DAL DATABASE
	public static List<Mail> loadMailInviate(Utente utente) {
		return mailDAO.getMailInviate(utente);
	}
	
	// CARICA PATOLOGIE DAL DATABASE
	public static List<Patologia> loadPatologieByPaziente(Utente paziente) {
		return patologiaDAO.getPatologieByPaziente(paziente);
	}
	
	// CARICA TERAPIE CONCOMITANTI DAL DATABASE
	public static List<TerapiaConcomitante> loadTerapieConcomitantiByPaziente(Utente paziente) {
		return terapiaConcomitanteDAO.getTerapieConcomitantiByPaziente(paziente);
	}
	
	// CARICA QUESTIONARI DI UN PAZIENTE DAL DATABASE
	public static List<Questionario> loadQuestionariByPaziente(Utente paziente) {
		return questDAO.getQuestionariByPaziente(paziente);
	}

	// CARICA TUTTI I QUESTIONARI NON CONFORMI DAL DATABASE
	public static List<Questionario> loadQuestionariNonConformi() {
		return questDAO.getQuestionariNonConformi();
	}
	
	// CREA LISTE PAZIENTI E DIABETOLOGI
	public static void creaListe(){
		pazienti = utenti.stream()
				.filter(utente -> utente.getRuolo().equals("paziente"))
				.toList();
		diabetologi = utenti.stream()
				.filter(utente -> utente.getRuolo().equals("diabetologo"))
				.toList();
	}

	//------------------------------------------
	// METODI DI ACCESSO RAPIDO
	//------------------------------------------

	// CONTROLLO ESISTENZA UTENTE
	public static boolean utenteEsiste(String cf) {
		return utenti.stream()
			.anyMatch(utente -> utente.getCf().equals(cf));
	}	
	
	// RITORNA UTENTE
	public static Utente getUtenteByCf(String cf) {
		return utenti.stream()
				.filter(utente -> utente.getCf().equals(cf))
				.findFirst()
				.orElse(null);
	}
	
	// RITORNA NOME UTENTE
	public static String getNomeUtenteByCf(String cf) {
		Utente u = getUtenteByCf(cf);
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
}