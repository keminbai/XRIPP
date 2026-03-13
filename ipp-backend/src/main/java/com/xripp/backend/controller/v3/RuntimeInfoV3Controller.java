package com.xripp.backend.controller.v3;

import com.xripp.backend.common.V3Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RuntimeInfoV3Controller {

    private static final Instant STARTED_AT = Instant.now();

    private final Environment environment;

    @Value("${spring.application.name:xripp-backend}")
    private String applicationName;

    @Value("${xripp.runtime.version:unknown}")
    private String runtimeVersion;

    @Value("${xripp.schema-preflight.enabled:true}")
    private boolean schemaPreflightEnabled;

    public RuntimeInfoV3Controller(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/v3/runtime-info")
    public V3Response<Map<String, Object>> runtimeInfo() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("application", applicationName);
        data.put("runtimeVersion", runtimeVersion);
        data.put("schemaPreflightEnabled", schemaPreflightEnabled);
        data.put("startedAt", STARTED_AT.toString());
        data.put("activeProfiles", environment.getActiveProfiles());
        return V3Response.success(data);
    }
}
