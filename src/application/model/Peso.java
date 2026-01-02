package application.model;

import java.time.LocalDate;

public class Peso {
    
    private int id;
    private String cf;
    private double valore;
    private LocalDate giorno;

    public Peso(int id, String cf, double valore, LocalDate giorno) {
        this.id = id;
        this.cf = cf;
        this.valore = valore;
        this.giorno = giorno;
    }

    public int getId() {
        return id;
    }

    public String getCf() {
        return cf;
    }

    public double getValore() {
        return valore;
    }

    public LocalDate getGiorno() {
        return giorno;
    }
}