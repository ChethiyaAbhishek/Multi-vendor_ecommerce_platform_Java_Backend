/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.Batch;
import hibernate.BatchDetails;
import hibernate.Brand;
import hibernate.BrandHasVarient;
import hibernate.Color;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.Size;
import hibernate.SizeHasCategory;
import hibernate.SubCategory;
import hibernate.User;
import hibernate.UserStatus;
import hibernate.Varient;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@MultipartConfig
@WebServlet(name = "ProductRegistration", urlPatterns = {"/ProductRegistration"})
public class ProductRegistration extends HttpServlet {

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

        if (session != null && session.getAttribute("seller") != null) {

            User user = (User) session.getAttribute("seller");

            String proId = request.getParameter("pid");

            if (proId.isEmpty() || !Util.checkInteger(proId)) {
                responseObject.addProperty("msg", "2");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Product product = (Product) ses.get(Product.class, Integer.parseInt(proId));

                if (product != null) {

                    Criteria c1 = ses.createCriteria(BatchDetails.class);
                    c1.add(Restrictions.eq("batch", product.getBatch()));

                    List<BatchDetails> bdlist = c1.list();
                    for (BatchDetails batchDetails : bdlist) {
                        Criteria c2 = ses.createCriteria(Color.class);
                        c2.add(Restrictions.eq("batchDetails", batchDetails));

                        responseObject.add("color_"+batchDetails.getBd_id(), gson.toJsonTree(c2.list()));
                    }

                    responseObject.add("batchDetails", gson.toJsonTree(c1.list()));
                    responseObject.add("product", gson.toJsonTree(product));
                    responseObject.addProperty("status", Boolean.TRUE);

                } else {
                    responseObject.addProperty("msg", "Product not found.");
                }

                ses.close();

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

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        Gson gson = new Gson();

        HttpSession session = request.getSession();

        if (session != null && session.getAttribute("seller") != null) {

            User user = (User) session.getAttribute("seller");
            Session ses = HibernateUtil.getSessionFactory().openSession();

            UserStatus userStatus = (UserStatus) ses.get(UserStatus.class, 2);

            Criteria u = ses.createCriteria(User.class);
            u.add(Restrictions.eq("email", user.getEmail()));
            u.add(Restrictions.eq("userStatus", userStatus));

            if (!u.list().isEmpty()) {
                String itemName = request.getParameter("itemName");
                String shortDesc = request.getParameter("shortDesc");
                String longDesc = request.getParameter("longDesc");
                String subCat = request.getParameter("subCat");
                String brand = request.getParameter("brand");
                String varient = request.getParameter("varient");
//        String qty = request.getParameter("qty");
//        String price = request.getParameter("price");
//        String discount = request.getParameter("discount");
//                String color1 = request.getParameter("color1");
//                String color2 = request.getParameter("color2");
//                String color3 = request.getParameter("color3");
//                String color4 = request.getParameter("color4");
//        String selectedSizes = request.getParameter("selectedSizes");
//        String batchDetails = request.getParameter("batchDetails");
                Part img1 = request.getPart("img1");
                Part img2 = request.getPart("img2");
                Part img3 = request.getPart("img3");
                Part img4 = request.getPart("img4");

                if (itemName.isEmpty()) {
                    responseObject.addProperty("msg", "Item name is required.");
                } else if (shortDesc.isEmpty()) {
                    responseObject.addProperty("msg", "Short description is required.");
                } else if (shortDesc.length() > 500) {
                    responseObject.addProperty("msg", "Short description is too long. It should be within 500 characters.");
                } else if (longDesc.isEmpty()) {
                    responseObject.addProperty("msg", "Long description is required.");
                } else if (!Util.checkInteger(subCat)) {
                    responseObject.addProperty("msg", "Selected sub category is not valid.");
                } else if (!Util.checkInteger(brand)) {
                    responseObject.addProperty("msg", "Selected brand is not valid.");
                } else if (!Util.checkInteger(varient)) {
                    responseObject.addProperty("msg", "Selected varient is not valid.");
                } else {

                    JsonArray batchDetails = gson.fromJson(request.getParameter("batchDetails"), JsonArray.class);

                    if (batchDetails.size() < 1) {
                        responseObject.addProperty("msg", "Item sizes and the respective details are required.");
                    } else {

                        boolean canContinue = true;

                        for (JsonElement batchDetail : batchDetails) {
                            JsonObject jobject = batchDetail.getAsJsonObject();

                            String sizeId = jobject.get("sizeId").getAsString();
                            String size = jobject.get("size").getAsString();
                            String price = jobject.get("price").getAsString();
                            String qty = jobject.get("qty").getAsString();
                            String discount = jobject.get("discount").getAsString();
                            String color1 = jobject.get("color1").getAsString();
                            String color2 = jobject.get("color2").getAsString();
                            String color3 = jobject.get("color3").getAsString();
                            String color4 = jobject.get("color4").getAsString();

                            if (qty.isEmpty()) {
                                responseObject.addProperty("msg", "Item quantity is required. [Size: " + size + "]");
                                canContinue = false;
                                break;
                            } else if (!Util.checkInteger(qty)) {
                                responseObject.addProperty("msg", "Provided quantity is not valid. [Size: " + size + "]");
                                canContinue = false;
                                break;
                            } else if (Integer.parseInt(qty) < 1) {
                                responseObject.addProperty("msg", "Provided quantity is not valid. [Size: " + size + "]");
                                canContinue = false;
                                break;
                            } else if (price.isEmpty()) {
                                responseObject.addProperty("msg", "Item price is required. [Size: " + size + "]");
                                canContinue = false;
                                break;
                            } else if (!Util.checkDouble(price)) {
                                responseObject.addProperty("msg", "Provided item price is not valid. [Size: " + size + "]");
                                canContinue = false;
                                break;
                            } else if (Double.valueOf(price) < 1) {
                                responseObject.addProperty("msg", "Provided item price is not valid. [Size: " + size + "]");
                                canContinue = false;
                                break;
                            } else if (!Util.checkColorCode(color1)) {
                                responseObject.addProperty("msg", "Provided color(01) is not valid. [Size: " + size + "]");
                                canContinue = false;
                                break;
                            } else if (!Util.checkColorCode(color2)) {
                                responseObject.addProperty("msg", "Provided color(02) is not valid. [Size: " + size + "]");
                                canContinue = false;
                                break;
                            } else if (!Util.checkColorCode(color3)) {
                                responseObject.addProperty("msg", "Provided color(03) is not valid. [Size: " + size + "]");
                                canContinue = false;
                                break;
                            } else if (!Util.checkColorCode(color4)) {
                                responseObject.addProperty("msg", "Provided color(04) is not valid. [Size: " + size + "]");
                                canContinue = false;
                                break;
                            }
                        }

                        if (canContinue) {

                            if (img1.getSubmittedFileName() == null) {
                                responseObject.addProperty("msg", "Image(01) is required.");
                            } else if (img2.getSubmittedFileName() == null) {
                                responseObject.addProperty("msg", "Image(02) is required.");
                            } else if (img3.getSubmittedFileName() == null) {
                                responseObject.addProperty("msg", "Image(03) is required.");
                            } else if (img4.getSubmittedFileName() == null) {
                                responseObject.addProperty("msg", "Image(04) is required.");
                            } else {

                                SubCategory scat = (SubCategory) ses.get(SubCategory.class, Integer.parseInt(subCat));

                                if (scat != null) {

                                    Brand b = (Brand) ses.get(Brand.class, Integer.parseInt(brand));

                                    if (b != null) {

                                        Varient v = (Varient) ses.get(Varient.class, Integer.parseInt(varient));

                                        if (v != null) {

                                            boolean sendNotFound = false;
                                            boolean sendNotMatch = false;
                                            String erroredSize = null;

                                            for (JsonElement batchDetail : batchDetails) {
                                                JsonObject jobject = batchDetail.getAsJsonObject();

                                                String sizeId = jobject.get("sizeId").getAsString();
                                                String size = jobject.get("size").getAsString();

                                                Size s2 = (Size) ses.get(Size.class, Integer.parseInt(sizeId));

                                                if (s2 != null) {
                                                    Criteria c = ses.createCriteria(SizeHasCategory.class);
                                                    c.add(Restrictions.eq("size", s2));
                                                    c.add(Restrictions.eq("category", scat.getCategory()));

                                                    if (c.list() != null) {
                                                        sendNotMatch = false;
                                                    } else {
                                                        sendNotMatch = true;
                                                        erroredSize = size;
                                                        break;
                                                    }
                                                    sendNotFound = false;
                                                } else {
                                                    sendNotFound = true;
                                                    erroredSize = size;
                                                    break;
                                                }
                                            }

                                            if (sendNotFound) {
                                                responseObject.addProperty("msg", "Selected size is invalid. [Size: " + erroredSize + "]");
                                            } else if (sendNotMatch) {
                                                responseObject.addProperty("msg", "Selected sizes do not match with the selected category. [Size: " + erroredSize + "]");
                                            } else {

                                                Criteria c1 = ses.createCriteria(BrandHasVarient.class);
                                                c1.add(Restrictions.eq("brand", b));
                                                c1.add(Restrictions.eq("varient", v));

                                                BrandHasVarient brandHasVarientObject;

                                                if (c1.list().isEmpty()) {
                                                    BrandHasVarient bv = new BrandHasVarient();
                                                    bv.setBrand(b);
                                                    bv.setVarient(v);
                                                    brandHasVarientObject = bv;
                                                    ses.save(bv);
                                                } else {
                                                    brandHasVarientObject = (BrandHasVarient) c1.list().get(0);
                                                }
 
                                                ActiveStatus status = new ActiveStatus();
                                                status.setAid(1);
                                                status.setAstatus("Active");

                                                Batch batch = new Batch();
                                                batch.setDate_time(new Date());
                                                batch.setActiveStatus(status);
                                                int batchId = (int) ses.save(batch);

                                                for (JsonElement batchD : batchDetails) {
                                                    JsonObject jobject = batchD.getAsJsonObject();

                                                    String sizeId = jobject.get("sizeId").getAsString();
                                                    String size = jobject.get("size").getAsString();
                                                    String price = jobject.get("price").getAsString();
                                                    String qty = jobject.get("qty").getAsString();
                                                    String discount = jobject.get("discount").getAsString();

                                                    List<String> colorList = new ArrayList();
                                                    colorList.add(jobject.get("color1").getAsString());
                                                    colorList.add(jobject.get("color2").getAsString());
                                                    colorList.add(jobject.get("color3").getAsString());
                                                    colorList.add(jobject.get("color4").getAsString());

                                                    Size sizeObject = (Size) ses.get(Size.class, Integer.parseInt(sizeId));

                                                    BatchDetails batchDetail = new BatchDetails();
                                                    batchDetail.setBatch(batch);
                                                    batchDetail.setPrice(Double.valueOf(price));
                                                    batchDetail.setQty(Integer.parseInt(qty));

                                                    if (!discount.isEmpty()) {
                                                        if (Util.checkDouble(discount)) {
                                                            batchDetail.setDiscount(Double.valueOf(discount));
                                                        } else {
                                                            responseObject.addProperty("msg", "Provided discount is not valid. [Size: " + size + "]");
                                                            return;
                                                        }
                                                    }

                                                    batchDetail.setSize(sizeObject);
                                                    ses.save(batchDetail);

                                                    Set<String> colorCodes = new HashSet<>();

                                                    for (String clr : colorList) {
                                                        if (!colorCodes.contains(clr)) {
                                                            colorCodes.add(clr);
                                                        }
                                                        System.out.println(clr);
                                                    }

                                                    for (String colorCode : colorCodes) {
                                                        Color color = new Color();
                                                        color.setCode(colorCode);
                                                        color.setBatchDetails(batchDetail);
                                                        ses.save(color);
                                                    }

                                                }

                                                Product product = new Product();
                                                product.setTitle(itemName);
                                                product.setSdescription(shortDesc);
                                                product.setLdescription(longDesc);
                                                product.setBatch(batch);
                                                product.setBrandHasVarient(brandHasVarientObject);
                                                product.setSubCategory(scat);
                                                product.setActiveStatus(status);
                                                product.setUser(user);
                                                int productId = (int) ses.save(product);

                                                ses.beginTransaction().commit();

                                                String appPath = getServletContext().getRealPath("");

                                                String newPath = appPath.replace("build\\web", "web\\product-images");

                                                File productFolder = new File(newPath, String.valueOf(productId));
                                                productFolder.mkdir();

                                                File file1 = new File(productFolder, "image1.png");
                                                Files.copy(img1.getInputStream(), file1.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                                File file2 = new File(productFolder, "image2.png");
                                                Files.copy(img2.getInputStream(), file2.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                                File file3 = new File(productFolder, "image3.png");
                                                Files.copy(img3.getInputStream(), file3.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                                File file4 = new File(productFolder, "image4.png");
                                                Files.copy(img4.getInputStream(), file4.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                                responseObject.addProperty("status", true);
                                                responseObject.addProperty("msg", "Product registered successfully.");

                                            }

                                        } else {
                                            responseObject.addProperty("msg", "Selected varient is invalid.");
                                        }

                                    } else {
                                        responseObject.addProperty("msg", "Selected brand is invalid.");
                                    }

                                } else {
                                    responseObject.addProperty("msg", "Selected sub category is invalid.");
                                }

                            }

                        }

                    }

                }

            } else {
                responseObject.addProperty("msg", "1");
            }
            ses.clear();
            ses.close();

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
