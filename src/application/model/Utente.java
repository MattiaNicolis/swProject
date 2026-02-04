package application.model;

import java.time.LocalDate;

public class Utente {

	private String cf;
	private String pw;
	private String nome;
	private String cognome;
	private LocalDate dataDiNascita;
	private String luogoDiNascita;
	private String sesso;
	private String mail;
	
	public Utente(String cf, String pw, String nome, String cognome, LocalDate dataDiNascita, String luogoDiNascita, String sesso, String mail) {
		this.cf = cf;
		this.pw = pw;
		this.nome = nome;
		this.cognome = cognome;
		this.dataDiNascita = dataDiNascita;
		this.sesso = sesso;
		this.luogoDiNascita = luogoDiNascita;
		this.mail = mail;
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
	
	public String getNome() {
		return nome;
	}

	public String getCognome() {
		return cognome;
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
}
