// package com.kitchensink.service.impl;
//
// import java.io.IOException;
//
// import org.springframework.http.HttpStatus;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.web.authentication.AuthenticationFailureHandler;
// import org.springframework.stereotype.Component;
//
// import com.kitchensink.service.LoginService;
//
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
//
// @Component
// public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
//
// private final LoginService loginService;
//
// public CustomAuthenticationFailureHandler(LoginService loginService) {
// this.loginService = loginService;
// }
//
// @Override
// public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
// AuthenticationException exception) throws IOException, ServletException {
// String email = request.getParameter("username");
// loginService.processFailedLogin(email);
// response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials");
// }
//
// }
