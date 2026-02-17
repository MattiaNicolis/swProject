package application.dao.interfaces;

import java.util.List;

import application.model.Paziente;

public interface PazienteDAOinterface {
    public Paziente loginPaziente(String cf, String password);
    public List<Paziente> getPazienti();
}