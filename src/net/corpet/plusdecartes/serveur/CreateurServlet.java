package net.corpet.plusdecartes.serveur;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Random;

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
		
		@SuppressWarnings("unchecked")
		Map<String,String[]> parametres = req.getParameterMap();
		String nom = parametres.get("concept")[0];
		
		Entity concept = null;
		
		resp.setContentType("text/plain");

		if (nom != null) {
			concept = new Entity("Concept", nom);
			
			for (String param : parametres.keySet()){
				concept.setProperty(param, parametres.get(param)[0]);
			}
			
			Date dateCreation = new Date();
			concept.setProperty("dateCreation", dateCreation);
			
			Random de = new Random();
			concept.setProperty("ordreHasard", de.nextDouble());
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
