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
@RequestMapping("/v3/member/customer-service")
@RequiredArgsConstructor
public class MemberCustomerServiceV3Controller {

    private final ICustomerServiceMessageService customerServiceMessageService;
    private final ICustomerServiceTicketService customerServiceTicketService;
    private final ICustomerServiceTicketFileService customerServiceTicketFileService;
    private final IMemberProfileService memberProfileService;
    private final ISysUserService sysUserService;

    @GetMapping("/tickets")
    public V3Response<V3PageData<Map<String, Object>>> listTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "10") long pageSize
    ) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        QueryWrapper<CustomerServiceTicket> qw = new QueryWrapper<CustomerServiceTicket>()
                .eq("user_id", userId);
        if (!isBlank(status)) {
            qw.eq("status", normalizeStatus(status, "open"));
        }
        if (!isBlank(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like("ticket_no", kw)
                    .or().like("title", kw)
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
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        CustomerServiceTicket ticket = customerServiceTicketService.getById(id);
        if (ticket == null) {
            return V3Response.error("RESOURCE_NOT_FOUND", "ticket not found");
        }
        if (!userId.equals(ticket.getUserId())) {
            return V3Response.error("AUTH_FORBIDDEN", "forbidden");
        }

        return V3Response.success(toTicketDetailItem(ticket));
    }

    @Transactional
    @PostMapping("/tickets")
    public V3Response<Map<String, Object>> createTicket(@RequestBody Map<String, Object> body) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        String title = str(body.get("title"));
        String content = firstNonBlank(str(body.get("content")), str(body.get("desc")));
        if (title.isEmpty() || content.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "title and content are required");
        }

        MemberProfile profile = getMemberProfile(userId);
        SysUser user = sysUserService.getById(userId);

        String contactName = firstNonBlank(str(body.get("contact_name")), str(body.get("contactName")),
                profile == null ? "" : safe(profile.getContactPerson()));
        String contactPhone = firstNonBlank(str(body.get("contact_phone")), str(body.get("contactPhone")),
                profile == null ? "" : safe(profile.getContactPhone()),
                user == null ? "" : safe(user.getPhone()));
        if (contactName.isEmpty() || contactPhone.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "contact_name and contact_phone are required");
        }

        Date now = new Date();
        CustomerServiceTicket ticket = new CustomerServiceTicket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setUserId(userId);
        ticket.setPartnerId(SecurityContextHolder.getCurrentPartnerId());
        ticket.setMemberLevelSnapshot(resolveMemberLevel(profile));
        ticket.setTicketType(normalizeTicketType(firstNonBlank(str(body.get("ticket_type")), str(body.get("type")))));
        ticket.setPriority(normalizePriority(str(body.get("priority"))));
        ticket.setTitle(title);
        ticket.setContent(content);
        ticket.setContactName(contactName);
        ticket.setContactPhone(contactPhone);
        ticket.setStatus("open");
        ticket.setReply("");
        ticket.setSourceChannel("member_portal");
        ticket.setCreatedByAdmin(false);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        customerServiceTicketService.save(ticket);

        replaceTicketFiles(ticket.getId(), body.containsKey("attachments") ? body.get("attachments") : body.get("files"), userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", ticket.getId());
        result.put("ticketNo", ticket.getTicketNo());
        return V3Response.success(result);
    }

    @Transactional
    @PostMapping("/messages")
    public V3Response<Map<String, Object>> createMessage(@RequestBody Map<String, Object> body) {
        Long userId = SecurityContextHolder.getCurrentUserId();
        if (userId == null || userId <= 0) {
            return V3Response.error("AUTH_UNAUTHORIZED", "login required");
        }

        String content = str(body.get("content"));
        if (content.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "content is required");
        }

        MemberProfile profile = getMemberProfile(userId);
        SysUser user = sysUserService.getById(userId);
        String name = firstNonBlank(str(body.get("name")),
                profile == null ? "" : safe(profile.getContactPerson()),
                user == null ? "" : safe(user.getUsername()));
        String phone = firstNonBlank(str(body.get("phone")),
                profile == null ? "" : safe(profile.getContactPhone()),
                user == null ? "" : safe(user.getPhone()));
        if (name.isEmpty() || phone.isEmpty()) {
            return V3Response.error("VALIDATION_ERROR", "name and phone are required");
        }

        Date now = new Date();
        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setUserId(userId);
        message.setMemberLevelSnapshot(resolveMemberLevel(profile));
        message.setName(name);
        message.setPhone(phone);
        message.setSourceChannel("member_portal");
        message.setContent(content);
        message.setStatus("pending");
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        customerServiceMessageService.save(message);

        return V3Response.success(Map.of("id", message.getId(), "status", "pending"));
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

    private Map<String, Object> toTicketListItem(CustomerServiceTicket ticket) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", ticket.getId());
        item.put("ticketNo", safe(ticket.getTicketNo()));
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
        item.put("reply", safe(ticket.getReply()));
        item.put("date", fmtDateTime(ticket.getCreatedAt()));
        item.put("updateTime", fmtDateTime(ticket.getUpdatedAt()));
        return item;
    }

    private Map<String, Object> toTicketDetailItem(CustomerServiceTicket ticket) {
        Map<String, Object> item = toTicketListItem(ticket);
        item.put("userId", ticket.getUserId());
        item.put("partnerId", ticket.getPartnerId());
        item.put("memberLevelSnapshot", safe(ticket.getMemberLevelSnapshot()));
        item.put("content", safe(ticket.getContent()));
        item.put("sourceChannel", safe(ticket.getSourceChannel()));
        item.put("createdByAdmin", Boolean.TRUE.equals(ticket.getCreatedByAdmin()));
        item.put("closedAt", fmtDateTime(ticket.getClosedAt()));
        item.put("attachments", buildAttachments(ticket.getId()));
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

    private String generateTicketNo() {
        return "TK" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%03d", new Random().nextInt(1000));
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

    private String normalizeStatus(String raw, String fallback) {
        String value = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "open", "processing", "closed" -> value;
            default -> fallback;
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
            case "closed" -> "已解决";
            default -> "待处理";
        };
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
