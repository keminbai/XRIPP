package com.xripp.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xripp.backend.entity.SysLoginMode;
import com.xripp.backend.entity.SysPermissionGrant;
import com.xripp.backend.entity.SysPermissionProfile;
import com.xripp.backend.entity.SysUser;
import com.xripp.backend.security.SecurityContextHolder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPermissionService {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_]{2,50}$");
    private static final Pattern MODE_CODE_PATTERN = Pattern.compile("^[a-z0-9_]{2,50}$");
    private static final Pattern MODULE_CODE_PATTERN = Pattern.compile("^[a-z0-9_]{2,80}$");
    private static final Set<String> VALID_BASE_ROLES = Set.of("admin", "partner");
    private final ISysPermissionProfileService sysPermissionProfileService;
    private final ISysPermissionGrantService sysPermissionGrantService;
    private final ISysLoginModeService sysLoginModeService;
    private final ISysUserService sysUserService;

    public boolean hasAdminPermission(String moduleCode, String action) {
        if (!SecurityContextHolder.isAdmin()) {
            return false;
        }
        if (moduleCode == null || moduleCode.isBlank() || action == null || action.isBlank()) {
            return false;
        }

        PermissionSnapshot snapshot = resolveCurrentSnapshotModel();
        if (!snapshot.isActive()) {
            return false;
        }
        if (snapshot.isSuperAdmin()) {
            return true;
        }

        ModuleGrant grant = snapshot.getModuleGrantMap().get(moduleCode);
        if (grant == null) {
            return false;
        }

        String normalizedAction = action.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedAction) {
            case "view" -> grant.isCanView();
            case "create" -> grant.isCanCreate();
            case "edit" -> grant.isCanEdit();
            case "delete" -> grant.isCanDelete();
            case "review" -> grant.isCanReview();
            case "export" -> grant.isCanExport();
            default -> false;
        };
    }

    public Map<String, Object> getCurrentSnapshot() {
        return toSnapshotMap(resolveCurrentSnapshotModel());
    }

    public List<Map<String, Object>> listLoginModes() {
        Map<Long, SysPermissionProfile> profileMap = loadProfileMap();
        return sysLoginModeService.list(
                        new QueryWrapper<SysLoginMode>().orderByAsc("sort_order").orderByAsc("id")
                ).stream()
                .map(mode -> toLoginModeItem(mode, profileMap.get(mode.getDefaultProfileId())))
                .toList();
    }

    @Transactional
    public List<Map<String, Object>> saveLoginModes(List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items required");
        }

        Date now = new Date();
        for (Map<String, Object> item : items) {
            String modeCode = normalizeModeCode(str(item.get("mode_code"), item.get("modeCode")));
            if (!MODE_CODE_PATTERN.matcher(modeCode).matches()) {
                throw new IllegalArgumentException("invalid mode_code");
            }

            String baseRole = normalizeBaseRole(str(item.get("base_role"), item.get("baseRole")));
            if (!VALID_BASE_ROLES.contains(baseRole)) {
                throw new IllegalArgumentException("invalid base_role");
            }

            SysLoginMode row = sysLoginModeService.getOne(
                    new QueryWrapper<SysLoginMode>().eq("mode_code", modeCode),
                    false
            );
            if (row == null) {
                row = new SysLoginMode();
                row.setModeCode(modeCode);
                row.setCreatedAt(now);
            }

            Long defaultProfileId = longVal(item.get("default_profile_id"), item.get("defaultProfileId"));
            if (defaultProfileId != null && sysPermissionProfileService.getById(defaultProfileId) == null) {
                throw new IllegalArgumentException("default profile not found");
            }

            row.setName(nonBlank(str(item.get("name")), str(item.get("mode")), modeCode));
            row.setBaseRole(baseRole);
            row.setEnabled(boolVal(item.get("enabled"), true));
            row.setDefaultProfileId(defaultProfileId);
            row.setSortOrder(intVal(item.get("sort_order"), item.get("sortOrder"), 0));
            row.setDescription(str(item.get("description"), item.get("desc")));
            row.setUpdatedAt(now);

            if (row.getId() == null) {
                sysLoginModeService.save(row);
            } else {
                sysLoginModeService.updateById(row);
            }
        }

        return listLoginModes();
    }

    public List<Map<String, Object>> listProfiles() {
        List<SysPermissionProfile> profiles = sysPermissionProfileService.list(
                new QueryWrapper<SysPermissionProfile>()
                        .orderByDesc("is_super_admin")
                        .orderByAsc("base_role")
                        .orderByAsc("id")
        );
        Map<Long, List<SysPermissionGrant>> grantMap = loadGrantMap(
                profiles.stream().map(SysPermissionProfile::getId).filter(Objects::nonNull).toList()
        );
        return profiles.stream().map(profile -> toProfileItem(profile, grantMap.get(profile.getId()))).toList();
    }

    @Transactional
    public Map<String, Object> createProfile(Map<String, Object> body) {
        return saveProfile(null, body);
    }

    @Transactional
    public Map<String, Object> updateProfile(Long id, Map<String, Object> body) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("invalid profile id");
        }
        return saveProfile(id, body);
    }

    @Transactional
    public Map<String, Object> assignProfileToUser(Long userId, Long profileId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("invalid user id");
        }

        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }

        if (profileId != null) {
            SysPermissionProfile profile = sysPermissionProfileService.getById(profileId);
            if (profile == null) {
                throw new IllegalArgumentException("profile not found");
            }
            String userRole = safe(user.getRole()).trim().toLowerCase(Locale.ROOT);
            String baseRole = safe(profile.getBaseRole()).trim().toLowerCase(Locale.ROOT);
            if (!userRole.isBlank() && !baseRole.equals(userRole)) {
                throw new IllegalArgumentException("profile base_role mismatch");
            }
        }

        user.setPermissionProfileId(profileId);
        sysUserService.updateById(user);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("username", safe(user.getUsername()));
        result.put("role", safe(user.getRole()));
        result.put("permissionProfileId", profileId);
        return result;
    }

    private Map<String, Object> saveProfile(Long id, Map<String, Object> body) {
        Date now = new Date();
        SysPermissionProfile profile = id == null ? null : sysPermissionProfileService.getById(id);
        if (id != null && profile == null) {
            throw new IllegalArgumentException("profile not found");
        }

        if (profile == null) {
            profile = new SysPermissionProfile();
            profile.setCreatedAt(now);
            profile.setIsSystem(false);
            profile.setIsSuperAdmin(false);
        }

        String rawCode = nonBlank(str(body.get("code")), safe(profile.getCode()));
        String code = normalizeProfileCode(rawCode);
        if (!isUpperCode(code)) {
            throw new IllegalArgumentException("invalid code");
        }
        if (Boolean.TRUE.equals(profile.getIsSystem()) && profile.getCode() != null && !profile.getCode().equals(code)) {
            throw new IllegalArgumentException("system profile code is immutable");
        }

        String baseRole = normalizeBaseRole(nonBlank(str(body.get("base_role")), str(body.get("baseRole")), safe(profile.getBaseRole())));
        if (!VALID_BASE_ROLES.contains(baseRole)) {
            throw new IllegalArgumentException("invalid base_role");
        }

        QueryWrapper<SysPermissionProfile> duplicateQw = new QueryWrapper<SysPermissionProfile>().eq("code", code);
        if (id != null) {
            duplicateQw.ne("id", id);
        }
        if (sysPermissionProfileService.count(duplicateQw) > 0) {
            throw new IllegalArgumentException("profile code already exists");
        }

        profile.setCode(code);
        profile.setName(nonBlank(str(body.get("name")), code));
        profile.setBaseRole(baseRole);
        profile.setStatus(boolVal(body.get("status"), body.containsKey("enabled") ? boolVal(body.get("enabled"), true) : boolObj(profile.getStatus(), true)));
        profile.setDescription(str(body.get("description"), body.get("desc")));
        if (!Boolean.TRUE.equals(profile.getIsSystem()) && body.containsKey("is_super_admin")) {
            profile.setIsSuperAdmin(boolVal(body.get("is_super_admin"), false));
        }
        profile.setUpdatedAt(now);

        if (profile.getId() == null) {
            sysPermissionProfileService.save(profile);
        } else {
            sysPermissionProfileService.updateById(profile);
        }

        if (body.containsKey("grants")) {
            replaceProfileGrants(profile.getId(), body.get("grants"), now);
        }

        List<SysPermissionGrant> grants = sysPermissionGrantService.list(
                new QueryWrapper<SysPermissionGrant>().eq("profile_id", profile.getId()).orderByAsc("id")
        );
        return toProfileItem(profile, grants);
    }

    private void replaceProfileGrants(Long profileId, Object rawGrants, Date now) {
        sysPermissionGrantService.remove(new QueryWrapper<SysPermissionGrant>().eq("profile_id", profileId));
        if (!(rawGrants instanceof List<?> items) || items.isEmpty()) {
            return;
        }

        for (Object raw : items) {
            if (!(raw instanceof Map<?, ?> rawMap)) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            rawMap.forEach((key, value) -> item.put(String.valueOf(key), value));

            String moduleCode = normalizeModuleCode(nonBlank(
                    str(item.get("module_code")),
                    str(item.get("moduleCode"))
            ));
            if (!MODULE_CODE_PATTERN.matcher(moduleCode).matches()) {
                continue;
            }

            SysPermissionGrant grant = new SysPermissionGrant();
            grant.setProfileId(profileId);
            grant.setModuleCode(moduleCode);
            grant.setModuleName(nonBlank(str(item.get("module_name")), str(item.get("moduleName")), moduleCode));
            grant.setCanView(boolVal(item.get("can_view"), item.get("view"), false));
            grant.setCanCreate(boolVal(item.get("can_create"), item.get("create"), false));
            grant.setCanEdit(boolVal(item.get("can_edit"), item.get("edit"), false));
            grant.setCanDelete(boolVal(item.get("can_delete"), item.get("delete"), false));
            grant.setCanReview(boolVal(item.get("can_review"), item.get("review"), false));
            grant.setCanExport(boolVal(item.get("can_export"), item.get("export"), false));
            grant.setCreatedAt(now);
            grant.setUpdatedAt(now);
            sysPermissionGrantService.save(grant);
        }
    }

    private PermissionSnapshot resolveCurrentSnapshotModel() {
        Long userId = SecurityContextHolder.getCurrentUserId();
        String role = safe(SecurityContextHolder.getCurrentRole()).trim().toLowerCase(Locale.ROOT);
        Long partnerId = SecurityContextHolder.getCurrentPartnerId();
        Long permissionProfileId = SecurityContextHolder.getCurrentPermissionProfileId();

        PermissionSnapshot snapshot = new PermissionSnapshot();
        snapshot.setUserId(userId);
        snapshot.setRole(role);
        snapshot.setPartnerId(partnerId);
        snapshot.setPermissionProfileId(permissionProfileId);
        snapshot.setActive(false);
        snapshot.setSuperAdmin(false);
        snapshot.setSource("none");
        snapshot.setModules(new ArrayList<>());
        snapshot.setModuleGrantMap(new LinkedHashMap<>());

        if ("admin".equals(role)) {
            if (permissionProfileId == null) {
                snapshot.setActive(true);
                snapshot.setSuperAdmin(true);
                snapshot.setSource("admin_fallback");
                snapshot.setProfileCode("ADMIN_BASELINE_FULL_ACCESS");
                snapshot.setProfileName("历史管理员兼容全量权限");
                return snapshot;
            }

            SysPermissionProfile profile = sysPermissionProfileService.getById(permissionProfileId);
            if (profile == null) {
                snapshot.setSource("missing_profile");
                return snapshot;
            }

            snapshot.setActive(boolObj(profile.getStatus(), true));
            snapshot.setSuperAdmin(Boolean.TRUE.equals(profile.getIsSuperAdmin()));
            snapshot.setSource("profile");
            snapshot.setProfileCode(safe(profile.getCode()));
            snapshot.setProfileName(safe(profile.getName()));
            snapshot.setBaseRole(safe(profile.getBaseRole()));
            snapshot.setDescription(safe(profile.getDescription()));

            List<SysPermissionGrant> grants = sysPermissionGrantService.list(
                    new QueryWrapper<SysPermissionGrant>()
                            .eq("profile_id", profile.getId())
                            .orderByAsc("id")
            );
            List<ModuleGrant> moduleGrants = grants.stream().map(this::toModuleGrant).toList();
            snapshot.setModules(moduleGrants);
            snapshot.setModuleGrantMap(moduleGrants.stream().collect(
                    Collectors.toMap(ModuleGrant::getModuleCode, item -> item, (a, b) -> a, LinkedHashMap::new)
            ));
            return snapshot;
        }

        snapshot.setActive(true);
        snapshot.setSource("role_baseline");
        snapshot.setProfileCode(role + "_BASELINE");
        snapshot.setProfileName(role + " baseline");
        snapshot.setBaseRole(role);
        return snapshot;
    }

    private Map<String, Object> toSnapshotMap(PermissionSnapshot snapshot) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", snapshot.getUserId());
        data.put("role", safe(snapshot.getRole()));
        data.put("partnerId", snapshot.getPartnerId());
        data.put("permissionProfileId", snapshot.getPermissionProfileId());
        data.put("profileCode", safe(snapshot.getProfileCode()));
        data.put("profileName", safe(snapshot.getProfileName()));
        data.put("baseRole", safe(snapshot.getBaseRole()));
        data.put("description", safe(snapshot.getDescription()));
        data.put("active", snapshot.isActive());
        data.put("isSuperAdmin", snapshot.isSuperAdmin());
        data.put("source", safe(snapshot.getSource()));
        data.put("modules", snapshot.getModules().stream().map(this::toGrantMap).toList());
        return data;
    }

    private Map<String, Object> toLoginModeItem(SysLoginMode mode, SysPermissionProfile profile) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", mode.getId());
        item.put("modeCode", safe(mode.getModeCode()));
        item.put("name", safe(mode.getName()));
        item.put("baseRole", safe(mode.getBaseRole()));
        item.put("enabled", boolObj(mode.getEnabled(), true));
        item.put("defaultProfileId", mode.getDefaultProfileId());
        item.put("defaultProfileCode", profile == null ? "" : safe(profile.getCode()));
        item.put("defaultProfileName", profile == null ? "" : safe(profile.getName()));
        item.put("sortOrder", mode.getSortOrder() == null ? 0 : mode.getSortOrder());
        item.put("description", safe(mode.getDescription()));
        item.put("updatedAt", formatDate(mode.getUpdatedAt()));
        return item;
    }

    private Map<String, Object> toProfileItem(SysPermissionProfile profile, List<SysPermissionGrant> grants) {
        List<SysPermissionGrant> safeGrants = grants == null ? List.of() : grants;
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", profile.getId());
        item.put("code", safe(profile.getCode()));
        item.put("name", safe(profile.getName()));
        item.put("baseRole", safe(profile.getBaseRole()));
        item.put("status", boolObj(profile.getStatus(), true));
        item.put("isSystem", boolObj(profile.getIsSystem(), false));
        item.put("isSuperAdmin", boolObj(profile.getIsSuperAdmin(), false));
        item.put("description", safe(profile.getDescription()));
        item.put("grantCount", safeGrants.size());
        item.put("grants", safeGrants.stream().map(this::toGrantMap).toList());
        item.put("updatedAt", formatDate(profile.getUpdatedAt()));
        return item;
    }

    private Map<String, Object> toGrantMap(SysPermissionGrant grant) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", grant.getId());
        item.put("profileId", grant.getProfileId());
        item.put("moduleCode", safe(grant.getModuleCode()));
        item.put("moduleName", safe(grant.getModuleName()));
        item.put("canView", boolObj(grant.getCanView(), false));
        item.put("canCreate", boolObj(grant.getCanCreate(), false));
        item.put("canEdit", boolObj(grant.getCanEdit(), false));
        item.put("canDelete", boolObj(grant.getCanDelete(), false));
        item.put("canReview", boolObj(grant.getCanReview(), false));
        item.put("canExport", boolObj(grant.getCanExport(), false));
        return item;
    }

    private Map<String, Object> toGrantMap(ModuleGrant grant) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("moduleCode", safe(grant.getModuleCode()));
        item.put("moduleName", safe(grant.getModuleName()));
        item.put("canView", grant.isCanView());
        item.put("canCreate", grant.isCanCreate());
        item.put("canEdit", grant.isCanEdit());
        item.put("canDelete", grant.isCanDelete());
        item.put("canReview", grant.isCanReview());
        item.put("canExport", grant.isCanExport());
        return item;
    }

    private ModuleGrant toModuleGrant(SysPermissionGrant grant) {
        ModuleGrant item = new ModuleGrant();
        item.setModuleCode(safe(grant.getModuleCode()));
        item.setModuleName(safe(grant.getModuleName()));
        item.setCanView(boolObj(grant.getCanView(), false));
        item.setCanCreate(boolObj(grant.getCanCreate(), false));
        item.setCanEdit(boolObj(grant.getCanEdit(), false));
        item.setCanDelete(boolObj(grant.getCanDelete(), false));
        item.setCanReview(boolObj(grant.getCanReview(), false));
        item.setCanExport(boolObj(grant.getCanExport(), false));
        return item;
    }

    private Map<Long, SysPermissionProfile> loadProfileMap() {
        return sysPermissionProfileService.list().stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(SysPermissionProfile::getId, item -> item, (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, List<SysPermissionGrant>> loadGrantMap(List<Long> profileIds) {
        if (profileIds == null || profileIds.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return sysPermissionGrantService.list(
                        new QueryWrapper<SysPermissionGrant>()
                                .in("profile_id", profileIds)
                                .orderByAsc("id")
                ).stream()
                .collect(Collectors.groupingBy(SysPermissionGrant::getProfileId, LinkedHashMap::new, Collectors.toList()));
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toString().replace('T', ' ');
    }

    private String normalizeProfileCode(String value) {
        return safe(value).trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private String normalizeModeCode(String value) {
        return safe(value).trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private String normalizeModuleCode(String value) {
        return safe(value).trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private String normalizeBaseRole(String value) {
        return safe(value).trim().toLowerCase(Locale.ROOT);
    }

    private boolean isUpperCode(String value) {
        return CODE_PATTERN.matcher(safe(value)).matches();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String str(Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            String value = String.valueOf(candidate).trim();
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String nonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return "";
    }

    private int intVal(Object first, Object second, int fallback) {
        Integer direct = toInt(first);
        if (direct != null) {
            return direct;
        }
        Integer alternate = toInt(second);
        return alternate == null ? fallback : alternate;
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long longVal(Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            try {
                String raw = String.valueOf(candidate).trim();
                if (!raw.isEmpty() && !"null".equalsIgnoreCase(raw)) {
                    return Long.parseLong(raw);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private boolean boolVal(Object value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            return fallback;
        }
        if ("1".equals(raw)) {
            return true;
        }
        if ("0".equals(raw)) {
            return false;
        }
        return Boolean.parseBoolean(raw);
    }

    private boolean boolVal(Object primary, Object secondary, boolean fallback) {
        if (primary != null) {
            return boolVal(primary, fallback);
        }
        return boolVal(secondary, fallback);
    }

    private boolean boolObj(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }

    @Data
    public static class PermissionSnapshot {
        private Long userId;
        private String role;
        private Long partnerId;
        private Long permissionProfileId;
        private String profileCode;
        private String profileName;
        private String baseRole;
        private String description;
        private boolean active;
        private boolean superAdmin;
        private String source;
        private List<ModuleGrant> modules;
        private Map<String, ModuleGrant> moduleGrantMap;
    }

    @Data
    public static class ModuleGrant {
        private String moduleCode;
        private String moduleName;
        private boolean canView;
        private boolean canCreate;
        private boolean canEdit;
        private boolean canDelete;
        private boolean canReview;
        private boolean canExport;
    }
}
