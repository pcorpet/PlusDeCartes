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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

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
			ancetre = KeyFactory.createKey("Type", type);
			try {
				base.get(ancetre);
			} catch (EntityNotFoundException e) {
				Entity nouveauType = new Entity(ancetre);
				
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
				if (!param.equals(type)){
					concept.setProperty(param, parametres.get(param)[0]);	
				}
			}
			
			Date dateCreation = new Date();
			concept.setProperty("dateCreation", dateCreation);
			
			Random de = new Random();
			concept.setProperty("ordreHasard", de.nextDouble());
		
			Query q = new Query("Concept").setKeysOnly().setFilter(new FilterPredicate("concept", FilterOperator.EQUAL, nom));
			PreparedQuery pq = base.prepare(q);
			if (!pq.asList(FetchOptions.Builder.withLimit(1)).isEmpty()){
				resp.getWriter().println("Concept \"" + nom  + "\" déjà présent dans la base.");
			} else {
				base.put(concept);
				resp.getWriter().println("Concept \"" + nom  + "\" ajouté.");
			}
		}
	}
}
