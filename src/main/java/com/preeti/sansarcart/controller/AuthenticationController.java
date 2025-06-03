package com.preeti.sansarcart.controller;

import com.preeti.sansarcart.common.Util;
import com.preeti.sansarcart.enums.RoleType;
import com.preeti.sansarcart.enums.TokenType;
import com.preeti.sansarcart.exception.custom.TokenException;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.authentication.CustomerRegisterDto;
import com.preeti.sansarcart.payload.authentication.LoginDto;
import com.preeti.sansarcart.entity.Customer;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.payload.authentication.ResetPasswordDto;
import com.preeti.sansarcart.payload.authentication.SellerRegisterDto;
import com.preeti.sansarcart.response.ApiResponse;
import com.preeti.sansarcart.response.LoginResponse;
import com.preeti.sansarcart.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.preeti.sansarcart.common.I18n.i18n;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("auth")
public class AuthenticationController {

    private final CustomerService customerService;
    private final SellerService sellerService;
    private final UserService userService;
    private final TokenService tokenService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register/customer")
    public ResponseEntity<ApiResponse<CustomerRegisterDto>> registerCustomer(@Valid @RequestBody CustomerRegisterDto customerRegisterDto) {
        log.info("Registering customer with email: {}", customerRegisterDto.getEmail());
        Customer registeredCustomer = customerService.signup(customerRegisterDto);
        authenticationService.generateTokenAndSendMail(registeredCustomer, TokenType.ACTIVATION);
        log.info("Customer registered successfully with email: {}", customerRegisterDto.getEmail());
        ApiResponse<CustomerRegisterDto> response = ApiResponse.success(i18n("customer.registration.success"), CustomerRegisterDto.from(registeredCustomer));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/register/seller")
    public ResponseEntity<ApiResponse<SellerRegisterDto>> registerSeller(@Valid @RequestBody SellerRegisterDto sellerRegisterDto) {
        log.info("Registering seller with company name: {}", sellerRegisterDto.getCompanyName());
        SellerRegisterDto seller = sellerService.signup(sellerRegisterDto);
        authenticationService.sendSellerWelcomeMail(seller.getEmail(), seller.getCompanyName());
        log.info("Seller registered successfully with company name: {}", sellerRegisterDto.getCompanyName());
        ApiResponse<SellerRegisterDto> response = ApiResponse.success(i18n("seller.registration.success"), seller);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/activate")
    public ResponseEntity<ApiResponse<Void>> activateCustomer(@RequestParam String token) {
        log.info("Activating user account with token: {}", token);
        tokenService.handleTokenUse(
                token,
                user -> {
                    boolean isCustomer = user.getRoles().stream().anyMatch(role -> role.getAuthority() == RoleType.CUSTOMER);
                    if (!isCustomer) throw new ValidationException(i18n("error.invalidRequest"));
                    userService.activateUserAccount(user);
                },
                user -> authenticationService.generateTokenAndSendMail(user, TokenType.ACTIVATION),
                TokenType.ACTIVATION
        );
        log.info("User account activated successfully with token: {}", token);
        return ResponseEntity.ok(ApiResponse.success(i18n("user.account.activated"), null));
    }

    @PostMapping("/resend-activation-token")
    public ResponseEntity<ApiResponse<Void>> resendActivationToken(@RequestParam String email) {
        log.info("Resending activation token to email: {}", email);
        Util.validateEmail(email);
        User user = userService.getUserByEmail(email);
        if (user.isActive()) {
            throw new ValidationException(i18n("error.accountAlreadyActive"));
        }
        authenticationService.generateTokenAndSendMail(user, TokenType.ACTIVATION);
        log.info("Activation token resent successfully to email: {}", email);
        return ResponseEntity.ok(ApiResponse.success(i18n("activation.token.resent"), null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticate(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {
        log.info("User attempting login with email: {}", loginDto.getEmail());
        LoginResponse loginResponse = authenticationService.authenticateUser(loginDto, response);
        log.info("User logged in successfully with email: {}", loginDto.getEmail());
        return ResponseEntity.ok(ApiResponse.success(i18n("login.success"), loginResponse));
    }

    @PostMapping("/forget-password")
    public ResponseEntity<ApiResponse<Void>> forgetPassword(@RequestParam String email) {
        log.info("Processing password reset for email: {}", email);
        Util.validateEmail(email);
        User user = userService.getUserByEmail(email);
        if (!user.isActive()) {
            throw new ValidationException(i18n("error.accountNotActive"));
        }
        authenticationService.generateTokenAndSendMail(user, TokenType.PASSWORD_RESET);
        log.info("Password reset token sent successfully to email: {}", email);
        return ResponseEntity.ok(ApiResponse.success(i18n("password.reset.token.sent"), null));
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetUserPassword(@RequestParam String token, @Valid @RequestBody ResetPasswordDto dto) {
        log.info("Resetting password with token: {}", token);
        dto.validatePasswords();
        tokenService.handleTokenUse(
                token,
                user -> {
                    userService.updateUserPassword(user, dto.getPassword());
                    authenticationService.sendPasswordUpdateMail(user.getEmail());
                },
                user -> {
                    throw new TokenException(i18n("error.tokenExpired", user.getEmail()));
                },
                TokenType.PASSWORD_RESET
        );
        log.info("Password updated successfully for token: {}", token);
        return ResponseEntity.ok(ApiResponse.success(i18n("password.updated.successfully"), null));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(
            @CookieValue(name = "refreshToken") String refreshToken) {
        try {
            log.info("Refreshing access token using refresh token: {}", refreshToken);
            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(i18n("error.refreshTokenMissing"), null));
            }
            LoginResponse response = authenticationService.refreshAccessToken(refreshToken);
            log.info("Access token refreshed successfully");
            return ResponseEntity.ok(ApiResponse.success(i18n("token.refreshed"), response));
        } catch (RuntimeException e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(i18n("error.tokenRefreshFailed"), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @RequestHeader("Authorization") String authHeader,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ApiResponse.error(i18n("error.accessTokenMissing"), null));
        }
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(i18n("error.refreshTokenMissing"), null));
        }
        String accessToken = authHeader.substring(7);

        authenticationService.logout(accessToken, refreshToken, response);
        return ResponseEntity.ok(ApiResponse.success(i18n("logout.success"), null));
    }
}

