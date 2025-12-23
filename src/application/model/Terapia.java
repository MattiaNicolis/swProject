package application.model;

import java.time.LocalDate;

public class Terapia {

	private int id;
	private String cf;
	private String nomeFarmaco;
	private int dosiGiornaliere;
	private int quantità;
	private LocalDate dataInizio;
	private LocalDate dataFine;
	private String indicazioni;
	private String diabetologo;
	private boolean visualizzata;
	private int questionari;
	
	public Terapia(int id, String cf, String nomeFarmaco, int dosiGiornaliere, int quantità, LocalDate dataInizio, LocalDate dataFine, String indicazioni, String diabetologo, boolean visualizzata, int questionari) {
		this.id = id;
		this.cf = cf;
		this.nomeFarmaco = nomeFarmaco;
		this.dosiGiornaliere = dosiGiornaliere;
		this.quantità = quantità;
		this.dataInizio = dataInizio;
		this.dataFine = dataFine;
		this.indicazioni = indicazioni;
		this.diabetologo = diabetologo;
		this.visualizzata = visualizzata;
		this.questionari = questionari;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getCf() {
		return cf;
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
	
	public LocalDate getDataInizio() {
		return dataInizio;
	}
	
	public LocalDate getDataFine() {
		return dataFine;
	}
	
	public String getIndicazioni() {
		return indicazioni;
	}
	
	public String getDiabetologo() {
		return diabetologo;
	}
	
	public boolean getVisualizzata() {
		return visualizzata;
	}

	public int getQuestionari() {
		return questionari;
	}
}
