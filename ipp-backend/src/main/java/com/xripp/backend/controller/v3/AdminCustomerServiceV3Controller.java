package com.xripp.backend.controller.v3;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xripp.backend.common.V3PageData;
import com.xripp.backend.common.V3Response;
import com.xripp.backend.entity.CustomerServiceMessage;
import com.xripp.backend.entity.CustomerServiceTicket;
import com.xripp.backend.entity.CustomerServiceTicketFile;
import com.xripp.backend.entity.MemberProfile;
import com.xripp.backend.entity.SysUser;
import com.xripp.backend.security.SecurityContextHolder;
import com.xripp.backend.service.ICustomerServiceMessageService;
import com.xripp.backend.service.ICustomerServiceTicketFileService;
import com.xripp.backend.service.ICustomerServiceTicketService;
import com.xripp.backend.service.IMemberProfileService;
import com.xripp.backend.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v3/admin/customer-service")
@RequiredArgsConstructor
public class AdminCustomerServiceV3Controller {

    private final ICustomerServiceMessageService customerServiceMessageService;
    private final ICustomerServiceTicketService customerServiceTicketService;
    private final ICustomerServiceTicketFileService customerServiceTicketFileService;
    private final IMemberProfileService memberProfileService;
    private final ISysUserService sysUserService;

    @GetMapping("/overview")
    public V3Response<Map<String, Object>> overview() {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        Date todayStart = startOfToday();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayMessageCount", customerServiceMessageService.count(
                new QueryWrapper<CustomerServiceMessage>().ge("created_at", todayStart)));
        result.put("pendingMessageCount", customerServiceMessageService.count(
                new QueryWrapper<CustomerServiceMessage>().eq("status", "pending")));
        result.put("openTicketCount", customerServiceTicketService.count(
                new QueryWrapper<CustomerServiceTicket>().eq("status", "open")));
        result.put("processingTicketCount", customerServiceTicketService.count(
                new QueryWrapper<CustomerServiceTicket>().eq("status", "processing")));
        result.put("closedTicketCount", customerServiceTicketService.count(
                new QueryWrapper<CustomerServiceTicket>().eq("status", "closed")));
        return V3Response.success(result);
    }

    @GetMapping("/messages")
    public V3Response<V3PageData<Map<String, Object>>> listMessages(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        QueryWrapper<CustomerServiceMessage> qw = new QueryWrapper<>();
        if (!isBlank(status)) {
            qw.eq("status", normalizeMessageStatus(status));
        }
        if (!isBlank(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like("name", kw)
                    .or().like("phone", kw)
                    .or().like("content", kw));
        }
        qw.orderByDesc("created_at").orderByDesc("id");

        Page<CustomerServiceMessage> result = customerServiceMessageService.page(new Page<>(page, pageSize), qw);
        List<Map<String, Object>> items = new ArrayList<>();
        for (CustomerServiceMessage message : result.getRecords()) {
            items.add(toMessageItem(message));
        }
        return V3Response.success(new V3PageData<>(items, result.getCurrent(), result.getSize(), result.getTotal()));
    }

    @Transactional
    @PostMapping("/messages/{id}/handle")
    public V3Response<Map<String, Object>> handleMessage(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        CustomerServiceMessage message = customerServiceMessageService.getById(id);
        if (message == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "message not found");
        }

        Date now = new Date();
        message.setStatus(normalizeMessageStatus(firstNonBlank(str(body.get("status")), "done")));
        message.setHandleRemark(firstNonBlank(str(body.get("handle_remark")), str(body.get("handleRemark"))));
        message.setHandledBy(SecurityContextHolder.getCurrentUserId());
        message.setHandledAt(now);
        message.setUpdatedAt(now);
        customerServiceMessageService.updateById(message);

        return V3Response.success(toMessageItem(message));
    }

    @GetMapping("/tickets")
    public V3Response<V3PageData<Map<String, Object>>> listTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize
    ) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        QueryWrapper<CustomerServiceTicket> qw = new QueryWrapper<>();
        if (!isBlank(status)) {
            qw.eq("status", normalizeTicketStatus(status));
        }
        if (!isBlank(priority)) {
            qw.eq("priority", normalizePriority(priority));
        }
        if (!isBlank(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like("ticket_no", kw)
                    .or().like("title", kw)
                    .or().like("contact_name", kw)
                    .or().like("contact_phone", kw)
                    .or().like("content", kw));
        }
        qw.orderByDesc("updated_at").orderByDesc("id");

        Page<CustomerServiceTicket> result = customerServiceTicketService.page(new Page<>(page, pageSize), qw);
        List<Map<String, Object>> items = new ArrayList<>();
        for (CustomerServiceTicket ticket : result.getRecords()) {
            items.add(toTicketListItem(ticket));
        }
        return V3Response.success(new V3PageData<>(items, result.getCurrent(), result.getSize(), result.getTotal()));
    }

    @GetMapping("/tickets/{id}")
    public V3Response<Map<String, Object>> ticketDetail(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        CustomerServiceTicket ticket = customerServiceTicketService.getById(id);
        if (ticket == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "ticket not found");
        }
        return V3Response.success(toTicketDetailItem(ticket));
    }

    @Transactional
    @PostMapping("/tickets")
    public V3Response<Map<String, Object>> createTicket(@RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        String title = str(body.get("title"));
        String content = firstNonBlank(str(body.get("content")), str(body.get("desc")));

        Long userId = longObj(body.get("user_id"));
        if (userId == null) {
            userId = longObj(body.get("userId"));
        }

        MemberProfile profile = userId == null ? null : getMemberProfile(userId);
        SysUser user = userId == null ? null : sysUserService.getById(userId);
        String contactName = firstNonBlank(
                str(body.get("contact_name")),
                str(body.get("contactName")),
                profile == null ? "" : safe(profile.getContactPerson()),
                user == null ? "" : safe(user.getUsername())
        );
        String contactPhone = firstNonBlank(
                str(body.get("contact_phone")),
                str(body.get("contactPhone")),
                profile == null ? "" : safe(profile.getContactPhone()),
                user == null ? "" : safe(user.getPhone())
        );
        if (title.isEmpty() || content.isEmpty() || contactName.isEmpty() || contactPhone.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "title/content/contact_name/contact_phone are required");
        }

        Long partnerId = longObj(body.get("partner_id"));
        if (partnerId == null) {
            partnerId = longObj(body.get("partnerId"));
        }
        if (partnerId == null && user != null) {
            partnerId = user.getPartnerId();
        }

        Date now = new Date();
        CustomerServiceTicket ticket = new CustomerServiceTicket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setUserId(userId);
        ticket.setPartnerId(partnerId);
        ticket.setMemberLevelSnapshot(resolveMemberLevel(profile));
        ticket.setTicketType(normalizeTicketType(firstNonBlank(str(body.get("ticket_type")), str(body.get("type")))));
        ticket.setPriority(normalizePriority(str(body.get("priority"))));
        ticket.setTitle(title);
        ticket.setContent(content);
        ticket.setContactName(contactName);
        ticket.setContactPhone(contactPhone);
        ticket.setStatus(normalizeTicketStatus(firstNonBlank(str(body.get("status")), "open")));
        ticket.setReply(firstNonBlank(str(body.get("reply")), ""));
        ticket.setSourceChannel("admin_manual");
        ticket.setCreatedByAdmin(true);
        ticket.setLastRepliedBy(SecurityContextHolder.getCurrentUserId());
        ticket.setClosedAt("closed".equals(ticket.getStatus()) ? now : null);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        customerServiceTicketService.save(ticket);

        replaceTicketFiles(
                ticket.getId(),
                body.containsKey("attachments") ? body.get("attachments") : body.get("files"),
                SecurityContextHolder.getCurrentUserId()
        );

        return V3Response.success(toTicketDetailItem(ticket));
    }

    @Transactional
    @PutMapping("/tickets/{id}")
    public V3Response<Map<String, Object>> updateTicket(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        CustomerServiceTicket ticket = customerServiceTicketService.getById(id);
        if (ticket == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "ticket not found");
        }

        String title = firstNonBlank(str(body.get("title")), safe(ticket.getTitle()));
        String content = firstNonBlank(str(body.get("content")), safe(ticket.getContent()));
        String contactName = firstNonBlank(str(body.get("contact_name")), str(body.get("contactName")), safe(ticket.getContactName()));
        String contactPhone = firstNonBlank(str(body.get("contact_phone")), str(body.get("contactPhone")), safe(ticket.getContactPhone()));
        if (title.isEmpty() || content.isEmpty() || contactName.isEmpty() || contactPhone.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "title/content/contact_name/contact_phone are required");
        }

        Date now = new Date();
        ticket.setTitle(title);
        ticket.setContent(content);
        ticket.setContactName(contactName);
        ticket.setContactPhone(contactPhone);
        ticket.setTicketType(normalizeTicketType(firstNonBlank(str(body.get("ticket_type")), str(body.get("type")), safe(ticket.getTicketType()))));
        ticket.setPriority(normalizePriority(firstNonBlank(str(body.get("priority")), safe(ticket.getPriority()))));
        ticket.setStatus(normalizeTicketStatus(firstNonBlank(str(body.get("status")), safe(ticket.getStatus()))));
        ticket.setReply(firstNonBlank(str(body.get("reply")), safe(ticket.getReply())));
        ticket.setLastRepliedBy(SecurityContextHolder.getCurrentUserId());
        ticket.setClosedAt("closed".equals(ticket.getStatus()) ? now : null);
        ticket.setUpdatedAt(now);
        customerServiceTicketService.updateById(ticket);

        if (body.containsKey("attachments") || body.containsKey("files")) {
            replaceTicketFiles(
                    ticket.getId(),
                    body.containsKey("attachments") ? body.get("attachments") : body.get("files"),
                    SecurityContextHolder.getCurrentUserId()
            );
        }

        return V3Response.success(toTicketDetailItem(ticket));
    }

    @Transactional
    @DeleteMapping("/tickets/{id}")
    public V3Response<Map<String, Object>> deleteTicket(@PathVariable Long id) {
        if (!SecurityContextHolder.isAdmin()) {
            return V3Response.error("AUTH_FORBIDDEN", "admin only");
        }

        CustomerServiceTicket ticket = customerServiceTicketService.getById(id);
        if (ticket == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "ticket not found");
        }

        customerServiceTicketFileService.remove(new QueryWrapper<CustomerServiceTicketFile>().eq("ticket_id", id));
        customerServiceTicketService.removeById(id);
        return V3Response.success(Map.of("id", id, "deleted", true));
    }

    private MemberProfile getMemberProfile(Long userId) {
        return memberProfileService.getOne(new QueryWrapper<MemberProfile>().eq("user_id", userId), false);
    }

    private void replaceTicketFiles(Long ticketId, Object rawAttachments, Long uploadedBy) {
        customerServiceTicketFileService.remove(new QueryWrapper<CustomerServiceTicketFile>().eq("ticket_id", ticketId));
        List<Map<String, Object>> attachments = normalizeAttachmentList(rawAttachments);
        Date now = new Date();
        for (int i = 0; i < attachments.size(); i++) {
            Map<String, Object> item = attachments.get(i);
            String fileUrl = firstNonBlank(str(item.get("fileUrl")), str(item.get("url")));
            if (fileUrl.isEmpty()) {
                continue;
            }
            CustomerServiceTicketFile file = new CustomerServiceTicketFile();
            file.setTicketId(ticketId);
            file.setFileName(firstNonBlank(str(item.get("fileName")), str(item.get("name")), extractFileName(fileUrl)));
            file.setFileUrl(fileUrl);
            file.setFileExt(firstNonBlank(str(item.get("fileExt")), extractExt(fileUrl)));
            file.setFileSize(longVal(item.get("fileSize")));
            file.setContentType(str(item.get("contentType")));
            file.setSortOrder(i + 1);
            file.setUploadedBy(uploadedBy);
            file.setCreatedAt(now);
            file.setUpdatedAt(now);
            customerServiceTicketFileService.save(file);
        }
    }

    private List<Map<String, Object>> normalizeAttachmentList(Object rawAttachments) {
        if (!(rawAttachments instanceof List<?> list)) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> attachments = new ArrayList<>();
        for (Object raw : list) {
            if (raw instanceof Map<?, ?> rawMap) {
                Map<String, Object> item = new LinkedHashMap<>();
                rawMap.forEach((key, value) -> item.put(String.valueOf(key), value));
                attachments.add(item);
            }
        }
        return attachments;
    }

    private Map<String, Object> toMessageItem(CustomerServiceMessage message) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", message.getId());
        item.put("userId", message.getUserId());
        item.put("memberLevelSnapshot", safe(message.getMemberLevelSnapshot()));
        item.put("name", safe(message.getName()));
        item.put("phone", safe(message.getPhone()));
        item.put("content", safe(message.getContent()));
        item.put("status", safe(message.getStatus()));
        item.put("statusLabel", "done".equals(message.getStatus()) ? "已处理" : "未处理");
        item.put("handleRemark", safe(message.getHandleRemark()));
        item.put("handledBy", message.getHandledBy());
        item.put("handledAt", fmtDateTime(message.getHandledAt()));
        item.put("date", fmtDateTime(message.getCreatedAt()));
        item.put("createdAt", fmtDateTime(message.getCreatedAt()));
        item.put("updatedAt", fmtDateTime(message.getUpdatedAt()));
        return item;
    }

    private Map<String, Object> toTicketListItem(CustomerServiceTicket ticket) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", ticket.getId());
        item.put("ticketNo", safe(ticket.getTicketNo()));
        item.put("userId", ticket.getUserId());
        item.put("partnerId", ticket.getPartnerId());
        item.put("memberLevelSnapshot", safe(ticket.getMemberLevelSnapshot()));
        item.put("title", safe(ticket.getTitle()));
        item.put("type", safe(ticket.getTicketType()));
        item.put("typeLabel", mapTicketTypeLabel(ticket.getTicketType()));
        item.put("priority", safe(ticket.getPriority()));
        item.put("priorityLabel", mapPriorityLabel(ticket.getPriority()));
        item.put("status", safe(ticket.getStatus()));
        item.put("statusLabel", mapStatusLabel(ticket.getStatus()));
        item.put("contactName", safe(ticket.getContactName()));
        item.put("contactPhone", safe(ticket.getContactPhone()));
        item.put("contact", buildContact(ticket.getContactName(), ticket.getContactPhone()));
        item.put("content", safe(ticket.getContent()));
        item.put("reply", safe(ticket.getReply()));
        item.put("date", fmtDateTime(ticket.getCreatedAt()));
        item.put("updateTime", fmtDateTime(ticket.getUpdatedAt()));
        return item;
    }

    private Map<String, Object> toTicketDetailItem(CustomerServiceTicket ticket) {
        Map<String, Object> item = toTicketListItem(ticket);
        item.put("sourceChannel", safe(ticket.getSourceChannel()));
        item.put("createdByAdmin", Boolean.TRUE.equals(ticket.getCreatedByAdmin()));
        item.put("lastRepliedBy", ticket.getLastRepliedBy());
        item.put("closedAt", fmtDateTime(ticket.getClosedAt()));
        item.put("attachments", buildAttachments(ticket.getId()));
        item.put("memberProfile", buildMemberProfile(ticket.getUserId()));
        return item;
    }

    private Map<String, Object> buildMemberProfile(Long userId) {
        if (userId == null) {
            return new LinkedHashMap<>();
        }
        MemberProfile profile = getMemberProfile(userId);
        SysUser user = sysUserService.getById(userId);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("userId", userId);
        item.put("username", user == null ? "" : safe(user.getUsername()));
        item.put("phone", user == null ? "" : safe(user.getPhone()));
        item.put("partnerId", user == null ? null : user.getPartnerId());
        item.put("companyName", profile == null ? "" : safe(profile.getCompanyName()));
        item.put("contactPerson", profile == null ? "" : safe(profile.getContactPerson()));
        item.put("contactPhone", profile == null ? "" : safe(profile.getContactPhone()));
        item.put("memberLevel", resolveMemberLevel(profile));
        return item;
    }

    private List<Map<String, Object>> buildAttachments(Long ticketId) {
        List<CustomerServiceTicketFile> files = customerServiceTicketFileService.list(
                new QueryWrapper<CustomerServiceTicketFile>()
                        .eq("ticket_id", ticketId)
                        .orderByAsc("sort_order")
                        .orderByAsc("id")
        );
        List<Map<String, Object>> items = new ArrayList<>();
        for (CustomerServiceTicketFile file : files) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", file.getId());
            item.put("fileName", safe(file.getFileName()));
            item.put("fileUrl", safe(file.getFileUrl()));
            item.put("fileExt", safe(file.getFileExt()));
            item.put("fileSize", file.getFileSize());
            item.put("contentType", safe(file.getContentType()));
            item.put("sortOrder", file.getSortOrder());
            items.add(item);
        }
        return items;
    }

    private String resolveMemberLevel(MemberProfile profile) {
        if (profile == null) {
            return "NORMAL";
        }
        String level = firstNonBlank(safe(profile.getMemberLevel()), safe(profile.getVipLevel()));
        if (level.isEmpty()) {
            return "NORMAL";
        }
        String normalized = level.trim().toUpperCase(Locale.ROOT);
        return List.of("NORMAL", "VIP", "SVIP").contains(normalized) ? normalized : "NORMAL";
    }

    private Date startOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private String generateTicketNo() {
        return "TK" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%03d", new Random().nextInt(1000));
    }

    private String normalizeMessageStatus(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return "done".equals(value) ? "done" : "pending";
    }

    private String normalizeTicketType(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "财务", "finance" -> "finance";
            case "账号", "account" -> "account";
            case "标书", "tender" -> "tender";
            case "活动", "activity" -> "activity";
            default -> "other";
        };
    }

    private String normalizePriority(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "low" -> "low";
            case "high" -> "high";
            case "urgent", "非常紧急" -> "urgent";
            case "一般", "normal", "" -> "normal";
            case "紧急" -> "high";
            default -> "normal";
        };
    }

    private String normalizeTicketStatus(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "processing" -> "processing";
            case "closed" -> "closed";
            default -> "open";
        };
    }

    private String mapTicketTypeLabel(String ticketType) {
        return switch (safe(ticketType)) {
            case "finance" -> "财务";
            case "account" -> "账号";
            case "tender" -> "标书";
            case "activity" -> "活动";
            default -> "其他";
        };
    }

    private String mapPriorityLabel(String priority) {
        return switch (safe(priority)) {
            case "low" -> "低";
            case "high" -> "高";
            case "urgent" -> "非常紧急";
            default -> "一般";
        };
    }

    private String mapStatusLabel(String status) {
        return switch (safe(status)) {
            case "processing" -> "处理中";
            case "closed" -> "已完成";
            default -> "待处理";
        };
    }

    private String buildContact(String contactName, String contactPhone) {
        String name = safe(contactName);
        String phone = safe(contactPhone);
        if (name.isEmpty()) {
            return phone;
        }
        if (phone.isEmpty()) {
            return name;
        }
        return name + " / " + phone;
    }

    private String fmtDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private String extractFileName(String fileUrl) {
        if (isBlank(fileUrl)) {
            return "";
        }
        int idx = fileUrl.lastIndexOf('/');
        return idx >= 0 ? fileUrl.substring(idx + 1) : fileUrl;
    }

    private String extractExt(String fileUrl) {
        String fileName = extractFileName(fileUrl);
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private long longVal(Object value) {
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private Long longObj(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
