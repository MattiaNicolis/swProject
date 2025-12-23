package application.utils;

import java.time.LocalDate;

import application.model.Glicemia;
import application.model.Utente;
import application.service.AdminService;

public class DiabetologoUtils {
    public static String getColoreSeverita(Glicemia g) {
		int val = g.getValore();
		String ind = g.getIndicazioni();
		
		// ROSSO (Grave)
		if ((ind.equals("Pre pasto") && (val < 60 || val > 150)) ||
			(ind.equals("Post pasto") && val > 200)) {
			return "#FF0000";
		}
		// ARANCIONE (Attenzione)
		if ((ind.equals("Pre pasto") && (val < 70 || val > 140)) ||
			(ind.equals("Post pasto") && val > 190)) {
			return "#FFA500";
		}
		// GIALLO (Lieve)
		if ((ind.equals("Pre pasto") && (val < 80 || val > 130)) ||
			(ind.equals("Post pasto") && val > 180)) {
			return "#FFD700"; // Gold
		}
		
		return null; // Tutto ok
	}

	public static int calcolaGiorniNonCompilati(Utente p) {
		int giorniRitardo = 0;
		
		// Controlliamo gli ultimi 3 giorni
		for (int i = 0; i < 3; i++) {
			LocalDate dataCheck = LocalDate.now().minusDays(i);
			
			int attive = AdminService.loadTerapieAttiveByCfAndData(p.getCf(), dataCheck);
			int fatte = AdminService.loadTerapieSoddisfatteByCfAndData(p.getCf(), dataCheck);
			
			if (attive == 0) {
				// Se quel giorno non aveva terapie, non è un ritardo. Interrompiamo.
				break; 
			}
			
			if (fatte < attive) {
				// FALLIMENTO: quel giorno non ha completato tutto
				giorniRitardo++;
			} else {
				// SUCCESSO: quel giorno è stato bravo. La catena di ritardi si spezza.
				break; 
			}
		}
		return giorniRitardo;
	}
}
