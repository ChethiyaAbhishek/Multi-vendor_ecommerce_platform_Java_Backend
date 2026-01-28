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
@Table(name = "user")
public class User implements Serializable{

    public User() {}
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid")
    private int uid;
    
    @Column(name = "fname", length = 45, nullable = false)
    private String fname;
    
    @Column(name = "lname", length = 45, nullable = false)
    private String lname;
    
    @Column(name = "mobile", length = 10, nullable = false)
    private String mobile;
    
    @Column(name = "email", length = 45, nullable = false)
    private String email;
    
    @Column(name = "password", length = 45, nullable = false)
    private String password;
    
    @Column(name = "verified", length = 5, nullable = false)
    private String verified;
    
    @Column(name = "vcode", length = 45, nullable = true)
    private String vcode;
    
    @Column(name = "seller_joined_date", nullable = true)
    private Date seller_joined_date;
    
    @ManyToOne
    @JoinColumn(name = "active_status_aid")
    private ActiveStatus activeStatus;
    
    @ManyToOne
    @JoinColumn(name = "user_status_usid")
    private UserStatus userStatus;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getVcode() {
        return vcode;
    }

    public void setVcode(String vcode) {
        this.vcode = vcode;
    }

    public Date getSeller_joined_date() {
        return seller_joined_date;
    }

    public void setSeller_joined_date(Date seller_joined_date) {
        this.seller_joined_date = seller_joined_date;
    }

    public ActiveStatus getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(ActiveStatus activeStatus) {
        this.activeStatus = activeStatus;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }
}
