/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.Batch;
import hibernate.BatchDetails;
import hibernate.Brand;
import hibernate.BrandHasVarient;
import hibernate.Category;
import hibernate.Color;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.ProductReview;
import hibernate.Size;
import hibernate.SubCategory;
import hibernate.Varient;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "AdvancedSearch", urlPatterns = {"/AdvancedSearch"})
public class AdvancedSearch extends HttpServlet {

    private static final int ACTIVE_STATUS_ID = 1;
    private static final int MAX_RESULT = 12;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        String text = request.getParameter("search");

        Gson gson = new Gson();
        Session ses = HibernateUtil.getSessionFactory().openSession();

        ActiveStatus activeStatus = (ActiveStatus) ses.get(ActiveStatus.class, AdvancedSearch.ACTIVE_STATUS_ID);

        Criteria c1 = ses.createCriteria(Product.class);

        if (!text.isEmpty()) {

            Criteria c4 = ses.createCriteria(SubCategory.class);
            c4.add(Restrictions.ilike("sub_name", text));

            SubCategory subCategory = (SubCategory) c4.uniqueResult();

            c1.add(Restrictions.or(
                    Restrictions.ilike("title", text, MatchMode.ANYWHERE),
                    Restrictions.eq("subCategory", subCategory)));
        }

        c1.add(Restrictions.eq("activeStatus", activeStatus));
        c1.setFirstResult(0);
        c1.setMaxResults(4);
        c1.addOrder(Order.desc("pro_id"));

        List<Product> pList = c1.list();
        for (Product product : pList) {
            responseObject.addProperty("img_" + product.getPro_id(), "http://127.0.0.1:8080/WEB_II_VIVA_Project/product-images/" + product.getPro_id() + "/image1.png");

            Criteria c2 = ses.createCriteria(BatchDetails.class);
            c2.add(Restrictions.eq("batch", product.getBatch()));
            responseObject.add("batchDetails_" + product.getPro_id(), gson.toJsonTree(c2.list()));

            Criteria c3 = ses.createCriteria(ProductReview.class);
            c3.add(Restrictions.eq("product", product));

            List<ProductReview> prList = c3.list();
            ArrayList<Integer> starList = new ArrayList<>();
            for (ProductReview productReview : prList) {
                starList.add(productReview.getStar_count());
            }
            responseObject.add("reviews_" + product.getPro_id(), gson.toJsonTree(starList));

            product.setUser(null);
        }

        responseObject.addProperty("status", Boolean.TRUE);
        responseObject.add("searchResults", gson.toJsonTree(pList));

        ses.close();

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
        JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);
        Session ses = HibernateUtil.getSessionFactory().openSession();

        Criteria c1 = ses.createCriteria(Product.class);

        ActiveStatus activeStatus = (ActiveStatus) ses.get(ActiveStatus.class, AdvancedSearch.ACTIVE_STATUS_ID);

        if (requestObject.has("cat") && !requestObject.get("cat").isJsonNull()) {
            String categoryId = requestObject.get("cat").getAsString();

            Criteria c2 = ses.createCriteria(SubCategory.class);

            Criteria c3 = ses.createCriteria(Category.class);
            c3.add(Restrictions.eq("cat_id", Integer.parseInt(categoryId)));
            c3.add(Restrictions.eq("activeStatus", activeStatus));

            if (!c3.list().isEmpty()) {

                Category category = (Category) c3.uniqueResult();
                c2.add(Restrictions.eq("category", category));
                c2.add(Restrictions.eq("activeStatus", activeStatus));
            }

            if (!c2.list().isEmpty()) {
                List<SubCategory> sublist = c2.list();
                c1.add(Restrictions.in("subCategory", sublist));
            }
        }

        if (requestObject.has("subcat") && !requestObject.get("subcat").isJsonNull()) {
            String subcatId = requestObject.get("subcat").getAsString();

            Criteria c3 = ses.createCriteria(SubCategory.class);
            c3.add(Restrictions.eq("sub_id", Integer.parseInt(subcatId)));
            c3.add(Restrictions.eq("activeStatus", activeStatus));

            SubCategory subCategory = (SubCategory) c3.uniqueResult();
            c1.add(Restrictions.eq("subCategory", subCategory));
        }

        if (requestObject.has("sizes") && !requestObject.get("sizes").isJsonNull()) {
            String sizeId = requestObject.get("sizes").getAsString();

            Criteria c3 = ses.createCriteria(Size.class);
            c3.add(Restrictions.eq("size_id", Integer.parseInt(sizeId)));

            Size size = (Size) c3.uniqueResult();
            Criteria c4 = ses.createCriteria(BatchDetails.class);
            c4.add(Restrictions.eq("size", size));

            List<BatchDetails> bdlist = c4.list();
            List<Batch> batches = new ArrayList<>();
            for (BatchDetails batchDetails : bdlist) {
                if (batchDetails.getBatch().getActiveStatus().getAid() == AdvancedSearch.ACTIVE_STATUS_ID) {
                    batches.add(batchDetails.getBatch());
                }
            }

            if (!batches.isEmpty()) {
                c1.add(Restrictions.in("batch", batches));
            }
        }

        if (requestObject.has("color") && !requestObject.get("color").isJsonNull()) {
            String colorId = requestObject.get("color").getAsString();

            Color color = (Color) ses.get(Color.class, Integer.parseInt(colorId));

            c1.add(Restrictions.eq("batch", color.getBatchDetails().getBatch()));

        }

        if (requestObject.has("priceMin") && requestObject.has("priceMax")) {
            String priceMin = requestObject.get("priceMin").getAsString();
            String priceMax = requestObject.get("priceMax").getAsString();

            if (Util.checkDouble(priceMin) && Util.checkDouble(priceMax)) {
                Criteria c2 = ses.createCriteria(BatchDetails.class);
                c2.add(Restrictions.ge("price", Double.valueOf(priceMin)));
                c2.add(Restrictions.le("price", Double.valueOf(priceMax)));

                List<BatchDetails> bdList = c2.list();
                Set<Batch> batches = new HashSet<>();
                for (BatchDetails batchDetails : bdList) {
                    batches.add(batchDetails.getBatch());
                }

                if (!batches.isEmpty()) {
                    c1.add(Restrictions.in("batch", batches));
                }
            }
        }

        if (requestObject.has("sort")) {
            String sort = requestObject.get("sort").getAsString();

            Criteria c3 = ses.createCriteria(BrandHasVarient.class);

            if (sort.equals("1")) {
                c1.addOrder(Order.asc("title"));
            } else if (sort.equals("2")) {
                c1.addOrder(Order.desc("title"));
            } else if (sort.equals("3") || sort.equals("4")) {

                Criteria c2 = ses.createCriteria(Brand.class);
                if (sort.equals("3")) {
                    c2.addOrder(Order.asc("bname"));
                } else if (sort.equals("4")) {
                    c2.addOrder(Order.desc("bname"));
                }
                List<Brand> brand = c2.list();

                c3.add(Restrictions.in("brand", brand));
                List<BrandHasVarient> brandHasVarients = c3.list();

                if (!brandHasVarients.isEmpty()) {
                    c1.add(Restrictions.in("brandHasVarient", brandHasVarients));
                }
            } else if (sort.equals("5") || sort.equals("6")) {

                Criteria c2 = ses.createCriteria(Varient.class);
                if (sort.equals("3")) {
                    c2.addOrder(Order.asc("vname"));
                } else if (sort.equals("4")) {
                    c2.addOrder(Order.desc("vname"));
                }
                List<Varient> varient = c2.list();

                c3.add(Restrictions.in("varient", varient));
                List<BrandHasVarient> brandHasVarients = c3.list();

                if (!brandHasVarients.isEmpty()) {
                    c1.add(Restrictions.in("brandHasVarient", brandHasVarients));
                }
            }
        }

        c1.add(Restrictions.eq("activeStatus", activeStatus));

        responseObject.addProperty("resultsCount", c1.list().size());

        if (requestObject.has("firstResult")) {
            if (Util.checkInteger(requestObject.get("firstResult").getAsString())) {
                c1.setFirstResult(requestObject.get("firstResult").getAsInt());
            } else {
                c1.setFirstResult(0);
            }
            c1.setMaxResults(AdvancedSearch.MAX_RESULT);
        }

        List<Product> products = c1.list();
        for (Product product : products) {

            Criteria c2 = ses.createCriteria(BatchDetails.class);
            c2.add(Restrictions.eq("batch", product.getBatch()));

            if (!c2.list().isEmpty()) {
                responseObject.add("batchDetails_" + product.getPro_id(), gson.toJsonTree(c2.list()));

                c2.setMaxResults(4);
                List<BatchDetails> bd = c2.list();
                Set<Color> colors = new HashSet<>();
                for (BatchDetails batchDetails : bd) {
                    Criteria c3 = ses.createCriteria(Color.class);
                    c3.add(Restrictions.eq("batchDetails", batchDetails));
                    c3.setMaxResults(1);

                    Color c = (Color) c3.uniqueResult();

                    if (c != null) {
                        colors.add(c);
                    }
                }

                responseObject.add("colors_" + product.getPro_id(), gson.toJsonTree(colors));

            }

            product.setUser(null);
        }

        responseObject.addProperty("status", Boolean.TRUE);
        responseObject.add("productList", gson.toJsonTree(products));
        ses.close();

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
