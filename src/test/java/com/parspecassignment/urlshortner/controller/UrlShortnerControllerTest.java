package com.parspecassignment.urlshortner.controller;

import com.parspecassignment.urlshortner.bean.UrlMappingBean;
import com.parspecassignment.urlshortner.service.UrlMappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UrlShortnerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UrlMappingService urlMappingService;

    @InjectMocks
    private UrlShortnerController urlShortnerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(urlShortnerController).build();
    }

    @Test
    void testGetHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("String data from home controller."));
    }

    @Test
    void testGetShortUrlFromLongUrl_validUrl() throws Exception {
        // Arrange
        UrlMappingBean bean = new UrlMappingBean();
        bean.setShortUrl("abc123");
        when(urlMappingService.addLongURL(anyString())).thenReturn(bean);

        // Mock controller’s isReachable() method to always return true
        UrlShortnerController spyController = new UrlShortnerController() {
            @Override
            public boolean isReachable(String url) {
                return true;
            }
        };
        spyController.urlMappingService = urlMappingService;
        mockMvc = MockMvcBuilders.standaloneSetup(spyController).build();

        // Act & Assert
        String json = "{\"url\":\"http://example.com\"}";

        mockMvc.perform(post("/longtoshorturl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("http://localhost:3000/abc123"));
    }

    @Test
    void testGetShortUrlFromLongUrl_invalidUrl() throws Exception {
        // Mock controller’s isReachable() to return false
        UrlShortnerController spyController = new UrlShortnerController() {
            @Override
            public boolean isReachable(String url) {
                return false;
            }
        };
        spyController.urlMappingService = urlMappingService;
        mockMvc = MockMvcBuilders.standaloneSetup(spyController).build();

        String json = "{\"url\":\"http://invalid-url.com\"}";

        mockMvc.perform(post("/longtoshorturl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().string("Error: Not reachable to ther url http://invalid-url.com"));
    }

    @Test
    void testGetLongUrlFromShortUrl_foundAndActive() throws Exception {
        UrlMappingBean bean = new UrlMappingBean();
        bean.setLongUrl("http://example.com");
        bean.setStatus("ACTIVE");

        when(urlMappingService.getLongURLfromShortURL("abc123")).thenReturn(bean);

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longUrl").value("http://example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testGetLongUrlFromShortUrl_notFound() throws Exception {
        when(urlMappingService.getLongURLfromShortURL("xyz")).thenReturn(null);

        mockMvc.perform(get("/xyz"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetLongUrlFromShortUrl_expired() throws Exception {
        UrlMappingBean bean = new UrlMappingBean();
        bean.setLongUrl("http://expired.com");
        bean.setStatus("EXPIRED");

        when(urlMappingService.getLongURLfromShortURL("expired123")).thenReturn(bean);

        mockMvc.perform(get("/expired123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"));
    }
}