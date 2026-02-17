package application.model;

import java.time.LocalDate;

public class Paziente extends Utente {
    
	private String diabetologoRif;
	
	public Paziente(String cf, String pw, String nome, String cognome, LocalDate dataDiNascita, String luogoDiNascita, String sesso, String mail, String diabetologoRif) {
		super(cf, pw, nome, cognome, dataDiNascita, luogoDiNascita, sesso, mail);
		this.diabetologoRif = diabetologoRif;
    }

    public String getDiabetologoRif() {
        return diabetologoRif;
    }

}