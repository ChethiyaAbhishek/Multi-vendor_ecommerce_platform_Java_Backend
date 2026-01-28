/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.BatchDetails;
import hibernate.DeliveryStatus;
import hibernate.HibernateUtil;
import hibernate.Invoice;
import hibernate.InvoiceItems;
import hibernate.Product;
import hibernate.Size;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "VendorDashboard", urlPatterns = {"/VendorDashboard"})
public class VendorDashboard extends HttpServlet {

    private static final int DELIVERED_STATUS = 4;
    private static final int ACTIVE_STATUS = 1;
    private static final int INACTIVE_STATUS = 2;

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

        if (session != null && session.getAttribute("seller") != null) {

            User user = (User) session.getAttribute("seller");

            Session ses = HibernateUtil.getSessionFactory().openSession();

            //SUMMARY
            Criteria c1 = ses.createCriteria(Product.class);
            c1.add(Restrictions.eq("user", user));
            responseObject.addProperty("itemCount", c1.list().size());

            DeliveryStatus deliveryStatus = (DeliveryStatus) ses.get(DeliveryStatus.class, VendorDashboard.DELIVERED_STATUS);

            Criteria c2 = ses.createCriteria(Invoice.class);
            Criteria c3 = ses.createCriteria(Invoice.class);
            c2.add(Restrictions.ne("deliveryStatus", deliveryStatus));
            c3.add(Restrictions.eq("deliveryStatus", deliveryStatus));

            double totalEarning = 0.0;

            List<Invoice> l1 = c2.list();
            int orderCount = 0;
            for (Invoice invoice : l1) {

                Criteria c4 = ses.createCriteria(InvoiceItems.class);
                c4.add(Restrictions.eq("invoice", invoice));

                List<InvoiceItems> l2 = c4.list();
                for (InvoiceItems invoiceItems : l2) {
                    if (invoiceItems.getProduct().getUser().getUid() == user.getUid()) {
                        orderCount++;
                        totalEarning += (invoiceItems.getBatchDetails().getPrice() * invoiceItems.getQty());
                    }
                }
            }
            responseObject.addProperty("ongoingOrderCount", orderCount);

            List<Invoice> l2 = c3.list();
            int completedCount = 0;
            for (Invoice invoice : l2) {

                Criteria c4 = ses.createCriteria(InvoiceItems.class);
                c4.add(Restrictions.eq("invoice", invoice));

                List<InvoiceItems> l3 = c4.list();
                for (InvoiceItems invoiceItems : l3) {
                    if (invoiceItems.getProduct().getUser().getUid() == user.getUid()) {
                        completedCount++;
                        totalEarning += (invoiceItems.getBatchDetails().getPrice() * invoiceItems.getQty());
                    }
                }
            }
            responseObject.addProperty("completedOrderCount", completedCount);
            responseObject.addProperty("totalEarnings", totalEarning);
            responseObject.addProperty("status", Boolean.TRUE);

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

            String fr = requestObject.get("firstResult").getAsString();
            int firstResult;

            if (fr.isEmpty() || !Util.checkInteger(fr)) {
                firstResult = 0;
            } else {
                firstResult = Integer.parseInt(fr);
            }

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Criteria c1 = ses.createCriteria(Product.class);
            if (seller) {
                c1.add(Restrictions.eq("user", user));
            }
            c1.addOrder(Order.asc("pro_id"));

            responseObject.addProperty("itemsCount", c1.list().size());

            c1.setFirstResult(firstResult);
            c1.setMaxResults(4);

            List<Product> l1 = c1.list();
            for (Product product : l1) {

                Criteria c2 = ses.createCriteria(BatchDetails.class);
                c2.add(Restrictions.eq("batch", product.getBatch()));

                responseObject.add("batchDetails_" + product.getPro_id(), gson.toJsonTree(c2.list()));

                product.setUser(null);
            }

            responseObject.addProperty("status", Boolean.TRUE);
            responseObject.add("products", gson.toJsonTree(l1));

            ses.close();

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

            String proId = requestObject.get("proId").getAsString();
            String size = requestObject.get("size").getAsString();

            if (proId.isEmpty() || !Util.checkInteger(proId)) {
                responseObject.addProperty("msg", "2");
            } else if (size.isEmpty()) {
                responseObject.addProperty("msg", "2");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Product product = (Product) ses.get(Product.class, Integer.parseInt(proId));

                if (product != null) {

                    ActiveStatus inactiveStatus = (ActiveStatus) ses.get(ActiveStatus.class, VendorDashboard.INACTIVE_STATUS);

                    ActiveStatus activeStatus = (ActiveStatus) ses.get(ActiveStatus.class, VendorDashboard.ACTIVE_STATUS);

                    if (product.getActiveStatus().getAstatus().equals(activeStatus.getAstatus())) {

                        product.setActiveStatus(inactiveStatus);

                        ses.update(product);

                        responseObject.addProperty("status", Boolean.TRUE);
                    } else if (product.getActiveStatus().getAstatus().equals(inactiveStatus.getAstatus())) {

                        Criteria c3 = ses.createCriteria(Size.class);
                        c3.add(Restrictions.eq("size", size));

                        if (c3.list().isEmpty()) {
                            responseObject.addProperty("msg", "2");
                        } else {

                            Size siz = (Size) c3.uniqueResult();

                            Criteria c2 = ses.createCriteria(BatchDetails.class);
                            c2.add(Restrictions.eq("batch", product.getBatch()));
                            c2.add(Restrictions.eq("size", siz));

                            BatchDetails batchDetails = (BatchDetails) c2.uniqueResult();

                            if (batchDetails.getQty() > 0) {
                                product.setActiveStatus(activeStatus);
                                ses.update(product);
                                responseObject.addProperty("status", Boolean.TRUE);
                            } else {
                                responseObject.addProperty("msg", "The product is not available.");
                            }
                        }
                    }

                    ses.beginTransaction().commit();
                    ses.clear();

                } else {
                    responseObject.addProperty("msg", "2");
                }
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
