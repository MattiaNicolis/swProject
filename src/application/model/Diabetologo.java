package application.model;

import java.time.LocalDate;

public class Diabetologo extends Utente {
    
    public Diabetologo(String cf, String pw, String nome, String cognome, LocalDate dataDiNascita, String luogoDiNascita, String sesso, String mail) {
		super(cf, pw, nome, cognome, dataDiNascita, luogoDiNascita, sesso, mail);
    }
}
