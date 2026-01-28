package hibernate;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "brand_has_varient")
public class BrandHasVarient implements Serializable {

    public BrandHasVarient() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bv_id")
    private int bv_id;

    @ManyToOne
    @JoinColumn(name = "brand_brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "varient_vid")
    private Varient varient;

    public int getBv_id() {
        return bv_id;
    }

    public void setBv_id(int bv_id) {
        this.bv_id = bv_id;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Varient getVarient() {
        return varient;
    }

    public void setVarient(Varient varient) {
        this.varient = varient;
    }
}
