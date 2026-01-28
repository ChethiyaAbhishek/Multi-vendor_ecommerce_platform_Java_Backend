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
import hibernate.InvoiceItems;
import hibernate.Product;
import hibernate.ProductReview;
import hibernate.SubCategory;
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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "Index", urlPatterns = {"/Index"})
public class Index extends HttpServlet {

    private static final int MAX_RESULTS = 12;
    private static final int ACTIVE_STATUS = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();

        Gson gson = new Gson();

        Session ses = HibernateUtil.getSessionFactory().openSession();

        ActiveStatus activeStatus = (ActiveStatus) ses.get(ActiveStatus.class, Index.ACTIVE_STATUS);

        Criteria c1 = ses.createCriteria(Product.class);
        c1.add(Restrictions.eq("activeStatus", activeStatus));

        // New arrivals
        if (request.getParameter("newarrIndex") != null) {
            c1.addOrder(Order.desc("pro_id"));
            c1.setFirstResult(Integer.parseInt(request.getParameter("newarrIndex")));
            c1.setMaxResults(Index.MAX_RESULTS);

            List<Product> nrList = c1.list();
            for (Product product : nrList) {

                Criteria c2 = ses.createCriteria(BatchDetails.class);
                c2.add(Restrictions.eq("batch", product.getBatch()));
                BatchDetails details = (BatchDetails) c2.list().get(0);

                responseObject.addProperty("newArrivals_" + product.getPro_id(), details.getPrice());

                product.setUser(null);
            }
            responseObject.add("newArrivals", gson.toJsonTree(nrList));
        }

        // Trending
        if (request.getParameter("trendingIndex") != null) {

            Criteria c2 = ses.createCriteria(InvoiceItems.class);

            ProjectionList plist = Projections.projectionList();
            plist.add(Projections.groupProperty("product"));
            plist.add(Projections.rowCount(), "count");

            c2.setProjection(plist);
            c2.addOrder(Order.desc("count"));

            c2.setFirstResult(Integer.parseInt(request.getParameter("trendingIndex")));
            c2.setMaxResults(Index.MAX_RESULTS);

            List<Object[]> trList = c2.list();
            List<Product> trProductList = new ArrayList<>();
            for (Object[] object : trList) {
                Product p = (Product) object[0];

                Criteria c3 = ses.createCriteria(BatchDetails.class);
                c3.add(Restrictions.eq("batch", p.getBatch()));
                BatchDetails details = (BatchDetails) c3.list().get(0);

                responseObject.addProperty("trending_" + p.getPro_id(), details.getPrice());

                p.setUser(null);
                trProductList.add(p);
            }
            responseObject.add("trending", gson.toJsonTree(trProductList));
        }

        // Top Rated
        if (request.getParameter("topRatedIndex") != null) {

            Criteria c2 = ses.createCriteria(ProductReview.class);

            ProjectionList plist = Projections.projectionList();
            plist.add(Projections.groupProperty("product"));
            plist.add(Projections.avg("star_count"), "count");

            c2.setProjection(plist);
            c2.addOrder(Order.desc("count"));

            c2.setFirstResult(Integer.parseInt(request.getParameter("topRatedIndex")));
            c2.setMaxResults(Index.MAX_RESULTS);

            List<Object[]> trList = c2.list();
            List<Product> tratedProductList = new ArrayList<>();
            for (Object[] object : trList) {
                Product p = (Product) object[0];

                Criteria c3 = ses.createCriteria(BatchDetails.class);
                c3.add(Restrictions.eq("batch", p.getBatch()));
                BatchDetails details = (BatchDetails) c3.list().get(0);

                responseObject.addProperty("topRated_" + p.getPro_id(), details.getPrice());

                p.setUser(null);
                tratedProductList.add(p);
            }
            responseObject.add("topRated", gson.toJsonTree(tratedProductList));
        }

        // Deal of the day
//        if (request.getParameter("dealIndex") != null) {
//
//            Criteria c2 = ses.createCriteria(InvoiceItems.class);
//
//            ProjectionList plist = Projections.projectionList();
//            plist.add(Projections.groupProperty("product"));
//            plist.add(Projections.rowCount(), "count");
//
//            c2.setProjection(plist);
//            c2.addOrder(Order.desc("count"));
//
//            c2.setFirstResult(Integer.parseInt(request.getParameter("trendingIndex")));
//            c2.setMaxResults(Index.MAX_RESULTS);
//
//            List<Object[]> trList = c2.list();
//            List<Product> trProductList = new ArrayList<>();
//            for (Object[] object : trList) {
//                Product p = (Product) object[0];
//
//                Criteria c3 = ses.createCriteria(BatchDetails.class);
//                c3.add(Restrictions.eq("batch", p.getBatch()));
//                BatchDetails details = (BatchDetails) c3.list().get(0);
//
//                responseObject.addProperty("trending_" + p.getPro_id(), details.getPrice());
//
//                p.setUser(null);
//                trProductList.add(p);
//            }
//            responseObject.add("trending", gson.toJsonTree(trProductList));
//        }
        // All Products
        if (request.getParameter("allProducts") != null) {

            c1.addOrder(Order.desc("batch"));
            c1.setMaxResults(Index.MAX_RESULTS);

            List<Product> pList = c1.list();
            HashMap<Integer, Double> pPriceList = new HashMap<>();
            for (Product product : pList) {
                product.setUser(null);

                Criteria c3 = ses.createCriteria(BatchDetails.class);
                c3.add(Restrictions.eq("batch", product.getBatch()));
                BatchDetails details = (BatchDetails) c3.list().get(0);

                pPriceList.put(product.getPro_id(), details.getPrice());

            }
            responseObject.add("allProducts", gson.toJsonTree(pList));
            responseObject.add("allProductsPrices", gson.toJsonTree(pPriceList));

            Criteria c2 = ses.createCriteria(InvoiceItems.class);

            ProjectionList plist = Projections.projectionList();
            plist.add(Projections.groupProperty("product"));
            plist.add(Projections.rowCount(), "count");

            c2.setProjection(plist);
            c2.addOrder(Order.desc("count"));

            List<Object[]> trSubList = c2.list();
            HashMap<String, ArrayList> subcategoryData = new HashMap<>();
            HashMap<String, HashMap<Integer, Double>> productPrices = new HashMap<>();
            ArrayList<SubCategory> s = new ArrayList<>();

            for (Object[] object : trSubList) {
                Product p = (Product) object[0];
                if (!s.contains(p.getSubCategory())) {
                    c1.add(Restrictions.eq("subCategory", p.getSubCategory()));
                    s.add(p.getSubCategory());

                    HashMap<Integer, Double> pPrices = new HashMap<>();
                    ArrayList<Product> subProducts = new ArrayList<>();
                    List<Product> pr = c1.list();
                    for (Product product : pr) {
                        product.setUser(null);
                        subProducts.add(product);

                        Criteria c3 = ses.createCriteria(BatchDetails.class);
                        c3.add(Restrictions.eq("batch", product.getBatch()));
                        BatchDetails details = (BatchDetails) c3.list().get(0);

                        pPrices.put(product.getPro_id(), details.getPrice());
                    }

                    if (s.size() <= 3) {
                        subcategoryData.put(p.getSubCategory().getSub_name(), subProducts);
                        productPrices.put(p.getSubCategory().getSub_name(), pPrices);
                    } else {
                        break;
                    }
                }
            }

            responseObject.add("productCategories", gson.toJsonTree(subcategoryData));
            responseObject.add("productCategoryPrices", gson.toJsonTree(productPrices));

        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

}
