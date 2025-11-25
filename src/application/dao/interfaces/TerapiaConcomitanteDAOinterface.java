package application.dao.interfaces;

import java.util.List;

import application.model.TerapiaConcomitante;
import application.model.Utente;

public interface TerapiaConcomitanteDAOinterface {
    public List<TerapiaConcomitante> getTerapieConcomitantiByPaziente(Utente paziente);
    public boolean creaTerapiaConcomitante(TerapiaConcomitante terapiaConcomitante);
    public boolean eliminaTerapiaConcomitante(TerapiaConcomitante terapiaConcomitante);
}