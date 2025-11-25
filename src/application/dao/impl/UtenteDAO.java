package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.Utente;

public class UtenteDAO implements application.dao.interfaces.UtenteDAOinterface {
    
    public List<Utente> getAllUtenti() {
        // Implementazione del metodo per ottenere tutti gli utenti dal database
        List<Utente> lista = new ArrayList<>();
        String query = "SELECT * FROM utenti";
	    try (Connection conn = Database.getConnection();
	    		PreparedStatement stmt = conn.prepareStatement(query);
	    		ResultSet rs = stmt.executeQuery()) {
	    	
            while (rs.next()) {
            	Utente utente = new Utente(
                    rs.getString("CF"),
                    rs.getString("pw"),
                    rs.getString("ruolo"),
                    rs.getString("nomeCognome"),
                    rs.getDate("dataDiNascita").toLocalDate(),
                    rs.getString("sesso"),
                    rs.getBlob("foto"),
                    rs.getString("mail"),
                    rs.getString("diabetologo_rif")
                );
                lista.add(utente);
            }
            
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    }
        return lista;
    }    
}