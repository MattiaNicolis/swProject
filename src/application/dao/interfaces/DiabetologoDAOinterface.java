package application.dao.interfaces;

import application.model.Diabetologo;

public interface DiabetologoDAOinterface {
    public Diabetologo loginDiabetologo(String cf, String password);
    public String getNomeCognomeDiabetologoByCf(String cf);
} 