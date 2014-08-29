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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class CreateurServlet extends HttpServlet {
	@Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

		DatastoreService base = DatastoreServiceFactory.getDatastoreService();
		resp.setContentType("text/plain");

		@SuppressWarnings("unchecked")
		Map<String,String[]> parametres = req.getParameterMap();
		String[] noms = parametres.get("concept"), types = parametres.get("type");
		String type = null, nom = null;
		boolean aType = true, aNom = true;
		if (types==null) {
			aType = false;
		} else {
			type = types[0];
		}
		if (noms==null) {
			aNom = false;
		} else {
			nom = noms[0];
		}
		Key ancetre = null;
		
		if (aType) {
			ancetre = KeyFactory.createKey("Concept", type);
			try {
				base.get(ancetre);
			} catch (EntityNotFoundException e) {
				Entity nouveauType = new Entity(ancetre);
				nouveauType.setProperty("concept", type);
				nouveauType.setProperty("type","type");
				
				Date dateCreation = new Date();
				nouveauType.setProperty("dateCreation", dateCreation);
				
				base.put(nouveauType);
				resp.getWriter().println("Type de concepts \"" + type  + "\" ajouté.");
			}
		}
		
		Entity concept;
		if (aNom) {
			
			concept = aType?new Entity("Concept", nom, ancetre):new Entity("Concept", nom);
			
			for (String param : parametres.keySet()){
				concept.setProperty(param, parametres.get(param)[0]);
			}
			
			Date dateCreation = new Date();
			concept.setProperty("dateCreation", dateCreation);
			
			Random de = new Random();
			concept.setProperty("ordreHasard", de.nextDouble());
		
			try {
				base.get(concept.getKey());
				resp.getWriter().println("Concept \"" + nom  + "\" déjà présent dans la base.");
			} 
			catch (EntityNotFoundException e) {
				base.put(concept);
		}	
			resp.getWriter().println("Concept \"" + nom  + "\" ajouté.");
		}
		

	}
}
