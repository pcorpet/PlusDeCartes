package net.corpet.plusdecartes.serveur;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;


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
        Random de = new Random();
        
        double seuil = de.nextDouble();
        Filter filtreParDefaut = 
        		new FilterPredicate("ordreHasard",Query.FilterOperator.GREATER_THAN, seuil);
        
        Query q = new Query("Concept").setFilter(filtreParDefaut).addSort("ordreHasard");
        PreparedQuery pq = base.prepare(q);
        
        if (pq.countEntities(FetchOptions.Builder.withLimit(1))==0){
        	q = new Query("Concept").addSort("ordreHasard");
        	pq = base.prepare(q);
        }
        
        List<Entity> concepts = pq.asList(FetchOptions.Builder.withLimit(1));
        if (concepts.isEmpty()){
        	resp.getWriter().println("Il n'y a pas d'entité correspondant à votre requête.");
        } else {
        	String nom = (String) concepts.get(0).getProperty("concept");
            resp.getWriter().println("Voici un concept tiré au hasard dans la base : " + nom);
        }

    }
}
