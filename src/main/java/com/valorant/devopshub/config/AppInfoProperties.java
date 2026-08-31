package com.valorant.devopshub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the custom "app.*" properties from application.yml (which in turn
 * come from environment variables such as APP_ENV, APP_VERSION,
 * BUILD_NUMBER and GIT_COMMIT). Keeping this as one small properties class
 * means every other class asks Spring for a single AppInfoProperties bean
 * instead of re-reading System.getenv() everywhere.
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppInfoProperties {

    /** e.g. "development", "docker", "production" - from APP_ENV. */
    private String env = "development";

    /** Explicit version override from APP_VERSION. Empty means "use the Maven build version". */
    private String version = "";

    /** CI build number injected by Jenkins via BUILD_NUMBER. "local" when run outside Jenkins. */
    private String buildNumber = "local";

    /** Git commit SHA injected by Jenkins/CI via GIT_COMMIT. "unknown" when not provided. */
    private String gitCommit = "unknown";

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getGitCommit() {
        return gitCommit;
    }

    public void setGitCommit(String gitCommit) {
        this.gitCommit = gitCommit;
    }
}
