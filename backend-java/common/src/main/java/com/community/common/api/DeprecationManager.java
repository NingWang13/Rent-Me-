package com.community.common.api;

public class DeprecationManager {

    public boolean isDeprecated(ApiVersion version) {
        return version.isDeprecated();
    }

    public String getDeprecationMessage(ApiVersion version) {
        return "API version " + version.getVersion() + " is deprecated. Please upgrade to a newer version.";
    }

    public ApiVersion getLatestVersion() {
        ApiVersion[] versions = ApiVersion.values();
        return versions[versions.length - 1];
    }
}
