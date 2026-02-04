package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.Paziente;

public class PazienteDAO implements application.dao.interfaces.PazienteDAOinterface {

    public Paziente loginPaziente(String cf, String password) {
        String query = "SELECT * FROM pazienti WHERE CF = ? AND pw = ?";
        try(Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, cf);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Paziente paziente = new Paziente(
                        rs.getString("cf"),
                        rs.getString("pw"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getDate("dataDiNascita").toLocalDate(),
                        rs.getString("luogoDiNascita"),
                        rs.getString("sesso"),
                        rs.getString("mail"),
                        rs.getString("diabetologoRif")
                    );
                    return paziente;
                }
            }

        } catch (SQLException e) {
	    	e.printStackTrace();
	    }
        return null;
    }

    public List<Paziente> getPazienti() {
        List<Paziente> lista = new ArrayList<>();
        String query = "SELECT * FROM pazienti";
        try(Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Paziente paziente = new Paziente(
                        rs.getString("cf"),
                        rs.getString("pw"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getDate("dataDiNascita").toLocalDate(),
                        rs.getString("luogoDiNascita"),
                        rs.getString("sesso"),
                        rs.getString("mail"),
                        rs.getString("diabetologoRif")
                    );
                    lista.add(paziente);
                }
            }

        } catch (SQLException e) {
	    	e.printStackTrace();
	    }
        return lista;
    }
}
