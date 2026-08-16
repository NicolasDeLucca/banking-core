package com.bankingcore.audit.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping
    public List<AuditLogResponse> list() {
        return listAuditLogsUseCase.execute().stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLogResult result) {
        return new AuditLogResponse(
                result.id(), result.actorUserId(), result.action(), result.targetType(), result.targetId(),
                result.description(), result.occurredAt());
    }
}
