package net.corpet.plusdecartes.serveur;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;

@SuppressWarnings("serial")
class ExceptionPasAssezDEntites extends Throwable{
	Filter filtre;
	Set<Key> clesDisponibles;
	
	public ExceptionPasAssezDEntites(Filter f, Set<Key> l){
		filtre = f;
		clesDisponibles = l;
	}
}

class Concept {
	private Key cle;
	private Entity entite = null;

	// Constructeur à partir d'une clé DataStore
	public Concept(Key c){
		cle = c;
	}
	
	public Key getCle(){
		return cle;
	}
	public Entity getEntite(){
		return entite;
	}
	
	public void recupEntite(DatastoreService base) throws EntityNotFoundException{
		if (entite==null){
			entite = base.get(cle);
		}
	}
}

class Requete {
	private Filter filtre;
	private Set<Key> clesDejaTirees = new HashSet<Key>();
	
	public Requete(){
		filtre = null;
	}
	public Requete(Filter f){
		filtre = f;
	}
	
	public void reinitClesTirees(){
		clesDejaTirees = new HashSet<Key>();
	}
	
	public Concept conceptSuivant(DatastoreService base) throws ExceptionPasAssezDEntites {
		Random de = new Random();
        double seuil = de.nextDouble();
        
        FilterPredicate filtreParDefaut = 
        		new FilterPredicate("ordreHasard",Query.FilterOperator.GREATER_THAN, seuil);
        Filter filtreFinal = filtre==null?filtreParDefaut:CompositeFilterOperator.and(filtre, filtreParDefaut);
        
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
        			throw new ExceptionPasAssezDEntites(filtre, clesDejaTirees);
        		}
        		q.setFilter(filtre);
        		pq = base.prepare(q);
        		fetchOptions = FetchOptions.Builder.withLimit(1);
        		boucle = true;
        	}
        	
        	fetchOptions.startCursor(concepts.getCursor());
        	concepts = pq.asQueryResultList(fetchOptions);
        } while (res==null || clesDejaTirees.contains(res));
        clesDejaTirees.add(res);
        return new Concept(res);
	}
}
