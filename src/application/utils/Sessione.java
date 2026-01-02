package application.utils;

import application.model.Mail;
import application.model.Patologia;
import application.model.Questionario;
import application.model.Terapia;
import application.model.TerapiaConcomitante;
import application.model.Utente;

public class Sessione {

	private static Sessione instance;
	
	private Utente utente;
	private Utente pazienteSelezionato;
	private Terapia terapiaSelezionata;
	private TerapiaConcomitante terapiaConcomitanteSelezionata;
	private Patologia patologiaSelezionata;
	private Mail mailSelezionata;
	private Questionario questSelezionato;
	
	// COSTRUTTORE PRIVATO
	private Sessione() {}
	
	public static Sessione getInstance() {
		if(instance == null) {
			instance = new Sessione();
		}
		return instance;
	}
	
	//------------------------------------
	public void setUtente(Utente utente) {
		this.utente = utente;
	}
	
	public Utente getUtente() {
		return utente;
	}
	
	public void logout() {
		setUtente(null);

		// NULL ANCHE IL RESTO PER PULIZIA SICURA
		setPazienteSelezionato(null);
		setTerapiaSelezionata(null);
		setTerapiaConcomitanteSelezionata(null);
		setPatologiaSelezionata(null);
		setMailSelezionata(null);
		setQuestionarioSelezionato(null);
	}

	//---------------------------------------------------
	public void setPazienteSelezionato(Utente pazienteSelezionato) {
		this.pazienteSelezionato = pazienteSelezionato;
	}
	
	public Utente getPazienteSelezionato() {
		return pazienteSelezionato;
	}

	//--------------------------------------------------
	public void setTerapiaSelezionata(Terapia terapia) {
		this.terapiaSelezionata = terapia;
	}
	
	public Terapia getTerapiaSelezionata() {
		return terapiaSelezionata;
	}

	//--------------------------------------------------------------------------------------
	public void setTerapiaConcomitanteSelezionata(TerapiaConcomitante terapiaConcomitante) {
		this.terapiaConcomitanteSelezionata = terapiaConcomitante;
	}
	
	public TerapiaConcomitante getTerapiaConcomitanteSelezionata() {
		return terapiaConcomitanteSelezionata;
	}

	//--------------------------------------------------------
	public void setPatologiaSelezionata(Patologia patologia) {
		this.patologiaSelezionata = patologia;
	}
	
	public Patologia getPatologiaSelezionata() {
		return patologiaSelezionata;
	}

	//-----------------------------------------
	public void setMailSelezionata(Mail mail) {
		this.mailSelezionata = mail;
	}
	
	public Mail getMailSelezionata() {
		return mailSelezionata;
	}
	//----------------------------------------------------------
	public void setQuestionarioSelezionato(Questionario quest) {
		this.questSelezionato = quest;
	}
	
	public Questionario getQuestionarioSelezionato() {
		return questSelezionato;
	}
}