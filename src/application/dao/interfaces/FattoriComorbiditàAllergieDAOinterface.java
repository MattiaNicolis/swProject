package application.dao.interfaces;

import java.util.List;

import application.model.FattoriComorbiditàAllergie;
import application.model.Utente;

public interface FattoriComorbiditàAllergieDAOinterface {
    public List<FattoriComorbiditàAllergie> getFattoriComorbiditàAllergieByPaziente(Utente paziente);
    public boolean creaFattoreComorbiditàAllergia(FattoriComorbiditàAllergie fca);
    public boolean eliminaFattoreComorbiditàAllergia(FattoriComorbiditàAllergie fca);
}