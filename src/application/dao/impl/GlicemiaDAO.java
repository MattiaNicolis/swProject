package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.Glicemia;
import application.model.Utente;

public class GlicemiaDAO implements application.dao.interfaces.GlicemiaDAOinterface {
    
    public List<Glicemia> getAllGlicemia() {
        List<Glicemia> lista = new ArrayList<>();
        String query = "SELECT * FROM glicemia ORDER BY giorno DESC, orario ASC";
		try(Connection conn = Database.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {
			
			while(rs.next()) {
                Glicemia glicemia = new Glicemia(
                    rs.getString("CF"),
					rs.getInt("valore"),
					rs.getDate("giorno").toLocalDate(),
					rs.getString("orario"),
					rs.getString("indicazioni"));
                lista.add(glicemia);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return lista;
    }

    public boolean creaGlicemia(Glicemia g) {
        String query = "INSERT INTO glicemia (CF, valore, giorno, orario, indicazioni) VALUES (?, ?, ?, ?, ?)";
	    try (Connection conn = Database.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(query)) {

	        stmt.setString(1, g.getCf());
	        stmt.setInt(2, g.getValore());
	        stmt.setDate(3, java.sql.Date.valueOf(g.getGiorno()));
	        stmt.setString(4, g.getOrario());
	        stmt.setString(5, g.getIndicazioni());

	        int rows = stmt.executeUpdate();

	        if (rows > 0) {
				return true;
	        } else {
	        	return false;
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
            return false;
	    }
    }

	public List<Glicemia> getGlicemiaByPaziente(Utente p) {
		List<Glicemia> lista = new ArrayList<>();
		String query = "SELECT * FROM glicemia WHERE CF = ? ORDER BY giorno ASC, orario ASC";
		try (Connection conn = Database.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, p.getCf());

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Glicemia glicemia = new Glicemia(
						p.getCf(),
						rs.getInt("valore"),
						rs.getDate("giorno").toLocalDate(),
						rs.getString("orario"),
						rs.getString("indicazioni")
					);
					lista.add(glicemia);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
	}
}