package net.corpet.plusdecartes.serveur;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;

@SuppressWarnings("serial")
public class CreateurServlet extends HttpServlet {
	@Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

		DatastoreService base = DatastoreServiceFactory.getDatastoreService();
		
		String nom = req.getQueryString();
		Entity concept = null;
		
		resp.setContentType("text/plain");

		if (nom != null) {
			concept = new Entity("Concept", nom);
			concept.setProperty("concept", nom);
			
			Date dateCreation = new Date();
			concept.setProperty("dateCreation", dateCreation);
		}
		try {
			base.get(concept.getKey());
			resp.getWriter().println("Concept \"" + nom  + "\" déjà présent dans la base.");
		} 
		catch (EntityNotFoundException e) {
			base.put(concept);
			
	        resp.getWriter().println("Concept \"" + nom  + "\" ajouté.");
		}
		

	}
}
