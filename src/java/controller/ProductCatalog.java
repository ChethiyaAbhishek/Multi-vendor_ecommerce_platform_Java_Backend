/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.BatchDetails;
import hibernate.Color;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.ProductReview;
import hibernate.User;
import java.io.IOException;
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
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "ProductCatalog", urlPatterns = {"/ProductCatalog"})
public class ProductCatalog extends HttpServlet {

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

        if (Util.checkInteger(request.getParameter("product_id"))
                && Integer.parseInt(request.getParameter("product_id")) > 0) {

            int productId = Integer.parseInt(request.getParameter("product_id"));

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Criteria statusCriteria = ses.createCriteria(ActiveStatus.class);
            statusCriteria.add(Restrictions.eq("astatus", "Active"));
            ActiveStatus status = (ActiveStatus) statusCriteria.list().get(0);

            Criteria criteria = ses.createCriteria(Product.class);
            criteria.add(Restrictions.eq("pro_id", productId));
            criteria.add(Restrictions.eq("activeStatus", status));

            if (criteria.list().isEmpty()) {
                responseObject.addProperty("msg", "Product not found.");
            } else {

                Product product = (Product) criteria.list().get(0);

                if (product.getSubCategory().getActiveStatus().getAstatus().equals("Active")) {

                    responseObject.add("product", gson.toJsonTree(criteria.list().get(0)));

                    Criteria batchDetails = ses.createCriteria(BatchDetails.class);
                    batchDetails.add(Restrictions.eq("batch", product.getBatch()));

                    List<BatchDetails> bDetails = batchDetails.list();

                    for (BatchDetails details : bDetails) {
                        Criteria colors = ses.createCriteria(Color.class);
                        colors.add(Restrictions.eq("batchDetails", details));

                        responseObject.add("colors_" + details.getBd_id(), gson.toJsonTree(colors.list()));
                    }

                    Criteria reviews = ses.createCriteria(ProductReview.class);
                    reviews.add(Restrictions.eq("product", product));

                    Criteria relatedItems = ses.createCriteria(Product.class);
                    relatedItems.add(Restrictions.eq("subCategory", product.getSubCategory()));
                    relatedItems.add(Restrictions.ne("pro_id", product.getPro_id()));
                    relatedItems.setMaxResults(4);

                    List<Product> ritems = relatedItems.list();

                    for (Product ritem : ritems) {
                        Criteria batchd = ses.createCriteria(BatchDetails.class);
                        batchd.add(Restrictions.eq("batch", ritem.getBatch()));

                        List<BatchDetails> bdetails = batchd.list();

                        responseObject.add("relatedItems_batch_" + ritem.getPro_id(), gson.toJsonTree(bdetails));

                        for (BatchDetails bd : bdetails) {
                            Criteria rbcolors = ses.createCriteria(Color.class);
                            rbcolors.add(Restrictions.eq("batchDetails", bd));

                            if (!rbcolors.list().isEmpty()) {
                                responseObject.add("relatedItems_colors_" + bd.getBd_id(), gson.toJsonTree(rbcolors.list()));
                            }
                        }

                        responseObject.add("relatedItems", gson.toJsonTree(ritems));
                    }
                    
                    List<String> imgList = new ArrayList<>();
                    imgList.add("http://127.0.0.1:8080/WEB_II_VIVA_Project/product-images/"+product.getPro_id()+"/image1.png");
                    imgList.add("http://127.0.0.1:8080/WEB_II_VIVA_Project/product-images/"+product.getPro_id()+"/image2.png");
                    imgList.add("http://127.0.0.1:8080/WEB_II_VIVA_Project/product-images/"+product.getPro_id()+"/image3.png");
                    imgList.add("http://127.0.0.1:8080/WEB_II_VIVA_Project/product-images/"+product.getPro_id()+"/image4.png");

                    responseObject.add("batchDetails", gson.toJsonTree(batchDetails.list()));
                    responseObject.add("productImgs", gson.toJsonTree(imgList));
                    responseObject.add("reviews", gson.toJsonTree(reviews.list()));
                    responseObject.addProperty("status", Boolean.TRUE);
                } else {
                    responseObject.addProperty("msg", "The product is not available at the moment.");
                }
            }

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

        HttpSession session = request.getSession();

        if (session != null && session.getAttribute("user") != null) {

            JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);

            String name = requestObject.get("name").getAsString();
            String comment = requestObject.get("comment").getAsString();
            String starCount = requestObject.get("star_count").getAsString();
            String productId = requestObject.get("productId").getAsString();

            if (name.isEmpty()) {
                responseObject.addProperty("msg", "2");
            } else if (comment.isEmpty()) {
                responseObject.addProperty("msg", "3");
            } else if (!Util.checkInteger(starCount) || Integer.parseInt(starCount) < 0) {
                responseObject.addProperty("msg", "4");
            } else if (productId.isEmpty() || !Util.checkInteger(productId)) {
                responseObject.addProperty("msg", "5");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Product product = (Product) ses.load(Product.class, Integer.parseInt(productId));

                if (product != null) {

                    ProductReview review = new ProductReview();
                    review.setReview(comment);
                    review.setStar_count(Integer.parseInt(starCount));
                    review.setDate_time(new Date());
                    review.setProduct(product);
                    review.setUser(((User) session.getAttribute("user")));

                    ses.save(review);
                    ses.beginTransaction().commit();

                    responseObject.addProperty("status", Boolean.TRUE);

                } else {
                    responseObject.addProperty("msg", "6");
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
