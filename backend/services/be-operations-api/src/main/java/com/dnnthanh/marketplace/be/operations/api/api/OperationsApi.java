package com.dnnthanh.marketplace.be.operations.api.api;

import com.dnnthanh.marketplace.be.operations.api.api.request.RecoverRequest;
import com.dnnthanh.marketplace.be.operations.api.api.response.IncidentView;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/private/operations")
public interface OperationsApi {

    @GetMapping("/incidents")
    @PreAuthorize("@authorizationService.hasPermission('OPERATIONS_VIEW')")
    List<IncidentView> open();

    @PostMapping("/incidents/{id}/recover")
    @PreAuthorize("@authorizationService.hasPermission('OPERATIONS_RECOVER')")
    void recover(@PathVariable Long id, @RequestBody RecoverRequest request);

    @PostMapping("/incidents/{id}/resolve")
    @PreAuthorize("@authorizationService.hasPermission('OPERATIONS_RECOVER')")
    void resolve(@PathVariable Long id, @RequestBody RecoverRequest request);
}
