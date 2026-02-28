package com.xripp.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xripp.backend.entity.MemberProfile;
import com.xripp.backend.entity.Partner;
import com.xripp.backend.service.IMemberProfileService;
import com.xripp.backend.service.IPartnerService;
import com.xripp.backend.service.StateTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemScheduledTasks {

    private final IPartnerService partnerService;
    private final IMemberProfileService memberProfileService;
    private final StateTransitionService stateTransitionService;

    /**
     * Partner renewal reminder — runs daily at 02:00.
     * Finds partners expiring within 30 days that haven't been reminded yet.
     * Phase E: send actual notification (SMS/email).
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void partnerRenewalReminder() {
        log.info("[ScheduledTask] partnerRenewalReminder started");

        try {
            LocalDate threshold = LocalDate.now().plusDays(30);

            QueryWrapper<Partner> qw = new QueryWrapper<>();
            qw.le("expire_date", threshold);
            qw.eq("status", 1);
            qw.and(w -> w.eq("renewal_reminder_sent", false).or().isNull("renewal_reminder_sent"));

            List<Partner> partners = partnerService.list(qw);

            if (partners.isEmpty()) {
                log.info("[ScheduledTask] partnerRenewalReminder: no partners need reminding");
                return;
            }

            int count = 0;
            for (Partner p : partners) {
                log.warn("[ScheduledTask] Partner '{}' (id={}) expires on {} — renewal reminder needed",
                        p.getPartnerName(), p.getId(), p.getExpireDate());

                UpdateWrapper<Partner> uw = new UpdateWrapper<>();
                uw.eq("id", p.getId());
                uw.set("renewal_reminder_sent", true);
                partnerService.update(uw);
                count++;
            }

            log.info("[ScheduledTask] partnerRenewalReminder completed: {} partners reminded", count);
        } catch (Exception e) {
            log.error("[ScheduledTask] partnerRenewalReminder failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Member auto-downgrade — runs daily at 04:00.
     * Downgrades expired VIP/SVIP members back to normal.
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void memberAutoDowngrade() {
        log.info("[ScheduledTask] memberAutoDowngrade started");

        try {
            QueryWrapper<MemberProfile> qw = new QueryWrapper<>();
            qw.lt("vip_expire_time", new Date());
            qw.ne("member_level", "normal");

            List<MemberProfile> expired = memberProfileService.list(qw);

            if (expired.isEmpty()) {
                log.info("[ScheduledTask] memberAutoDowngrade: no expired members");
                return;
            }

            int count = 0;
            for (MemberProfile mp : expired) {
                String oldLevel = mp.getMemberLevel();

                mp.setMemberLevel("normal");
                mp.setVipLevel("FREE");
                memberProfileService.updateById(mp);

                stateTransitionService.log(
                        "member_profile", mp.getId(),
                        oldLevel, "normal",
                        "auto_downgrade",
                        "VIP expired at " + mp.getVipExpireTime()
                );

                log.info("[ScheduledTask] member id={} downgraded {} -> normal (expired: {})",
                        mp.getId(), oldLevel, mp.getVipExpireTime());
                count++;
            }

            log.info("[ScheduledTask] memberAutoDowngrade completed: {} members downgraded", count);
        } catch (Exception e) {
            log.error("[ScheduledTask] memberAutoDowngrade failed: {}", e.getMessage(), e);
        }
    }
}
