package application.model;

public class UtenteInfo {
    private String nome;
    private String cognome;
    private String mail;

    public UtenteInfo(String nome, String cognome, String mail) {
        this.nome = nome;
        this.cognome = cognome;
        this.mail = mail;
    }

    public String getMail() { 
        return mail; 
    }
    
    public String getNomeCognome() { 
        return nome + " " + cognome; 
    }
}