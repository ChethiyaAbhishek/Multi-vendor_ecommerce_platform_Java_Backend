/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.DeliveryStatus;
import hibernate.HibernateUtil;
import hibernate.Invoice;
import hibernate.InvoiceItems;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "Orders", urlPatterns = {"/Orders"})
public class Orders extends HttpServlet {

    private static final int PENDING_STATUS = 1;
    private static final int PROCESSING_STATUS = 2;
    private static final int SHIPPED_STATUS = 3;
    private static final int DELIVERED_STATUS = 4;

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

        HttpSession session = request.getSession(false);

        if (session != null && (session.getAttribute("seller") != null || session.getAttribute("admin") != null)) {

            User user = null;
            boolean seller = true;

            if (session.getAttribute("seller") != null) {
                user = (User) session.getAttribute("seller");
            } else if (session.getAttribute("admin") != null) {
                seller = false;
            }

            String fr = request.getParameter("firstResult");
            int firstResult;

            if (fr.isEmpty() || !Util.checkInteger(fr)) {
                firstResult = 0;
            } else {
                firstResult = Integer.parseInt(fr);
            }

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Criteria c1 = ses.createCriteria(Invoice.class);
            c1.addOrder(Order.asc("deliveryStatus"));

            responseObject.addProperty("orderItemsCount", c1.list().size());

            c1.setFirstResult(firstResult);
            c1.setMaxResults(4);

            List<Invoice> l1 = c1.list();
            List<InvoiceItems> userItems = new ArrayList<>();
            for (Invoice invoice : l1) {

                Criteria c2 = ses.createCriteria(InvoiceItems.class);
                c2.add(Restrictions.eq("invoice", invoice));

                List<InvoiceItems> l2 = c2.list();

                for (InvoiceItems invoiceItems : l2) {
                    if (seller) {
                        if (invoiceItems.getProduct().getUser().getUid() == user.getUid()) {
                            userItems.add(invoiceItems);
                        }
                    } else {
                        userItems.add(invoiceItems);
                    }
                }
            }

            for (InvoiceItems userItem : userItems) {
                userItem.getProduct().setUser(null);
                userItem.getInvoice().setUser(null);
            }

            responseObject.addProperty("status", Boolean.TRUE);
            responseObject.add("orderItems", gson.toJsonTree(userItems));

            ses.close();

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        Gson gson = new Gson();

        HttpSession session = request.getSession(false);

        if (session != null && (session.getAttribute("seller") != null || session.getAttribute("admin") != null)) {

            User user = null;
            boolean seller = true;

            if (session.getAttribute("seller") != null) {
                user = (User) session.getAttribute("seller");
            } else if (session.getAttribute("admin") != null) {
                seller = false;
            }

            JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);

            String invoiceItemId = requestObject.get("invItemId").getAsString();

            if (invoiceItemId.isEmpty() || !Util.checkInteger(invoiceItemId)) {
                responseObject.addProperty("msg", "2");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                InvoiceItems invoiceItems = (InvoiceItems) ses.get(InvoiceItems.class, Integer.parseInt(invoiceItemId));

                if (invoiceItems != null) {

                    DeliveryStatus processingStatus = (DeliveryStatus) ses.get(DeliveryStatus.class, Orders.PROCESSING_STATUS);

                    if (seller) {
                        if (invoiceItems.getInvoice().getDeliveryStatus().getDes_status().equals("PENDING")) {

                            invoiceItems.getInvoice().setDeliveryStatus(processingStatus);
                            ses.update(invoiceItems);

                            responseObject.addProperty("status", true);
                        } else {
                            responseObject.addProperty("msg", "2");
                        }
                    } else {

                        DeliveryStatus shippedStatus = (DeliveryStatus) ses.get(DeliveryStatus.class, Orders.SHIPPED_STATUS);
                        DeliveryStatus deliveredStatus = (DeliveryStatus) ses.get(DeliveryStatus.class, Orders.DELIVERED_STATUS);

                        int status = invoiceItems.getInvoice().getDeliveryStatus().getDes_id();

                        if (status == Orders.PENDING_STATUS) {
                            invoiceItems.getInvoice().setDeliveryStatus(processingStatus);
                        } else if (status == Orders.PROCESSING_STATUS) {
                            invoiceItems.getInvoice().setDeliveryStatus(shippedStatus);
                        } else if (status == Orders.SHIPPED_STATUS) {
                            invoiceItems.getInvoice().setDeliveryStatus(deliveredStatus);
                        }

                        ses.update(invoiceItems);
                        responseObject.addProperty("status", true);
                    }

                    ses.beginTransaction().commit();
                } else {
                    responseObject.addProperty("msg", "The order item could not be found.");
                }
                ses.close();
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
