package application.model;

import java.time.LocalDate;

public class Utente {

	private String cf;
	private String pw;
	private String ruolo;
	private String nomeCognome;
	private LocalDate dataDiNascita;
	private String luogoDiNascita;
	private String sesso;
	private String mail;
	private String diabetologoRif;
	
	public Utente(String cf, String pw, String ruolo, String nomeCognome, LocalDate dataDiNascita, String luogoDiNascita, String sesso, String mail, String diabetologoRif) {
		this.cf = cf;
		this.pw = pw;
		this.ruolo = ruolo;
		this.nomeCognome = nomeCognome;
		this.dataDiNascita = dataDiNascita;
		this.sesso = sesso;
		this.luogoDiNascita = luogoDiNascita;
		this.mail = mail;
		this.diabetologoRif = diabetologoRif;
	}
	
	public boolean checkPw(String pw) {
		return this.pw.equals(pw);
	}
	
	public String getCf() {
		return cf;
	}

	public String getPw() {
		return pw;
	}
	
	public String getRuolo() {
		return ruolo;
	}

	public boolean isDiabetologo() {
    	return "diabetologo".equals(this.ruolo);
	}

	public boolean isPaziente() {
		return "paziente".equals(this.ruolo);
	}
	
	public String getNomeCognome() {
		return nomeCognome;
	}
	
	public LocalDate getDataDiNascita() {
		return dataDiNascita;
	}
	
	public String getSesso() {
		return sesso;
	}
	
	public String getLuogoDiNascita() {
		return luogoDiNascita;
	}
	
	public String getMail() {
		return mail;
	}
	
	public String getDiabetologoRif() {
		return diabetologoRif;
	}
}
