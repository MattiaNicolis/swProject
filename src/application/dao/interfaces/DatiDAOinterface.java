package application.dao.interfaces;

import java.util.List;

import application.model.Dato;
import application.model.Utente;

public interface DatiDAOinterface {
    public List<Dato> getDatiByPaziente(Utente paziente, String tipo);
    public boolean creaDato(Dato dato, String tipo);
    public boolean eliminaDato(Dato dato, String tipo);
}