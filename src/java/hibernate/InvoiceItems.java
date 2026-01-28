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
@Table(name = "invoice_items")
public class InvoiceItems implements Serializable {

    public InvoiceItems() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private int item_id;

    @Column(name = "qty", nullable = false)
    private int qty;
    
    @ManyToOne
    @JoinColumn(name = "product_pro_id")
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "color_color_id")
    private Color color;
    
    @ManyToOne
    @JoinColumn(name = "batch_details_bd_id")
    private BatchDetails batchDetails;
    
    @ManyToOne
    @JoinColumn(name = "invoice_invoice_no")
    private Invoice invoice;

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public BatchDetails getBatchDetails() {
        return batchDetails;
    }

    public void setBatchDetails(BatchDetails batchDetails) {
        this.batchDetails = batchDetails;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
