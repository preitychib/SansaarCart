//package com.preeti.sansarcart.filter;
//
//import com.preeti.sansarcart.common.HelperFunction;
//import com.preeti.sansarcart.repository.user.UserRepository;
//import com.preeti.sansarcart.security.service.UserAccountLockService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
// // not needed
//@Component
//@RequiredArgsConstructor
//public class LockedUserValidationFilter extends OncePerRequestFilter {
//
//    private final UserRepository userRepository;
//    private final UserAccountLockService lockTracker;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        if ("/auth/login".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
//            String email = request.getParameter("email");
//
//            if (email != null && lockTracker.isLocked(email)) {
//                HelperFunction.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
//                        "Account is locked. Try again later.");
//                return;
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
