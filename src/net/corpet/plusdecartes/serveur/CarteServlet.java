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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;


/**
 * L'implémentation côté serveur du service.
 */
@SuppressWarnings("serial")
class ExceptionPasDEntite extends Throwable {
  Filter filtre;
  
  public ExceptionPasDEntite(Filter f) {
    filtre = f;
  }
}

@SuppressWarnings("serial")
public class CarteServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    DatastoreService base = DatastoreServiceFactory.getDatastoreService();
        
    try {
      String nom = (String) base.get(cleConceptAuHasard(base)).getProperty("concept");
      resp.getWriter().println("Voici un concept tiré au hasard dans la base : " + nom);
    } catch (ExceptionPasDEntite|EntityNotFoundException e) {
      resp.getWriter().println("Il n'y a pas de concept correspondant à la requête");
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
