package net.corpet.plusdecartes.serveur;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;


/**
 * L'implémentation côté serveur du service
 */
@SuppressWarnings("serial")
class ExceptionPasDEntite extends Throwable{
	Filter filtre;
	
	public ExceptionPasDEntite(Filter f){
		filtre = f;
	}
}

@SuppressWarnings("serial")
public class CarteServlet extends HttpServlet {

	@Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
		
		resp.setContentType("text/plain");
		DatastoreService base = DatastoreServiceFactory.getDatastoreService();
        int nombre = 1, essais = 0;
        List<Key> cles = new ArrayList<Key>();
		try {
			nombre = Integer.parseInt(req.getParameter("nombre"));
		} catch (NumberFormatException e) {}
		
		while (cles.size()<nombre && essais <= 10*nombre){
			Key cle;
			essais++;
			try {
				cle = cleConceptAuHasard(base);
				if (!cles.contains(cle)) {
					cles.add(cle);
				}
			} catch (ExceptionPasDEntite e) {
				resp.getWriter().println("Il n'y a pas de concept correspondant à la requête");
			}
		}
		if (cles.size()<nombre){
			resp.getWriter().println("Nous n'avons réussi à extraire que "+ cles.size() + " concept(s) au hasard dans la base : ");			
		} else {
			resp.getWriter().println("Voici "+ nombre + " concept(s) tiré(s) au hasard dans la base : ");
		}
		for (Key cle : cles){
			String nom;
			try {
				nom = (String) base.get(cle).getProperty("concept");
				resp.getWriter().println(nom);
			} catch (EntityNotFoundException e) {}
		}		
	}
	
	private Key cleConceptAuHasard(DatastoreService base) throws ExceptionPasDEntite {
		return cleConceptAuHasard(base, null);
	}
	private Key cleConceptAuHasard(DatastoreService base, Filter filtre) throws ExceptionPasDEntite {
		Random de = new Random();
        double seuil = de.nextDouble();
        
        FilterPredicate filtreParDefaut = 
        		new FilterPredicate("ordreHasard",Query.FilterOperator.GREATER_THAN, seuil);
        Filter filtreFinal;
        if (filtre==null){
        	filtreFinal = filtreParDefaut;
        } else {
        	filtreFinal = CompositeFilterOperator.and(filtre, filtreParDefaut);
        }
        Query q = new Query("Concept").setKeysOnly().setFilter(filtreFinal).addSort("ordreHasard");
        PreparedQuery pq = base.prepare(q);
        
        if (pq.countEntities(FetchOptions.Builder.withLimit(1))==0){
        	q.setFilter(filtre);
        	pq = base.prepare(q);
        }
        
        List<Entity> concepts = pq.asList(FetchOptions.Builder.withLimit(1));
        if (concepts.isEmpty()){
        	throw new ExceptionPasDEntite(filtre);
        } else {
        	return concepts.get(0).getKey();
        }
	}
}
