package hibernate;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "coupon")
public class Coupon implements Serializable {

    public Coupon() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cid")
    private int cid;

    @Column(name = "code", length = 45, nullable = false)
    private String code;

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "active_duration", length = 10, nullable = false)
    private int active_duration;

    @ManyToOne
    @JoinColumn(name = "user_uid")
    private User user;

    @ManyToOne
    @JoinColumn(name = "active_status_aid")
    private ActiveStatus activeStatus;

    @ManyToOne
    @JoinColumn(name = "coupon_discounts_id")
    private CouponDiscounts couponDiscounts;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getActive_duration() {
        return active_duration;
    }

    public void setActive_duration(int active_duration) {
        this.active_duration = active_duration;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ActiveStatus getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(ActiveStatus activeStatus) {
        this.activeStatus = activeStatus;
    }

    public CouponDiscounts getCouponDiscounts() {
        return couponDiscounts;
    }

    public void setCouponDiscounts(CouponDiscounts couponDiscounts) {
        this.couponDiscounts = couponDiscounts;
    }
}
