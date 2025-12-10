package application.model;

public class Comorbidità extends Dato {
    
    private String tipo;

    public Comorbidità(String cf, String tipo, String nome, String modificato) {
        super(cf, nome, modificato);
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }
}
