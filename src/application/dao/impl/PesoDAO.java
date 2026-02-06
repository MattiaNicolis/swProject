package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.database.Database;
import application.model.Peso;

public class PesoDAO implements application.dao.interfaces.PesoDAOinterface {
    
	//Restituisce la lista dei pesi corporei di un determinato paziente a partire dal suo cf
    public List<Peso> getPesoByCf(String cf) {
        List<Peso> lista = new ArrayList<>();
        String query = "SELECT * FROM peso WHERE cf = ? AND giorno >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) ORDER BY giorno ASC;";
        try (Connection conn = Database.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, cf);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Peso peso = new Peso(
						rs.getInt("id"),
                        rs.getString("cf"),
						rs.getDouble("valore"),
						rs.getDate("giorno").toLocalDate()
					);
					lista.add(peso);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lista;
    }

	//Crea una nuovo istanza di peso per un determinato paziente e lo inserisce nel database
	public boolean creaPeso(Peso p) {
		String query = "INSERT INTO peso (cf, valore, giorno) VALUES (?, ?, ?)";
		try (Connection conn = Database.getConnection(); 
			PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, p.getCf());
			stmt.setDouble(2, p.getValore());
			stmt.setDate(3, java.sql.Date.valueOf(p.getGiorno()));
				
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

	//Modifica una determinata istanza di peso
	public boolean aggiornaPeso(Peso p) {
		String query = "UPDATE peso SET valore = ?, giorno = ? WHERE id = ?";
		try (Connection conn = Database.getConnection();
			PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setDouble(1, p.getValore());
			stmt.setDate(2, java.sql.Date.valueOf(p.getGiorno()));
			stmt.setInt(3, p.getId());

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
}