package com.parspecassignment.urlshortner.controller;

import com.parspecassignment.urlshortner.bean.LoginResponseBean;
import com.parspecassignment.urlshortner.dao.UserRepository;
import com.parspecassignment.urlshortner.entity.User;
import com.parspecassignment.urlshortner.security.JwtUtil;
import com.parspecassignment.urlshortner.service.GoogleAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtil jwtUtils;


    @Autowired
    private GoogleAuthService googleAuthService;


    @PostMapping("/google")
    public ResponseEntity<LoginResponseBean> googleLogin(@RequestBody TokenRequest request,HttpServletResponse response) throws Exception {
        GoogleIdToken.Payload payload = googleAuthService.verifyToken(request.getIdToken());

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // Find or create user
        User user = userRepository.findByEmail(email);
             if(user == null){
                    user = User.builder()
                            .email(email)
                            .username(email)
                            .password("password")
                            .picture(picture)
                            .build();
                    userRepository.save(user);
             }

        String token= jwtUtils.generateToken(user.getUsername());
        // Create HttpOnly cookie
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Set to true if using HTTPS
        cookie.setPath("/");    // Cookie available to all endpoints
        cookie.setMaxAge(24 * 60 * 60); // 1 day expiry (in seconds)
        logger.info("User {} signed in successfully via Google", user.getUsername());
        response.addCookie(cookie);
        return new ResponseEntity<>(new LoginResponseBean(token,user.getUsername()), HttpStatus.OK);
    }

    public static class TokenRequest {
        private String idToken;
        public String getIdToken() { return idToken; }
        public void setIdToken(String idToken) { this.idToken = idToken; }
    }


    @PostMapping("/signin")
    public ResponseEntity<LoginResponseBean> authenticateUser(@RequestBody User user, HttpServletResponse response) {
        try{
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            logger.info("User {} signed in successfully", user.getUsername());
            String token= jwtUtils.generateToken(userDetails.getUsername());
            // Create HttpOnly cookie
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // Set to true if using HTTPS
            cookie.setPath("/");    // Cookie available to all endpoints
            cookie.setMaxAge(24 * 60 * 60); // 1 day expiry (in seconds)

            response.addCookie(cookie);
            return new ResponseEntity<>(new LoginResponseBean(token,user.getUsername()), HttpStatus.OK);
        }catch (Exception e){
            logger.error("User Not found:"+e.getMessage());
            return new ResponseEntity<>(new LoginResponseBean(),HttpStatus.UNAUTHORIZED);
        }
    }
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            if (userRepository.existsByUsername(user.getUsername())) {
                logger.error("Username {} already exists", user.getUsername());
                return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
            }
            // Create new user's account
            User newUser = new User(
                    null,
                    user.getUsername(),
                    encoder.encode(user.getPassword())
            );
            userRepository.save(newUser);
            logger.info("User {} registered successfully", user.getUsername());
            return new ResponseEntity<>(user.getUsername(), HttpStatus.CREATED);
        } catch (DataAccessException dae) {
            // Specific Spring exception for DB access errors
            logger.error("Database error occurred while registering user {}: {}", user.getUsername(), dae.getMessage(), dae);
            return new ResponseEntity<>("Database error. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            // Catch-all for unexpected issues
            logger.error("Unexpected error occurred while registering user {}: {}", user.getUsername(), e.getMessage(), e);
            return new ResponseEntity<>("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null); // Set cookie value to null
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // same as when you created it
        cookie.setPath("/");
        cookie.setMaxAge(0); // Delete cookie immediately

        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }
}