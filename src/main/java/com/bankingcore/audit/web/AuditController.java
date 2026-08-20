package com.bankingcore.audit.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bankingcore.audit.application.AuditLogResult;
import com.bankingcore.audit.application.ListAuditLogsUseCase;
import com.bankingcore.audit.web.dtos.AuditLogResponse;

/** Protected by the "/api/admin/**" rule in config.SecurityConfig (ROLE_ADMIN only). */
@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditController {

    private final ListAuditLogsUseCase listAuditLogsUseCase;

    public AuditController(ListAuditLogsUseCase listAuditLogsUseCase) {
        this.listAuditLogsUseCase = listAuditLogsUseCase;
    }

    // page/size default to shared.paging.PageRequest's own defaults (0 / 20);
    // left optional here so existing callers without these params still work,
    // just paginated now instead of getting back every row ever written.
    @GetMapping
    public List<AuditLogResponse> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return listAuditLogsUseCase.execute(page, size).stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLogResult result) {
        return new AuditLogResponse(
                result.id(), result.actorUserId(), result.action(), result.targetType(), result.targetId(),
                result.description(), result.occurredAt());
    }
}
