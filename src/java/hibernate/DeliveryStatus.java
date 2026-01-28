package hibernate;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "delivery_status")
public class DeliveryStatus implements Serializable {

    public DeliveryStatus() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "des_id")
    private int des_id;

    @Column(name = "des_status", length = 45, nullable = false)
    private String des_status;

    public int getDes_id() {
        return des_id;
    }

    public void setDes_id(int des_id) {
        this.des_id = des_id;
    }

    public String getDes_status() {
        return des_status;
    }

    public void setDes_status(String des_status) {
        this.des_status = des_status;
    }
}
