package com.preeti.sansarcart.controller;

import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.projection.SellerListView;
import com.preeti.sansarcart.projection.UserListView;
import com.preeti.sansarcart.response.ApiResponse;
import com.preeti.sansarcart.response.MetaData;
import com.preeti.sansarcart.service.AdminService;
import com.preeti.sansarcart.service.CustomerService;
import com.preeti.sansarcart.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.preeti.sansarcart.common.I18n.i18n;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("admin")
@Secured(value ="ROLE_ADMIN")
public class AdminUserController {

    private final AdminService adminService;
    private final SellerService sellerService;
    private final CustomerService customerService;

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<UserListView>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page, // offset
            @RequestParam(defaultValue = "10") int size, // size of one page
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String email
    ){
        log.info("Admin: Fetching customers with filters - page: {}, size: {}, sortBy: {}, sortDir: {}, email: {}",
                page, size, sortBy, sortDir, email);

        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new ValidationException(i18n("validation.sort.direction.invalid", sortDir));
        }

        Map<String, String> filters = new HashMap<>();
        if (StringUtils.hasText(email)) {
            filters.put("email", email);
        }

        List<UserListView> customers = customerService.getAllCustomers(page, size, sortBy, sortDir, email);
        MetaData metaData = MetaData.ofPagination(size, page, sortBy, sortDir, filters);

        log.info("Admin: Fetched {} customers", customers.size());
        return ResponseEntity.ok(ApiResponse.success(i18n("admin.customers.fetched"), customers, metaData));
    }

    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<SellerListView>>> getAllSellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String email
    ){
        log.info("Admin: Fetching sellers with filters - page: {}, size: {}, sortBy: {}, sortDir: {}, email: {}",
                page, size, sortBy, sortDir, email);

        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new ValidationException(i18n("validation.sort.direction.invalid", sortDir));
        }

        Map<String, String> filters = new HashMap<>();
        if (StringUtils.hasText(email)) {
            filters.put("email", email);
        }

        List<SellerListView> sellers = sellerService.getAllSellers(page, size, sortBy, sortDir, email);
        MetaData metaData = MetaData.ofPagination(size, page, sortBy, sortDir, filters);

        log.info("Admin: Fetched {} sellers", sellers.size());
        return ResponseEntity.ok(ApiResponse.success(i18n("admin.sellers.fetched"), sellers, metaData));
    }

    @PatchMapping({"user/{id}/activate", "seller/{id}/activate", "customer/{id}/activate"})
    public ResponseEntity<ApiResponse<Void>> activateCustomer(@PathVariable UUID id) {
        log.info("Admin: Activating user with id: {}", id);

        boolean isUserAccountActivated = adminService.activateUserAndSendMail(id);
        if (!isUserAccountActivated) {
            log.info("Admin: User with id: {} already activated", id);
            return ResponseEntity.ok(ApiResponse.success(i18n("admin.user.already.activated"), null));
        }

        log.info("Admin: User with id: {} activated successfully", id);
        return ResponseEntity.ok(ApiResponse.success(i18n("admin.user.activated"), null));
    }

    @PatchMapping({"user/{id}/deactivate", "seller/{id}/deactivate", "customer/{id}/deactivate"})
    public ResponseEntity<ApiResponse<Void>> deactivateCustomer(@PathVariable UUID id) {
        log.info("Admin: Deactivating user with id: {}", id);

        boolean isUserAccountDeactivated = adminService.deactivateUserAndSendMail(id);
        if (!isUserAccountDeactivated) {
            log.info("Admin: User with id: {} already deactivated", id);
            return ResponseEntity.ok(ApiResponse.success(i18n("admin.user.already.deactivated"), null));
        }

        log.info("Admin: User with id: {} deactivated successfully", id);
        return ResponseEntity.ok(ApiResponse.success(i18n("admin.user.deactivated"), null));
    }
}
