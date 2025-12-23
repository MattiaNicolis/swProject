package application.dao.interfaces;

import java.util.List;

import application.model.Utente;

public interface UtenteDAOinterface {
    public Utente login(String cf, String password);
    public List<Utente> getPeopleByRole(String role);
} 
