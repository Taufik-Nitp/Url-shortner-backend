package com.parspecassignment.urlshortner.controller;

import java.util.Map;

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

	public boolean isReachable(String url) {
//		return true;

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("HEAD");
			connection.setConnectTimeout(3000);
			return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
		} catch (Exception e) {
			return false;
		}

	}

	@RequestMapping(value = "/longtoshorturl", method = RequestMethod.POST)
	public ResponseEntity<String> getShortUrlFromLongUrl(@RequestBody Map<String, Object> requestBody) {
		String longURL = (String) requestBody.get("url");

		if (isReachable(longURL)) {
			UrlMappingBean responseBean = urlMappingService.addLongURL(longURL);
			return new ResponseEntity<String>("http://localhost:3000/" + responseBean.getShortUrl(),
					HttpStatus.CREATED);
		} else {

			return new ResponseEntity<String>("", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	@RequestMapping(value = "/{slug}", method = RequestMethod.GET)
	public ResponseEntity<UrlMappingBean> getLongUrlfromShortUrl(@PathVariable String slug) {

		UrlMappingBean responseBean = urlMappingService.getLongURLfromShortURL(slug);

		if (responseBean == null) {
			return new ResponseEntity<UrlMappingBean>(responseBean, HttpStatus.NOT_FOUND);
		} else if ("EXPIRED".equals(responseBean.getStatus())) {
			return new ResponseEntity<UrlMappingBean>(responseBean, HttpStatus.OK);
		}
		return new ResponseEntity<UrlMappingBean>(responseBean, HttpStatus.OK);

	}

}
