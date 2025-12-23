package application.dao.interfaces;

import java.util.List;

import application.model.Peso;

public interface PesoDAOinterface {
    public List<Peso> getPesoByCf(String cf);
    public boolean creaPeso(Peso peso);
    public boolean aggiornaPeso(Peso peso);
} 
