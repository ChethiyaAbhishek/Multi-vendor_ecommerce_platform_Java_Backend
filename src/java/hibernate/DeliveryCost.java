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
@Table(name = "delivery_cost")
public class DeliveryCost implements Serializable {

    public DeliveryCost() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dec_id")
    private int dec_id;

    @Column(name = "activated_at", nullable = false)
    private Date activated_at;

    @ManyToOne
    @JoinColumn(name = "district_dis_id")
    private District district;

    @Column(name = "price", nullable = false)
    private double price;

    @ManyToOne
    @JoinColumn(name = "active_status_aid")
    private ActiveStatus activeStatus;

    public int getDec_id() {
        return dec_id;
    }

    public void setDec_id(int dec_id) {
        this.dec_id = dec_id;
    }

    public Date getActivated_at() {
        return activated_at;
    }

    public void setActivated_at(Date activated_at) {
        this.activated_at = activated_at;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ActiveStatus getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(ActiveStatus activeStatus) {
        this.activeStatus = activeStatus;
    }
}
