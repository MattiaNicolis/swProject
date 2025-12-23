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

    public Utente login(String cf, String password) {
        String query = "SELECT * FROM utenti WHERE CF = ? AND pw = ?";
        try(Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, cf);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Utente utente = new Utente(
                        rs.getString("CF"),
                        rs.getString("pw"),
                        rs.getString("ruolo"),
                        rs.getString("nomeCognome"),
                        rs.getDate("dataDiNascita").toLocalDate(),
                        rs.getString("luogoDiNascita"),
                        rs.getString("sesso"),
                        rs.getString("mail"),
                        rs.getString("diabetologo_rif")
                    );
                    return utente;
                }
            }

        } catch (SQLException e) {
	    	e.printStackTrace();
	    }
        return null;
    }

    public List<Utente> getPeopleByRole(String role) {
        List<Utente> lista = new ArrayList<>();
        String query = "SELECT * FROM utenti WHERE ruolo = ?";
        try(Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, role);

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Utente utente = new Utente(
                        rs.getString("CF"),
                        rs.getString("pw"),
                        rs.getString("ruolo"),
                        rs.getString("nomeCognome"),
                        rs.getDate("dataDiNascita").toLocalDate(),
                        rs.getString("luogoDiNascita"),
                        rs.getString("sesso"),
                        rs.getString("mail"),
                        rs.getString("diabetologo_rif")
                    );
                    lista.add(utente);
                }
            }

        } catch (SQLException e) {
	    	e.printStackTrace();
	    }
        return lista;
    }
}
