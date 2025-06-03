package com.preeti.sansarcart.service;


import com.preeti.sansarcart.entity.RefreshToken;
import com.preeti.sansarcart.entity.Token;
import com.preeti.sansarcart.entity.User;
import com.preeti.sansarcart.enums.RoleType;
import com.preeti.sansarcart.enums.TokenType;
import com.preeti.sansarcart.exception.custom.ValidationException;
import com.preeti.sansarcart.payload.authentication.LoginDto;
import com.preeti.sansarcart.payload.email.EmailDetails;
import com.preeti.sansarcart.response.LoginResponse;
import com.preeti.sansarcart.security.UserDetailsImp;
import com.preeti.sansarcart.security.service.JWTService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final TokenService tokenService;
    private final EmailService emailService;
    private final UserService userService;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AccessTokenBlacklistService accessTokenBlacklistService;

   public void generateTokenAndSendMail(User user, TokenType tokenType){
       Token token=tokenService.createToken(user,tokenType);
       EmailDetails emailDetails =switch (tokenType) {
           case ACTIVATION -> {
               boolean isCustomer = user.getRoles().stream()
                       .anyMatch(role -> role.getAuthority() == RoleType.CUSTOMER);
               if(!isCustomer) throw new ValidationException("Invalid Request");
               yield EmailBuilderService.buildActivationTokenEmail(user.getEmail(), token.getToken());
           }
           case PASSWORD_RESET -> EmailBuilderService.buildPasswordResetEmail(user.getEmail(), token.getToken());
       };
       emailService.sendEmail(emailDetails);
   }

   public void sendPasswordUpdateMail(String email){
       emailService.sendEmail(EmailBuilderService.buildPasswordUpdatedEmail(email));
   }

   public void sendSellerWelcomeMail(String email, String name){
       emailService.sendEmail(EmailBuilderService.buildSellerRegistrationEmail(email,name));
   }

    public LoginResponse authenticateUser(@Valid LoginDto loginDto, HttpServletResponse response) {
        User authenticatedUser = userService.authenticate(loginDto);

        String accessToken = jwtService.generateAccessToken(authenticatedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authenticatedUser);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(Duration.ofMillis(refreshTokenService.getRefreshTokenDurationMs()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new LoginResponse(accessToken, jwtService.getAccessTokenExpiration());
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || !refreshTokenService.isValid(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        RefreshToken tokenEntity = refreshTokenService.getValidToken(refreshToken);
        User user = tokenEntity.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        return new LoginResponse(newAccessToken, jwtService.getAccessTokenExpiration());
    }

    public void logout(String accessToken, String refreshToken, HttpServletResponse response) {
        Date expiry = jwtService.extractClaim(accessToken, Claims::getExpiration);
        accessTokenBlacklistService.blacklist(accessToken, expiry.getTime());
        if (refreshToken != null) {
            refreshTokenService.invalidateToken(refreshToken);
        }
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void blacklistAccessToken(String accessToken){
        Date expiry = jwtService.extractClaim(accessToken, Claims::getExpiration);
        accessTokenBlacklistService.blacklist(accessToken, expiry.getTime());
    }


    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("No authenticated user found.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImp userDetails) {
            return userService.getUserById(userDetails.getUser().getId());
        } else {
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass());
        }
    }

    public boolean userHasAdminRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getAuthority().equals(RoleType.ADMIN));

    }

}
