package com.ontic.Garuda.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TenantPlanetMapping {
    @Id
    private String id;

    @Indexed(unique = true)
    private String tenantId;

    private String planetId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getPlanetId() {
        return planetId;
    }

    public void setPlanetId(String planetId) {
        this.planetId = planetId;
    }

    public interface FM {
        String TENANT_ID = "tenantId";
        String PLANET_ID = "planetId";
    }
}
