package com.xripp.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "xripp.data-scope")
public class DataScopeProperties {

    private Boolean enabled = true;

    // 旧表白名单（保留）
    private List<String> whiteList = new ArrayList<>();

    // 新增：Mapper白名单（推荐优先使用）
    private List<String> mapperWhiteList = new ArrayList<>();

    public boolean isInWhiteList(String tableName) {
        if (tableName == null || whiteList == null || whiteList.isEmpty()) return false;
        String lower = tableName.toLowerCase();
        return whiteList.stream().anyMatch(t -> t != null && t.toLowerCase().equals(lower));
    }

    public boolean isMapperInWhiteList(String mappedStatementId) {
        if (mappedStatementId == null || mapperWhiteList == null || mapperWhiteList.isEmpty()) {
            return false;
        }
        return mapperWhiteList.stream()
                .filter(s -> s != null && !s.isBlank())
                .anyMatch(mappedStatementId::contains);
    }
}
