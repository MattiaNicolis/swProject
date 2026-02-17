package application.model;

public class Allergia extends Dato {
    
    private String tipo;

    public Allergia(String cf, String tipo, String nome, String modificato) {
        super(cf, nome, modificato);
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }
}