package app.clinicloc.com.bean;

import java.io.Serializable;

/**
 * Created by syednasharudin on 9/17/2014.
 */
public class Tenant implements Serializable {

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    private long tenantId;
    private String name;
    private String companyName;

}
