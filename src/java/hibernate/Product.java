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
import org.hibernate.annotations.Generated;

@Entity
@Table(name = "product")
public class Product implements Serializable {

    public Product() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pro_id")
    private int pro_id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "sdescription", length = 500, nullable = true)
    private String sdescription;

    @Column(name = "ldescription", nullable = false)
    private String ldescription;

    @ManyToOne
    @JoinColumn(name = "batch_bid")
    private Batch batch;

    @ManyToOne
    @JoinColumn(name = "brand_has_varient_bv_id")
    private BrandHasVarient brandHasVarient;

    @ManyToOne
    @JoinColumn(name = "sub_category_sub_id")
    private SubCategory subCategory;

    @ManyToOne
    @JoinColumn(name = "active_status_aid")
    private ActiveStatus activeStatus;

    @ManyToOne
    @JoinColumn(name = "seller_uid")
    private User user;

    public int getPro_id() {
        return pro_id;
    }

    public void setPro_id(int pro_id) {
        this.pro_id = pro_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSdescription() {
        return sdescription;
    }

    public void setSdescription(String sdescription) {
        this.sdescription = sdescription;
    }

    public String getLdescription() {
        return ldescription;
    }

    public void setLdescription(String ldescription) {
        this.ldescription = ldescription;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public BrandHasVarient getBrandHasVarient() {
        return brandHasVarient;
    }

    public void setBrandHasVarient(BrandHasVarient brandHasVarient) {
        this.brandHasVarient = brandHasVarient;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public ActiveStatus getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(ActiveStatus activeStatus) {
        this.activeStatus = activeStatus;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
