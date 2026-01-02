package application.model;

import java.time.LocalDate;

public class Questionario {

	private int id;
	private String cf;
	private LocalDate giornoCompilazione;
	private String nomeFarmaco;
	private int dosiGiornaliere;
	private int quantità;
	private String sintomi;
	private boolean controllato;
	private int terapia_id;

	public Questionario(int id, String cf, LocalDate giornoCompilazione, String nomeFarmaco, int dosiGiornaliere, int quantità, String sintomi, boolean controllato, int terapia_id) {
		this.id = id;
		this.cf = cf;
		this.giornoCompilazione = giornoCompilazione;
		this.nomeFarmaco = nomeFarmaco;
		this.dosiGiornaliere = dosiGiornaliere;
		this.quantità = quantità;
		this.sintomi = sintomi;
		this.controllato = controllato;
		this.terapia_id = terapia_id;
	}
	
	public int getId() {
		return id;
	}

	public String getCf() {
		return cf;
	}
	
	public LocalDate getGiornoCompilazione() {
		return giornoCompilazione;
	}
	
	public String getNomeFarmaco() {
		return nomeFarmaco;
	}
	
	public int getDosiGiornaliere() {
		return dosiGiornaliere;
	}
	
	public int getQuantità() {
		return quantità;
	}
	
	public String getSintomi() {
		return sintomi;
	}
	
	public boolean getControllato() {
		return controllato;
	}

	public void setControllato(boolean controllato) {
		this.controllato = controllato;
	}

	public int getTerapiaId() {
		return terapia_id;
	}
}