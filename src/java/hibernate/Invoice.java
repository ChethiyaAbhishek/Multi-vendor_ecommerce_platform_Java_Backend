package hibernate;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "invoice")
public class Invoice implements Serializable {

    public Invoice() {
    }

    @Id
    @Column(name = "invoice_no", length = 45, nullable = false)
    private String invoice_no;

    @Column(name = "date_time", nullable = false)
    private Date date_time;

    @ManyToOne
    @JoinColumn(name = "coupon_cid", nullable = true)
    private Coupon coupon;

    @ManyToOne
    @JoinColumn(name = "user_uid")
    private User user;

    @ManyToOne
    @JoinColumn(name = "delivery_cost_dec_id")
    private DeliveryCost deliveryCost;

    @ManyToOne
    @JoinColumn(name = "delivery_status_des_id")
    private DeliveryStatus deliveryStatus;

    public String getInvoice_no() {
        return invoice_no;
    }

    public void setInvoice_no(String invoice_no) {
        this.invoice_no = invoice_no;
    }

    public Date getDate_time() {
        return date_time;
    }

    public void setDate_time(Date date_time) {
        this.date_time = date_time;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DeliveryCost getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(DeliveryCost deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}
