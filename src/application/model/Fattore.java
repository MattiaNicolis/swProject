package application.model;

public class Fattore extends Dato {
    
    private String tipo;

    public Fattore(String cf, String tipo, String nome, String modificato) {
        super(cf, nome, modificato);
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }
}