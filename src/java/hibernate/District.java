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
@Table(name = "district")
public class District implements Serializable{

    public District() {}
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dis_id")
    private int dis_id;
    
    @Column(name = "dname", length = 45, nullable = false)
    private String dname;
    
    @ManyToOne
    @JoinColumn(name = "province_pid")
    private Province province;

    public int getDis_id() {
        return dis_id;
    }

    public void setDis_id(int dis_id) {
        this.dis_id = dis_id;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    } 
}
