package com.parspecassignment.urlshortner.controller;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.tomcat.util.json.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.parspecassignment.urlshortner.bean.UrlMappingBean;
import com.parspecassignment.urlshortner.service.UrlMappingService;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.InetAddress;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class UrlShortnerController {
	Logger logger = LoggerFactory.getLogger(UrlShortnerController.class);

	@Autowired
	UrlMappingService urlMappingService;

	@GetMapping("/")
	@ResponseBody
	public String getHome() {
		return "String data from home controller.";
	}
  
	
	/// Function to check if the url is reachable or not i.e it is valid url or not by pinging the url.
	// and waiting for 3 sec for HTTP_OK status.
	public boolean isReachable(String url) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("HEAD");
			connection.setConnectTimeout(3000);
			logger.info("Checking if URL {} is reachable",url);
			return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
		} catch (Exception e) {
			logger.info("Exception: " + e.getMessage());
			return false;
		}
	}


    @Operation(
            summary = "Long to Short URL Conversion",
            description = "This API takes the long URL in the request body and returns the corresponding short URL."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
	@RequestMapping(value = "/longtoshorturl", method = RequestMethod.POST)
	public ResponseEntity<String> getShortUrlFromLongUrl(@RequestBody Map<String, Object> requestBody) {
		String longURL = (String) requestBody.get("url");
		// Checking whether the long URL provided is valid or not.
		if (isReachable(longURL)) {
			long startTime = System.currentTimeMillis();

			UrlMappingBean responseBean = urlMappingService.addLongURL(longURL);
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;

			logger.info("Time taken by controller for URL  {}: " ,longURL + duration + " ms");
			return new ResponseEntity<String>("http://localhost:3000/" + responseBean.getShortUrl(),
					HttpStatus.CREATED);
		} else {
			// if not valid then send HTTP status as 406 NOT_ACCEPTABLE.
			return new ResponseEntity<String>("Error: Not reachable to ther url "+ longURL , HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@RequestMapping(value = "/{slug}", method = RequestMethod.GET)
	public ResponseEntity<UrlMappingBean> getLongUrlfromShortUrl(@PathVariable String slug) {

		UrlMappingBean responseBean = urlMappingService.getLongURLfromShortURL(slug);
         
//		 if the bean returned as null it means we have not entry  for this shorturl.
		if (responseBean == null) {
			return new ResponseEntity<UrlMappingBean>(responseBean, HttpStatus.NOT_FOUND);
		} else if ("EXPIRED".equals(responseBean.getStatus())) {
			 // we have entry corresponding to the sortURL but the status got expired.
			return new ResponseEntity<UrlMappingBean>(responseBean, HttpStatus.OK);
		}
		// active short url.
		return new ResponseEntity<UrlMappingBean>(responseBean, HttpStatus.OK);

	}

}
