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
class ExceptionPasAssezDEntites extends Throwable{
	Filter filtre;
	int nombre;
	List<Key> liste;
	
	public ExceptionPasAssezDEntites(Filter f, int n, List<Key> l){
		filtre = f;
		nombre = n;
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
			List<Key> clesPoten;
			try {
				clesPoten = cleConceptAuHasard(base, i+1);
				for (Key cle: clesPoten){
					if (!cles.contains(cle)){
						cles.add(cle);
						break;
					}
				}
			} catch (ExceptionPasAssezDEntites e) {
				resp.getWriter().println("Il n'y a pas assez de concepts correspondant à la requête");
			}
		}
		resp.getWriter().println("Voici "+ cles.size() + " concept(s) tiré(s) au hasard dans la base : ");
		for (Key cle : cles){
			String nom;
			try {
				nom = (String) base.get(cle).getProperty("concept");
				resp.getWriter().println(nom);
			} catch (EntityNotFoundException e) {}
		}		
	}
	
	private List<Key> cleConceptAuHasard(DatastoreService base, int nombre) throws ExceptionPasAssezDEntites {
		return cleConceptAuHasard(base, nombre, null);
	}
	private List<Key> cleConceptAuHasard(DatastoreService base, int nombre, Filter filtre) throws ExceptionPasAssezDEntites {
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
        
        if (pq.countEntities(FetchOptions.Builder.withLimit(nombre))<nombre){
        	q.setFilter(filtre);
        	pq = base.prepare(q);
        }
        
        List<Entity> concepts = pq.asList(FetchOptions.Builder.withLimit(nombre));
        List<Key> res = new ArrayList<Key>();
        for (Entity e : concepts){
        	res.add(e.getKey());
        }
        if (concepts.size()<nombre){
        	throw new ExceptionPasAssezDEntites(filtre, nombre, res);
        } else {
        	return res;
        }
	}
}
