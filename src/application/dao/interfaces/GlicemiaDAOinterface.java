package application.dao.interfaces;

import java.util.List;

import application.model.Glicemia;
import application.model.Utente;

public interface GlicemiaDAOinterface {
    public List<Glicemia> getAllGlicemia();
    public boolean creaGlicemia(Glicemia glicemia);
    public List<Glicemia> getGlicemiaByPaziente(Utente paziente);
}
