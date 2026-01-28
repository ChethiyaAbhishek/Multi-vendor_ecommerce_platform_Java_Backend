/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Address;
import hibernate.HibernateUtil;
import hibernate.Invoice;
import hibernate.InvoiceItems;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "UserHistory", urlPatterns = {"/UserHistory"})
public class UserHistory extends HttpServlet {

    private static final int MAXCOUNT = 8;

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

            String fResult = request.getParameter("firstResult");

            int firstResult = 0;

            if (!fResult.isEmpty() || Util.checkInteger(fResult)) {
                firstResult = Integer.parseInt(fResult);
            }

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Criteria c1 = ses.createCriteria(Invoice.class);
            c1.add(Restrictions.eq("user", user));
            
            responseObject.addProperty("ItemsCount", c1.list().size());
            
            c1.setFirstResult(firstResult);
            c1.setMaxResults(UserHistory.MAXCOUNT);

            List<Invoice> invoiceList = c1.list();

            for (Invoice invoice : invoiceList) {
                invoice.setUser(null);

                Criteria c2 = ses.createCriteria(InvoiceItems.class);
                c2.add(Restrictions.eq("invoice", invoice));

                int itemCount = 0;
                double total = 0;

                List<InvoiceItems> invoiceItemList = c2.list();
                for (InvoiceItems invoiceItems : invoiceItemList) {
                    itemCount++;
                    total += invoiceItems.getQty() * invoiceItems.getBatchDetails().getPrice();
                }

                responseObject.addProperty("itemCount_" + invoice.getInvoice_no(), itemCount);
                responseObject.addProperty("invoiceTotal_" + invoice.getInvoice_no(), total);

            }

            responseObject.addProperty("status", Boolean.TRUE);
            responseObject.add("invoices", gson.toJsonTree(invoiceList));

            ses.close();
        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

            JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);
            String invoiceId = requestObject.get("invoiceId").getAsString();

            if (invoiceId.isEmpty()) {
                responseObject.addProperty("msg", "2");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Invoice invoice = (Invoice) ses.get(Invoice.class, invoiceId);

                ArrayList userDataList = new ArrayList();
                userDataList.add(invoice.getUser().getFname() + " " + invoice.getUser().getLname());
                userDataList.add(invoice.getUser().getMobile());

                Criteria c1 = ses.createCriteria(InvoiceItems.class);
                c1.add(Restrictions.eq("invoice", invoice));

                List<InvoiceItems> invoiceItemList = c1.list();

                for (InvoiceItems items : invoiceItemList) {
                    items.getProduct().setUser(null);
                    items.getInvoice().setUser(null);
                }

                invoice.setUser(null);

                responseObject.addProperty("status", Boolean.TRUE);
                responseObject.add("invoiceItems", gson.toJsonTree(invoiceItemList));
                responseObject.add("invoice", gson.toJsonTree(invoice));
                responseObject.add("user", gson.toJsonTree(userDataList));

                ses.close();
            }
        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
