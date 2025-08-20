package com.ontic.Garuda.model;

import java.net.URI;

public class ResolvedURI {

    private URI targetUri;

    private String planetId;

    public ResolvedURI() {
    }

    public ResolvedURI(URI targetUri, String planetId) {
        this.targetUri = targetUri;
        this.planetId = planetId;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(URI targetUri) {
        this.targetUri = targetUri;
    }

    public String getPlanetId() {
        return planetId;
    }

    public void setPlanetId(String planetId) {
        this.planetId = planetId;
    }
}
