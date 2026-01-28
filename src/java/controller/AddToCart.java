/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.Address;
import hibernate.BatchDetails;
import hibernate.Cart;
import hibernate.Color;
import hibernate.DeliveryCost;
import hibernate.HibernateUtil;
import hibernate.Product;
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
@WebServlet(name = "AddToCart", urlPatterns = {"/AddToCart"})
public class AddToCart extends HttpServlet {

    private static final int ACTIVE_STATUS_ID = 1;

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

        JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);
        String qty = requestObject.get("qty").getAsString();
        JsonElement colorElement = requestObject.get("colorSelected");
        String batchDetailsId = requestObject.get("batchDetailsId").getAsString();
        String productId = requestObject.get("productId").getAsString();

        if (colorElement == null || colorElement.isJsonNull()) {
            responseObject.addProperty("msg", "Item color is required.");
        } else {

            String colorSelected = requestObject.get("colorSelected").getAsString();

            if (qty.isEmpty()) {
                responseObject.addProperty("msg", "Quantity is required.");
            } else if (!Util.checkInteger(qty)) {
                responseObject.addProperty("msg", "Invalid quantity.");
            } else if (Integer.parseInt(qty) < 1) {
                responseObject.addProperty("msg", "Quantity must be greater than 0.");
            } else if (colorSelected.isEmpty()) {
                responseObject.addProperty("msg", "Item color is required.");
            } else if (!Util.checkInteger(colorSelected)) {
                responseObject.addProperty("msg", "Invalid color.");
            } else if (Integer.parseInt(colorSelected) < 1) {
                responseObject.addProperty("msg", "Invalid color.");
            } else if (batchDetailsId.isEmpty()) {
                responseObject.addProperty("msg", "Item size is required.");
            } else if (!Util.checkInteger(batchDetailsId)) {
                responseObject.addProperty("msg", "Invalid size.");
            } else if (Integer.parseInt(batchDetailsId) < 1) {
                responseObject.addProperty("msg", "Invalid size.");
            } else if (productId.isEmpty() || !Util.checkInteger(productId)) {
                responseObject.addProperty("msg", "Something went wrong. Please try refreshing the page.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Product product = (Product) ses.get(Product.class, Integer.parseInt(productId));

                if (product != null) {

                    Criteria c1 = ses.createCriteria(BatchDetails.class);
                    c1.add(Restrictions.eq("batch", product.getBatch()));
                    c1.add(Restrictions.eq("bd_id", Integer.parseInt(batchDetailsId)));

                    if (!c1.list().isEmpty()) {

                        BatchDetails batchDetails = (BatchDetails) c1.list().get(0);

                        if (Integer.parseInt(qty) <= batchDetails.getQty()) {

                            Criteria c2 = ses.createCriteria(Color.class);
                            c2.add(Restrictions.eq("color_id", Integer.parseInt(colorSelected)));
                            c2.add(Restrictions.eq("batchDetails", batchDetails));

                            if (!c2.list().isEmpty()) {

                                Color color = (Color) c2.list().get(0);

                                if (session != null && session.getAttribute("user") != null) {
                                    // DB CART    

                                    User user = (User) session.getAttribute("user");

                                    Criteria c3 = ses.createCriteria(Cart.class);
                                    c3.add(Restrictions.eq("product", product));
                                    c3.add(Restrictions.eq("batchDetails", batchDetails));
                                    c3.add(Restrictions.eq("color", color));
                                    c3.add(Restrictions.eq("user", user));

                                    if (c3.list().isEmpty()) {
                                        Cart cart = new Cart();
                                        cart.setQty(Integer.parseInt(qty));
                                        cart.setProduct(product);
                                        cart.setBatchDetails(batchDetails);
                                        cart.setColor(color);
                                        cart.setUser(user);

                                        ses.save(cart);
                                        responseObject.addProperty("status", Boolean.TRUE);
                                        responseObject.addProperty("msg", "Item added.");
                                    } else {
                                        Cart cart = (Cart) c3.list().get(0);
                                        if (cart.getQty() + Integer.parseInt(qty) <= batchDetails.getQty()) {
                                            cart.setQty(cart.getQty() + Integer.parseInt(qty));
                                            ses.update(cart);
                                            responseObject.addProperty("status", Boolean.TRUE);
                                            responseObject.addProperty("msg", "Item already exists. Quantity updated.");
                                        } else {
                                            responseObject.addProperty("msg", "Insufficient Quantity. [Only " + batchDetails.getQty() + " available]");
                                        }
                                    }

                                    ses.beginTransaction().commit();

                                } else if (session != null) {
                                    // SESSION CART

                                    if (session.getAttribute("cart") == null) {
                                        ArrayList<Cart> cartArray = new ArrayList<>();

                                        Cart cart = new Cart();
                                        cart.setQty(Integer.parseInt(qty));
                                        cart.setProduct(product);
                                        cart.setBatchDetails(batchDetails);
                                        cart.setColor(color);
                                        cart.setUser(null);

                                        cartArray.add(cart);

                                        session.setAttribute("cart", cartArray);
                                        response.addCookie(Util.createSessionCookie(session.getId()));

                                        responseObject.addProperty("status", Boolean.TRUE);
                                        responseObject.addProperty("msg", "Item added.");
                                    } else {
                                        ArrayList<Cart> cartArray = (ArrayList<Cart>) session.getAttribute("cart");
                                        Cart cart = null;

                                        boolean setCartItem = true;

                                        for (Cart cartItem : cartArray) {
                                            if (cartItem.getProduct().getPro_id() == product.getPro_id()
                                                    && cartItem.getBatchDetails().getBd_id() == batchDetails.getBd_id()
                                                    && cartItem.getColor().getColor_id() == color.getColor_id()) {
                                                cart = cartItem;
                                                setCartItem = false;
                                                break;
                                            }
                                        }

                                        if (setCartItem) {

                                            cart = new Cart();
                                            cart.setQty(Integer.parseInt(qty));
                                            cart.setProduct(product);
                                            cart.setBatchDetails(batchDetails);
                                            cart.setColor(color);
                                            cart.setUser(null);

                                            cartArray.add(cart);

                                            session.setAttribute("cart", cartArray);
                                            response.addCookie(Util.createSessionCookie(session.getId()));

                                            responseObject.addProperty("status", Boolean.TRUE);
                                            responseObject.addProperty("msg", "Item added.");
                                        } else {
                                            if (cart.getQty() + Integer.parseInt(qty) <= batchDetails.getQty()) {
                                                cart.setQty(cart.getQty() + Integer.parseInt(qty));

                                                session.setAttribute("cart", cartArray);
                                                response.addCookie(Util.createSessionCookie(session.getId()));

                                                responseObject.addProperty("status", Boolean.TRUE);
                                                responseObject.addProperty("msg", "Item already exists. Quantity updated.");
                                            } else {
                                                responseObject.addProperty("msg", "Insufficient Quantity. [Only " + batchDetails.getQty() + " available]");
                                            }
                                        }
                                    }
                                } else {
                                    responseObject.addProperty("msg", "Something went wrong while processing. Please try again later.");
                                }

                            } else {
                                responseObject.addProperty("msg", "Item color not found.");
                            }

                        } else {
                            responseObject.addProperty("msg", "Insufficient Quantity. [Only " + batchDetails.getQty() + " available]");
                        }

                    } else {
                        responseObject.addProperty("msg", "Item batch not found.");
                    }

                } else {
                    responseObject.addProperty("msg", "Item not found.");
                }

                ses.close();

            }

        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Criteria c1 = ses.createCriteria(Cart.class);
            c1.add(Restrictions.eq("user", user));

            if (!c1.list().isEmpty()) {

                List<Cart> cartData = c1.list();

                for (Cart cart : cartData) {
                    cart.setUser(null);
                    cart.getProduct().setUser(null);

                    responseObject.addProperty("noQty_" + cart.getCart_id(), false);

                    if (cart.getProduct().getBatch().getActiveStatus().getAid() == AddToCart.ACTIVE_STATUS_ID) {
                        if (cart.getBatchDetails().getQty() < cart.getQty()) {
                            cart.setQty(cart.getBatchDetails().getQty());
                            responseObject.addProperty("qty_msg_" + cart.getCart_id(), "Available Quantity: " + cart.getBatchDetails().getQty());
                        }
                    } else {
                        responseObject.addProperty("noQty_" + cart.getCart_id(), true);
                    }

                    responseObject.addProperty("img_" + cart.getCart_id(), "http://127.0.0.1:8080/WEB_II_VIVA_Project/product-images/" + cart.getProduct().getPro_id() + "/image1.png");
                
                }

                Criteria c2 = ses.createCriteria(Address.class);
                c2.add(Restrictions.eq("user", user));

                if (!c2.list().isEmpty()) {

                    List<Address> addreList = c2.list();
                    ArrayList<DeliveryCost> delivList = new ArrayList<>();

                    for (Address address : addreList) {

                        address.setUser(null);
                        
                        ActiveStatus activeStatus = (ActiveStatus) ses.get(ActiveStatus.class, AddToCart.ACTIVE_STATUS_ID);

                        Criteria c3 = ses.createCriteria(DeliveryCost.class);
                        c3.add(Restrictions.eq("district", address.getCity().getDistrict()));
                        c3.add(Restrictions.eq("activeStatus", activeStatus));
                        
                        if (!c3.list().isEmpty()) {
                            responseObject.add("deliveryCost_" + address.getAid(), gson.toJsonTree(c3.list()));
                        }
                    }

                    responseObject.addProperty("addressStatus", Boolean.TRUE);
                    responseObject.add("address", gson.toJsonTree(c2.list()));

                } else {
                    // NO Address
                    responseObject.addProperty("addressStatus", Boolean.FALSE);
                }

                responseObject.add("cartData", gson.toJsonTree(cartData));
                responseObject.addProperty("isDBcart", true);
                responseObject.addProperty("status", Boolean.TRUE);

            } else {
                responseObject.addProperty("msg", "2");
            }

            ses.close();

        } else if (session != null) {

            if (session.getAttribute("cart") != null) {

                ArrayList<Cart> cartList = (ArrayList<Cart>) session.getAttribute("cart");

                if (!cartList.isEmpty()) {

                    for (Cart cart : cartList) {
                        cart.setUser(null);
                        cart.getProduct().setUser(null);

                        responseObject.addProperty("noQty_" + cart.getCart_id(), false);

                        if (cart.getProduct().getBatch().getActiveStatus().getAid() == AddToCart.ACTIVE_STATUS_ID) {
                            if (cart.getBatchDetails().getQty() < cart.getQty()) {
                                cart.setQty(cart.getBatchDetails().getQty());
                                responseObject.addProperty("qty_msg_" + cart.getCart_id(), "Available Quantity: " + cart.getBatchDetails().getQty());
                            }
                        } else {
                            responseObject.addProperty("noQty_" + cart.getCart_id(), true);
                        }

                        responseObject.addProperty("img_" + cart.getCart_id(), "http://127.0.0.1:8080/WEB_II_VIVA_Project/product-images/" + cart.getProduct().getPro_id() + "/image1.png");
                    }

                    responseObject.add("cartData", gson.toJsonTree(cartList));
                    responseObject.addProperty("isDBcart", false);
                    responseObject.addProperty("status", Boolean.TRUE);

                } else {
                    responseObject.addProperty("msg", "2");
                }

            } else {
                //empty cart
                responseObject.addProperty("msg", "2");
            }

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

        JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);

        HttpSession session = request.getSession(false);

        String qty = requestObject.get("qty").getAsString();
        String cartId = requestObject.get("cartId").getAsString();

        if (qty.isEmpty()) {
            responseObject.addProperty("msg", "Quantity is required.");
        } else if (!Util.checkInteger(qty)) {
            responseObject.addProperty("msg", "Invalid quantity.");
        } else if (Integer.parseInt(qty) < 1) {
            responseObject.addProperty("msg", "Invalid quantity.");
        } else if (cartId.isEmpty() || !Util.checkInteger(cartId)) {
            responseObject.addProperty("msg", "Something went wrong. Please try again later.");
        } else {

            if (session != null && session.getAttribute("user") != null) {

                User user = (User) session.getAttribute("user");

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Criteria c1 = ses.createCriteria(Cart.class);
                c1.add(Restrictions.eq("cart_id", Integer.parseInt(cartId)));
                c1.add(Restrictions.eq("user", user));

                if (!c1.list().isEmpty()) {

                    Cart cart = (Cart) c1.uniqueResult();
                    if (cart.getBatchDetails().getQty() >= Integer.parseInt(qty)) {
                        cart.setQty(Integer.parseInt(qty));

                        ses.update(cart);
                        ses.beginTransaction().commit();
                        responseObject.addProperty("msg", "Quantity updated.");
                        responseObject.addProperty("status", Boolean.TRUE);
                    } else {
                        responseObject.addProperty("msg", "Insufficient item quantity.");
                    }

                } else {
                    responseObject.addProperty("msg", "Cart item not found.");
                }
                ses.close();
            } else if (session != null) {

                if (session.getAttribute("cart") != null) {

                    ArrayList<Cart> cartList = (ArrayList<Cart>) session.getAttribute("cart");

                    for (Cart cart : cartList) {
                        if (cart.getCart_id() == Integer.parseInt(cartId)) {
                            if (cart.getBatchDetails().getQty() >= Integer.parseInt(qty)) {
                                cart.setQty(Integer.parseInt(qty));

                                responseObject.addProperty("msg", "Quantity updated.");
                                responseObject.addProperty("status", Boolean.TRUE);
                            } else {
                                responseObject.addProperty("msg", "Insufficient item quantity.");
                            }
                            break;
                        }
                    }

                } else {
                    responseObject.addProperty("msg", "Cart item not found.");
                }
            }

        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

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

        String cartId = request.getParameter("cartId");

        if (session != null && session.getAttribute("user") != null) {

            User user = (User) session.getAttribute("user");

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Criteria c1 = ses.createCriteria(Cart.class);
            c1.add(Restrictions.eq("cart_id", Integer.parseInt(cartId)));
            c1.add(Restrictions.eq("user", user));

            if (!c1.list().isEmpty()) {

                Cart cart = (Cart) c1.uniqueResult();
                ses.delete(cart);

                ses.beginTransaction().commit();

                responseObject.addProperty("msg", "Cart item deleted.");
                responseObject.addProperty("status", Boolean.TRUE);
            } else {
                responseObject.addProperty("msg", "Cart item not found.");
            }

            ses.close();
        } else if (session != null) {

            if (session.getAttribute("cart") != null) {

                ArrayList<Cart> cartList = (ArrayList<Cart>) session.getAttribute("cart");

                for (Cart cart : cartList) {
                    if (cart.getCart_id() == Integer.parseInt(cartId)) {

                        cartList.remove(cart);
                        responseObject.addProperty("msg", "Cart item deleted.");
                        responseObject.addProperty("status", Boolean.TRUE);
                        break;
                    }
                }

            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

}
