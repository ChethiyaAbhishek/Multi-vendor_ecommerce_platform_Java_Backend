/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.BatchDetails;
import hibernate.Color;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.User;
import hibernate.Wishlist;
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
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "AddToWishlist", urlPatterns = {"/AddToWishlist"})
public class AddToWishlist extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        HttpSession session = request.getSession(false);

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        if (session != null && session.getAttribute("user") != null) {

            if (Util.checkInteger(request.getParameter("product_id"))
                    && Integer.parseInt(request.getParameter("product_id")) > 0) {

                int product_id = Integer.parseInt(request.getParameter("product_id"));

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Product product = (Product) ses.load(Product.class, product_id);

                if (product != null) {

                    User user = (User) session.getAttribute("user");

                    Criteria criteria = ses.createCriteria(Wishlist.class);
                    criteria.add(Restrictions.eq("user", user));
                    criteria.add(Restrictions.eq("product", product));

                    if (criteria.list().isEmpty()) {

                        Wishlist wishlist = new Wishlist();
                        wishlist.setUser(user);
                        wishlist.setProduct(product);
                        ses.save(wishlist);

                        ses.beginTransaction().commit();

                    } else {
                        responseObject.addProperty("msg", "4");
                    }

                    responseObject.addProperty("status", Boolean.TRUE);

                } else {
                    responseObject.addProperty("msg", "3");
                }

                ses.close();

            } else {
                responseObject.addProperty("msg", "2");
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(responseObject));

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        HttpSession session = request.getSession(false);

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        if (session != null && session.getAttribute("user") != null) {

            String fNum = new Gson().fromJson(request.getReader(), JsonObject.class).get("firstResult").getAsString();

            if (!fNum.isEmpty() || Util.checkInteger(fNum)) {
                int firstResult = Integer.parseInt(fNum);

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = ses.createCriteria(Wishlist.class);
                criteria.add(Restrictions.eq("user", session.getAttribute("user")));
                criteria.setFirstResult(firstResult);
                criteria.setMaxResults(4);

                if (!criteria.list().isEmpty()) {

                    List wlist = new ArrayList();

                    List<Wishlist> ws = criteria.list();

                    for (Wishlist w : ws) {
                        w.setUser(null);
                        wlist.add(w);

                        Criteria batchdetails = ses.createCriteria(BatchDetails.class);
                        batchdetails.add(Restrictions.eq("batch", w.getProduct().getBatch()));

                        responseObject.add("details_" + w.getProduct().getBatch().getBid(), new Gson().toJsonTree(batchdetails.list()));
                        List<BatchDetails> bd = batchdetails.list();

                        for (BatchDetails batchd : bd) {

                            Criteria colors = ses.createCriteria(Color.class);
                            colors.add(Restrictions.eq("batchDetails", batchd));
                            colors.setMaxResults(2);

                            if (!colors.list().isEmpty()) {
                                responseObject.add("colors_" + batchd.getBd_id(), new Gson().toJsonTree(colors.list()));
                            }
                        }
                        
                        List<String> imgList = new ArrayList<>();
                        imgList.add("http://127.0.0.1:8080/WEB_II_VIVA_Project/product-images/" + w.getProduct().getPro_id() + "/image1.png");
                        imgList.add("http://127.0.0.1:8080/WEB_II_VIVA_Project/product-images/" + w.getProduct().getPro_id() + "/image2.png");
                        responseObject.add("imgs_" + w.getWid(), new Gson().toJsonTree(imgList));

                    }

                    responseObject.add("item", new Gson().toJsonTree(wlist));
                    responseObject.addProperty("itemCount", new Gson().toJson(wlist.size()));
                    responseObject.addProperty("status", Boolean.TRUE);

                } else {
                    responseObject.addProperty("msg", "2");
                }

                ses.close();

            } else {
                responseObject.addProperty("msg", "3");
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(responseObject));

    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);
        Gson gson = new Gson();

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("user") != null) {

            String wID = request.getParameter("wishlistId");

            if (wID.isEmpty()) {
                responseObject.addProperty("msg", "2");
            } else if (!Util.checkInteger(wID)) {
                responseObject.addProperty("msg", "3");
            } else {

                int wishlistID = Integer.parseInt(wID);

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = ses.createCriteria(Wishlist.class);
                criteria.add(Restrictions.eq("wid", wishlistID));
                criteria.add(Restrictions.eq("user", ((User) session.getAttribute("user"))));

                if (!criteria.list().isEmpty()) {

                    Wishlist wishlist = (Wishlist) criteria.uniqueResult();

                    ses.delete(wishlist);
                    ses.beginTransaction().commit();
                    ses.clear();

                    responseObject.addProperty("status", Boolean.TRUE);

                } else {
                    responseObject.addProperty("msg", "4");
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
