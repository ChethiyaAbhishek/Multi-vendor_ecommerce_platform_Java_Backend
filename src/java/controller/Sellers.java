/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.BatchDetails;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.User;
import hibernate.UserStatus;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "Sellers", urlPatterns = {"/Sellers"})
public class Sellers extends HttpServlet {

    private static final int SELLER_STATUS = 2;
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

        if (session != null && session.getAttribute("admin") != null) {

            Session ses = HibernateUtil.getSessionFactory().openSession();

            UserStatus userStatus = (UserStatus) ses.get(UserStatus.class, Sellers.SELLER_STATUS);

            Criteria c1 = ses.createCriteria(User.class);
            c1.add(Restrictions.eq("userStatus", userStatus));

            List<User> sellerList = c1.list();
            for (User user : sellerList) {

                Criteria c2 = ses.createCriteria(Product.class);
                c2.add(Restrictions.eq("user", user));

                List<Product> productList = c2.list();
                for (Product product : productList) {

                    Criteria c3 = ses.createCriteria(BatchDetails.class);
                    c3.add(Restrictions.eq("batch", product.getBatch()));

                    responseObject.add("batchdetails_" + product.getPro_id(), gson.toJsonTree(c3.list()));

                    product.setUser(null);
                }
                responseObject.add("product_" + user.getEmail(), gson.toJsonTree(productList));
                responseObject.addProperty("productCount_" + user.getEmail(), productList.size());

                user.setMobile("0000000000");
                user.setPassword("*****");
                user.setVcode("****");
            }

            responseObject.add("sellers", gson.toJsonTree(sellerList));
            responseObject.addProperty("status", Boolean.TRUE);

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        try {

            JsonObject responseObject = new JsonObject();
            responseObject.addProperty("status", Boolean.FALSE);

            Gson gson = new Gson();

            HttpSession session = request.getSession(false);

            if (session != null && session.getAttribute("admin") != null) {

                JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);

                String sellerId = requestObject.get("sellerId").getAsString();

                if (sellerId.isEmpty()) {
                    responseObject.addProperty("msg", "An error occured.");
                } else if (!Util.checkInteger(sellerId)) {
                    responseObject.addProperty("msg", "Received seller id is not valid.");
                } else if (Integer.parseInt(sellerId) < 1) {
                    responseObject.addProperty("msg", "An Cerror occured.");
                } else {
                    Session ses = HibernateUtil.getSessionFactory().openSession();

                    User seller = (User) ses.get(User.class, Integer.parseInt(sellerId));

                    if (seller != null) {

                        if (seller.getUserStatus().getUsid() == Sellers.SELLER_STATUS) {

                            ActiveStatus active = (ActiveStatus) ses.get(ActiveStatus.class, Sellers.ACTIVE_STATUS);
                            ActiveStatus inactive = (ActiveStatus) ses.get(ActiveStatus.class, Sellers.INACTIVE_STATUS);

                            if (seller.getActiveStatus().getAid() == Sellers.ACTIVE_STATUS) {
                                seller.setActiveStatus(inactive);
                            } else if (seller.getActiveStatus().getAid() == Sellers.INACTIVE_STATUS) {
                                seller.setActiveStatus(active);
                            }

                            ses.update(seller);
                            ses.beginTransaction().commit();
                            responseObject.addProperty("status", Boolean.TRUE);

                        } else {
                            responseObject.addProperty("msg", "Selected user is not a registered seller.");
                        }

                    } else {
                        responseObject.addProperty("msg", "Seller not found.");
                    }
                }
            } else {
                responseObject.addProperty("msg", "1");
            }

            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(responseObject));

        } catch (Exception e) {
            System.out.println("An error occured.");
        }
    }

}
