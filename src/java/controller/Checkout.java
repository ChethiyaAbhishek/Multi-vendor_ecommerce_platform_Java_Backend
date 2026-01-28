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
import hibernate.Coupon;
import hibernate.CouponDiscounts;
import hibernate.DeliveryCost;
import hibernate.DeliveryStatus;
import hibernate.HibernateUtil;
import hibernate.Invoice;
import hibernate.InvoiceItems;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.PayHere;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "Checkout", urlPatterns = {"/Checkout"})
public class Checkout extends HttpServlet {

    private static final int ACTIVE_STATUS_ID = 1;
    private static final int INACTIVE_STATUS_ID = 2;
    private static final int PENDING_STATUS_ID = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Content-Security-Policy", "script-src 'self' https://www.payhere.lk 'unsafe-inline';");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        Gson gson = new Gson();

        HttpSession session = request.getSession();

        if (session != null && session.getAttribute("user") != null) {

            User user = (User) session.getAttribute("user");

            JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);
            JsonElement sAd = requestObject.get("selectedAddress");
            String couponDiscount = requestObject.get("couponDiscount").getAsString();

            if (sAd == null || sAd.isJsonNull()) {
                responseObject.addProperty("msg", "2");
            } else {
                String selectedAddress = requestObject.get("selectedAddress").getAsString();

                if (selectedAddress.isEmpty()) {
                    responseObject.addProperty("msg", "Please select a shipping address.");
                } else if (!Util.checkInteger(selectedAddress) || Integer.parseInt(selectedAddress) < 1) {
                    responseObject.addProperty("msg", "Invalid shipping address.");
                } else {
                    Session ses = HibernateUtil.getSessionFactory().openSession();

                    Criteria c1 = ses.createCriteria(Address.class);
                    c1.add(Restrictions.eq("user", user));
                    c1.add(Restrictions.eq("aid", Integer.parseInt(selectedAddress)));

                    if (!c1.list().isEmpty()) {

                        Address address = (Address) c1.uniqueResult();

                        boolean continueProcess = true;
                        Double discountPercentage = 0.00;
                        Coupon coupon = null;

                        ActiveStatus activeStatus = (ActiveStatus) ses.get(ActiveStatus.class, Checkout.ACTIVE_STATUS_ID);

                        if (!couponDiscount.isEmpty() && !couponDiscount.equals("0")) {

                            JsonElement ccode = requestObject.get("couponCode");

                            if (ccode == null || ccode.isJsonNull()) {
                                responseObject.addProperty("msg", "Coupon code not found.");
                                continueProcess = false;
                            } else {

                                String couponCode = requestObject.get("couponCode").getAsString();

                                if (Util.check6DigitCode(couponCode)) {

                                    Criteria c2 = ses.createCriteria(Coupon.class);
                                    c2.add(Restrictions.eq("user", user));
                                    c2.add(Restrictions.eq("code", couponCode));
                                    c2.add(Restrictions.eq("activeStatus", activeStatus));

                                    if (!c2.list().isEmpty()) {

                                        coupon = (Coupon) c2.uniqueResult();

                                        if (coupon.getCouponDiscounts().getDiscount() == Double.valueOf(couponDiscount)) {

                                            discountPercentage = coupon.getCouponDiscounts().getDiscount();

                                        } else {
                                            responseObject.addProperty("msg", "Something went wrong. Try again later.");
                                            continueProcess = false;
                                        }

                                    } else {
                                        responseObject.addProperty("msg", "Coupon code not found.");
                                        continueProcess = false;
                                    }

                                } else {
                                    responseObject.addProperty("msg", "Invalid coupon code.");
                                    continueProcess = false;
                                }
                            }
                        }

                        if (continueProcess) {

                            Transaction transaction = ses.beginTransaction();

                            try {

                                Criteria c3 = ses.createCriteria(DeliveryCost.class);
                                c3.add(Restrictions.eq("district", address.getCity().getDistrict()));

                                DeliveryCost deliveryCost = (DeliveryCost) c3.uniqueResult();

                                DeliveryStatus deliveryStatus = (DeliveryStatus) ses.get(DeliveryStatus.class, Checkout.PENDING_STATUS_ID);

                                ActiveStatus inactiveStatus = (ActiveStatus) ses.get(ActiveStatus.class, Checkout.INACTIVE_STATUS_ID);

                                String invoiceId = Util.generateInvoiceId();

                                Invoice invoice = new Invoice();
                                invoice.setInvoice_no(invoiceId);
                                invoice.setDate_time(new Date());
                                if (coupon != null) {
                                    invoice.setCoupon(coupon);
                                    coupon.setActiveStatus(inactiveStatus);
                                    ses.update(coupon);
                                }
                                invoice.setUser(user);
                                invoice.setDeliveryCost(deliveryCost);
                                invoice.setDeliveryStatus(deliveryStatus);
                                ses.save(invoice);

                                Criteria c4 = ses.createCriteria(Cart.class);
                                c4.add(Restrictions.eq("user", user));

                                List<Cart> cartItems = c4.list();

                                double amount = 0.0;
                                String itemNames = "";

                                for (Cart cartItem : cartItems) {

                                    InvoiceItems invoiceItems = new InvoiceItems();
                                    invoiceItems.setQty(cartItem.getQty());
                                    invoiceItems.setProduct(cartItem.getProduct());
                                    invoiceItems.setColor(cartItem.getColor());
                                    invoiceItems.setBatchDetails(cartItem.getBatchDetails());
                                    invoiceItems.setInvoice(invoice);
                                    ses.save(invoiceItems);

                                    amount += (cartItem.getQty() * cartItem.getBatchDetails().getPrice());
                                    itemNames += cartItem.getProduct().getTitle() + " x " + cartItem.getQty() + ", ";

                                    BatchDetails batchDetails = cartItem.getBatchDetails();
                                    batchDetails.setQty(cartItem.getBatchDetails().getQty() - cartItem.getQty());
                                    ses.update(batchDetails);

                                    ses.delete(cartItem);
                                } 
                                
                                amount += deliveryCost.getPrice();
                                
                                String merchantID = "1224658";
                                String merchantSecret = "NDE2MDIwMTY2MzA4MjkxMTMxMTE4NjAwOTk0NjM1NDY5NTk2MjY=";
                                String orderID = "#" + invoiceId;
                                String currency = "LKR";
                                String formattedAmount = new DecimalFormat("0.00").format(amount);
                                String merchantSecretMD5 = PayHere.generateMD5(merchantSecret);

                                String hash = PayHere.generateMD5(merchantID + orderID + formattedAmount + currency + merchantSecretMD5);

                                JsonObject payHereObject = new JsonObject();
                                payHereObject.addProperty("sandbox", true);
                                payHereObject.addProperty("merchant_id", merchantID);

                                payHereObject.addProperty("return_url", "https://75bdd2994dec.ngrok-free.app/WEB_II_VIVA_Project/VerifyPayments");
                                payHereObject.addProperty("cancel_url", "https://75bdd2994dec.ngrok-free.app/WEB_II_VIVA_Project/VerifyPayments"); 
                                payHereObject.addProperty("notify_url", "https://75bdd2994dec.ngrok-free.app/WEB_II_VIVA_Project/VerifyPayments");
//                                payHereObject.addProperty("notify_url", "http://sample.com/notify");

                                payHereObject.addProperty("order_id", orderID);
                                payHereObject.addProperty("items", itemNames);
                                payHereObject.addProperty("amount", formattedAmount);
                                payHereObject.addProperty("currency", currency);
                                payHereObject.addProperty("hash", hash);

                                payHereObject.addProperty("first_name", user.getFname());
                                payHereObject.addProperty("last_name", user.getLname());
                                payHereObject.addProperty("email", user.getEmail());

                                payHereObject.addProperty("phone", user.getMobile());
                                payHereObject.addProperty("address", address.getLine1() + ", " + address.getLine2());
                                payHereObject.addProperty("city", address.getCity().getCname());
                                payHereObject.addProperty("country", "Sri Lanka");

                                responseObject.addProperty("status", true);
                                responseObject.add("payhereObject", new Gson().toJsonTree(payHereObject));
                                responseObject.addProperty("msg", "Payment Completed");
                                

                                transaction.commit();

                            } catch (Exception e) {
                                transaction.rollback();
                            }
                        }

                    } else {
                        responseObject.addProperty("msg", "Selected shipping address not found.");
                    }

                    ses.close();
                }
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

}
