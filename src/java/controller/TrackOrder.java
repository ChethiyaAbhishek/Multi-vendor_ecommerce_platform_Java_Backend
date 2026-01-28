/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Invoice;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "TrackOrder", urlPatterns = {"/TrackOrder"})
public class TrackOrder extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        Gson gson = new Gson();

        HttpSession session = request.getSession();

        if (session != null && session.getAttribute("user") != null) {

            User user = (User) session.getAttribute("user");
            
            String invoiceId = request.getParameter("invId");

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Criteria c1 = ses.createCriteria(Invoice.class);
            c1.add(Restrictions.eq("invoice_no", invoiceId));
            c1.add(Restrictions.eq("user", user));

            if (!c1.list().isEmpty()) {
                
                Invoice invoice = (Invoice) c1.uniqueResult(); 
                
                responseObject.addProperty("status", Boolean.TRUE);
                responseObject.addProperty("invoiceNo", invoice.getInvoice_no());
                responseObject.addProperty("deliveryStatus", invoice.getDeliveryStatus().getDes_id());

            } else {
                responseObject.addProperty("msg", "2");
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

}
