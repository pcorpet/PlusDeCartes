package net.corpet.plusdecartes.serveur;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

@SuppressWarnings("serial")
public class CreateurServlet extends HttpServlet {
	@Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		String nom = req.getQueryString();
		if (nom != null) {
			Entity concept = new Entity("Concept", nom);
			
			concept.setProperty("concept", nom);
			
			Date dateCreation = new Date();
			
			concept.setProperty("dateCreation", dateCreation);
		datastore.put(concept);
		}
	}
}
