package net.corpet.plusdecartes.serveur;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * L'implémentation côté serveur du service
 */
@SuppressWarnings("serial")
public class CarteServlet extends HttpServlet {

	@Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain");
        resp.getWriter().println("{ \"name\": \"World\" }");
    }
}
