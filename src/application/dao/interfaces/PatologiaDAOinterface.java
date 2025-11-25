package application.dao.interfaces;

import java.util.List;

import application.model.Patologia;
import application.model.Utente;

public interface PatologiaDAOinterface {
    public List<Patologia> getPatologieByPaziente(Utente paziente);
    public boolean creaPatologia(Patologia patologia);
    public boolean eliminaPatologia(Patologia Patologia);
}