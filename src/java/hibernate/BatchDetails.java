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
@Table(name = "batch_details")
public class BatchDetails implements Serializable{

    public BatchDetails() {}
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bd_id")
    private int bd_id;
    
    @Column(name = "price", nullable = false)
    private Double price;
    
    @Column(name = "qty", nullable = false)
    private int qty;
    
    @Column(name = "discount", nullable = false)
    private Double discount;
    
    @ManyToOne()
    @JoinColumn(name = "batch_bid")
    private Batch batch;
    
    @ManyToOne
    @JoinColumn(name = "size_size_id")
    private Size size;

    public int getBd_id() {
        return bd_id;
    }

    public void setBd_id(int bd_id) {
        this.bd_id = bd_id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }
}
