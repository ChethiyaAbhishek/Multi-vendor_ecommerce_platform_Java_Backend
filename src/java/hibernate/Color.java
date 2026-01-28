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
@Table(name = "color")
public class Color implements Serializable{

    public Color() {
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "color_id")
    private int color_id;
    
    @Column(name = "code", length = 10, nullable = false)
    private String code;
    
    @ManyToOne
    @JoinColumn(name = "batch_details_bd_id")
    private BatchDetails batchDetails;

    public int getColor_id() {
        return color_id;
    }

    public void setColor_id(int color_id) {
        this.color_id = color_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BatchDetails getBatchDetails() {
        return batchDetails;
    }

    public void setBatchDetails(BatchDetails batchDetails) {
        this.batchDetails = batchDetails;
    }
}
