package com.parspecassignment.urlshortner.controller;

import com.parspecassignment.urlshortner.bean.LoginResponseBean;
import com.parspecassignment.urlshortner.dao.UserRepository;
import com.parspecassignment.urlshortner.entity.User;
import com.parspecassignment.urlshortner.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtil jwtUtils;

    @Mock
    private HttpServletResponse httpServletResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticateUser_success() {
        User user = new User(null, "testuser", "password");
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(jwtUtils.generateToken("testuser")).thenReturn("jwt-token");

        ResponseEntity<LoginResponseBean> response = authController.authenticateUser(user, httpServletResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void testAuthenticateUser_failure() {
        User user = new User(null, "baduser", "badpassword");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<LoginResponseBean> response = authController.authenticateUser(user, httpServletResponse);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody().getToken());
        assertNull(response.getBody().getUsername());
    }

    @Test
    void testRegisterUser_success() {
        User user = new User(null, "newuser", "password");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User(1L, "newuser", "encodedPassword"));

        ResponseEntity<String> response = authController.registerUser(user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("newuser", response.getBody());
    }

    @Test
    void testRegisterUser_usernameExists() {
        User user = new User(null, "existinguser", "password");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        ResponseEntity<String> response = authController.registerUser(user);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username already exists", response.getBody());
    }

    @Test
    void testRegisterUser_databaseError() {
        User user = new User(null, "dbuser", "password");
        when(userRepository.existsByUsername("dbuser")).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB error"));

        ResponseEntity<String> response = authController.registerUser(user);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Database error. Please try again later.", response.getBody());
    }

    @Test
    void testRegisterUser_unexpectedError() {
        User user = new User(null, "erroruser", "password");
        when(userRepository.existsByUsername("erroruser")).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Unexpected"));

        ResponseEntity<String> response = authController.registerUser(user);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred.", response.getBody());
    }

    @Test
    void testLogout() {
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        ResponseEntity<?> response = authController.logout(httpServletResponse);

        verify(httpServletResponse).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("jwt", cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logged out successfully", response.getBody());
    }
}

