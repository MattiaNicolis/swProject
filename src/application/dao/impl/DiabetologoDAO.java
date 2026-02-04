package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import application.database.Database;
import application.model.Diabetologo;

public class DiabetologoDAO implements application.dao.interfaces.DiabetologoDAOinterface {

    public Diabetologo loginDiabetologo(String cf, String password) {
        String query = "SELECT * FROM diabetologi WHERE CF = ? AND pw = ?";
        try(Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, cf);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Diabetologo diabetologo = new Diabetologo(
                        rs.getString("cf"),
                        rs.getString("pw"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getDate("dataDiNascita").toLocalDate(),
                        rs.getString("luogoDiNascita"),
                        rs.getString("sesso"),
                        rs.getString("mail")
                    );
                    return diabetologo;
                }
            }

        } catch (SQLException e) {
	    	e.printStackTrace();
	    }
        return null;
    }

    public String getNomeCognomeDiabetologoByCf(String cf) {
        String query = "SELECT nome, cognome FROM diabetologi WHERE CF = ?";
        try(Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, cf);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nome") + " " + rs.getString("cognome");
                }
            }

        } catch (SQLException e) {
        	e.printStackTrace();
        }
        return null;
    }
}
