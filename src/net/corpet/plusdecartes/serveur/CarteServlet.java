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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;


/**
 * L'implémentation côté serveur du service
 */
@SuppressWarnings("serial")
class ExceptionPasAssezDEntites extends Throwable{
	Filter filtre;
	List<Key> liste;
	
	public ExceptionPasAssezDEntites(Filter f, List<Key> l){
		filtre = f;
		liste = l;
	}
}

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
		
		for (int i=0;i<nombre;i++){
			try {
				cles.add(cleConceptAuHasard(base, cles));
			} catch (ExceptionPasAssezDEntites e) {
				resp.getWriter().println("Il n'y a que " + e.liste.size() + " correspondant à la requête.");
			}
		}
		resp.getWriter().println("Voici "+ cles.size() + " concept(s) tiré(s) au hasard dans la base : ");
		for (Key cle : cles){
			resp.getWriter().println(cle.getName());
		}		
	}
	
	private Key cleConceptAuHasard(DatastoreService base, List<Key> interdites) throws ExceptionPasAssezDEntites {
		return cleConceptAuHasard(base, interdites, null);
	}
	private Key cleConceptAuHasard(DatastoreService base, List<Key> interdites, Filter filtre) 
			throws ExceptionPasAssezDEntites {
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
        
        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
        QueryResultList<Entity> concepts = pq.asQueryResultList(fetchOptions);
        Key res = null;
        boolean boucle = false;
        do {
        	try {
        		res = concepts.get(0).getKey();
        	}
        	catch (IndexOutOfBoundsException e) {
        		if (boucle) {
        			throw new ExceptionPasAssezDEntites(filtre, interdites);
        		}
        		q.setFilter(filtre);
        		pq = base.prepare(q);
        		fetchOptions = FetchOptions.Builder.withLimit(1);
        		concepts = pq.asQueryResultList(fetchOptions);
        		boucle = true;
        	}
        	fetchOptions.startCursor(concepts.getCursor());
        	concepts = pq.asQueryResultList(fetchOptions);
        } while (res==null || interdites.contains(res));
        return res;
	}
}
