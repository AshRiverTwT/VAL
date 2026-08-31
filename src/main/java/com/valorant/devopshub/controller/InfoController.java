package com.valorant.devopshub.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.valorant.devopshub.service.RuntimeInfoService;

/**
 * Application information endpoint. Backs the "DevOps Infrastructure" panel
 * on the About page (fetched client-side by static/js/about.js) and is also
 * useful directly via curl during troubleshooting labs.
 */
@RestController
public class InfoController {

    private final RuntimeInfoService runtimeInfoService;

    public InfoController(RuntimeInfoService runtimeInfoService) {
        this.runtimeInfoService = runtimeInfoService;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("applicationVersion", runtimeInfoService.getVersion());
        body.put("javaVersion", runtimeInfoService.getJavaVersion());
        body.put("javaVendor", runtimeInfoService.getJavaVendor());
        body.put("environment", runtimeInfoService.getEnvironment());
        body.put("buildNumber", runtimeInfoService.getBuildNumber());
        body.put("gitCommit", runtimeInfoService.getGitCommit());
        body.put("buildTime", runtimeInfoService.getBuildTime());
        body.put("hostname", runtimeInfoService.getHostname());
        body.put("containerStatus", runtimeInfoService.getContainerStatus());
        body.put("serverTime", runtimeInfoService.getServerTime().toString());
        return body;
    }
}
