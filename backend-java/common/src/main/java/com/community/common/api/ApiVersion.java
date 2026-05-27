package com.community.common.api;

public enum ApiVersion {
    V1("v1", false),
    V2("v2", false),
    V3("v3", false);

    private final String version;
    private final boolean deprecated;

    ApiVersion(String version, boolean deprecated) {
        this.version = version;
        this.deprecated = deprecated;
    }

    public String getVersion() {
        return version;
    }

    public boolean isDeprecated() {
        return deprecated;
    }
}
