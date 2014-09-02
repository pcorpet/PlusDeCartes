package net.corpet.plusdecartes.serveur;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;

/**
 * L'implémentation côté serveur du service
 */

@SuppressWarnings("serial")
public class CarteServlet extends HttpServlet {

	@Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
		
		resp.setContentType("text/plain");
		DatastoreService base = DatastoreServiceFactory.getDatastoreService();
        int nombre = 1;
        List<Key> cles = new ArrayList<Key>();
		try {
			nombre = Integer.parseInt(req.getParameter("nombre"));
		} catch (NumberFormatException e) {}
		
		Requete requete = new Requete();
		
		try {
			for (int i=0;i<nombre;i++){
				cles.add(requete.conceptSuivant(base).getCle());
				}
		} catch (ExceptionPasAssezDEntites e) {
			resp.getWriter().println("Il n'y a que " + e.clesDisponibles.size() + " résultats correspondant à la requête.");
		}
		resp.getWriter().println("Voici "+ cles.size() + " concept(s) tiré(s) au hasard dans la base : ");
		for (Key cle : cles){
			resp.getWriter().println(cle.getName());
		}		
	}	
}
