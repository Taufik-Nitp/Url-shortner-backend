package com.parspecassignment.urlshortner.service;

import java.util.Optional;

import com.parspecassignment.urlshortner.redis.RedisUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parspecassignment.urlshortner.bean.UrlMappingBean;
import com.parspecassignment.urlshortner.dao.UrlMappingDAO;
import com.parspecassignment.urlshortner.entity.UrlMappingEntity;

import jakarta.transaction.Transactional;

@Service
public class UrlMappingServiceIMPL implements UrlMappingService {

	private final String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final Logger logger = LoggerFactory.getLogger(UrlMappingServiceIMPL.class);
	private static final int EXPIRY_DURATION= 2;  // In minutes

	@Autowired
	UrlMappingEntity urlMappingEntity;

	@Autowired
	UrlMappingDAO urlMappingDAO;

	@Autowired
	RedisUrlService redisUrlService;


	@Override
	public UrlMappingBean addLongURL(String longURL) {
		// TODO Auto-generated method stub

	      UrlMappingBean umb=  redisUrlService.getUrlMappingBeanFromLongUrlFromRedis(longURL);

		if(umb!=null) {
			 logger.info("Long URL from Redis: "+ umb);
//			redisTemplate.expire(redisKey, Duration.ofHours(24));
			return umb;
		}

		UrlMappingBean urlMappingBean = new UrlMappingBean();

		Optional<UrlMappingEntity> existingEntity = urlMappingDAO.findByLongUrl(longURL);
         /// Here I am checking whether the entry exist or not in DB
		// If the long URL exists, return the existing mapping 
		if (existingEntity.isPresent()) {
			UrlMappingEntity urlMappingEntity = existingEntity.get();
			//set timestamp of this url as current timestamp.
			urlMappingEntity.setTimestamp(System.currentTimeMillis());
			// and make the status active
			urlMappingEntity.setStatus("ACTIVE");
			
			urlMappingDAO.save(urlMappingEntity);
			BeanUtils.copyProperties(urlMappingEntity, urlMappingBean);
			logger.info("Saving in Redis,key as longURL {} with value as Bean: {} " ,longURL,urlMappingBean);
			redisUrlService.saveLongUrlToRedis(longURL, urlMappingBean);
			logger.info("Saving in Redis key as shortURL {} with value as Bean: {} " ,urlMappingEntity.getShortUrl(),urlMappingBean);
			redisUrlService.saveShortUrlToRedis(urlMappingEntity.getShortUrl(), urlMappingBean);
			return urlMappingBean;
		}

		// if the longURL doesn't exist.
		UrlMappingEntity urlMappingEntity = new UrlMappingEntity();
		urlMappingEntity.setLongUrl(longURL);
		urlMappingEntity.setStatus("ACTIVE");
		urlMappingEntity.setAccessCount(0);
		urlMappingEntity.setTimestamp(System.currentTimeMillis());

		urlMappingDAO.save(urlMappingEntity);
		//after adding the long URL we will add the short URL which we have generated using UrlID of current entry insertion above.
		String shortURL = getShortURL(urlMappingEntity.getUrlId());
         // Now updating the short URL in the DB.
		urlMappingEntity.setShortUrl(shortURL);
		urlMappingDAO.saveAndFlush(urlMappingEntity);
		BeanUtils.copyProperties(urlMappingEntity, urlMappingBean);
		logger.info("Saving in Redis,key as longURL {} with value as Bean: {} " ,longURL,urlMappingBean);
		redisUrlService.saveLongUrlToRedis(longURL, urlMappingBean);
		logger.info("Saving in Redis key as shortURL {} with value as Bean: {} " ,shortURL,urlMappingBean);
		redisUrlService.saveShortUrlToRedis(shortURL, urlMappingBean);

		return urlMappingBean;
	}

	@Override
	public UrlMappingBean getLongURLfromShortURL(String shortURL) {
		// TODO Auto-generated method stub
		 UrlMappingBean umb =  redisUrlService.getUrlMappingBeanFromShortURLFromRedis(shortURL);
		 if(umb!=null) {
			 logger.info("Long URL from Redis: "+ umb);
			 return umb;
		 }
		UrlMappingBean urlMappingBean = new UrlMappingBean();
		Optional<UrlMappingEntity> existingEntity = urlMappingDAO.findByShortUrl(shortURL);
		 //Checking if the short url exist in db if it doesnt exist return null object
		 // if exist then return the row as urlMappingBean.
		if (existingEntity.isPresent()) {
			// If the short URL exists, return the existing mapping
			UrlMappingEntity urlMappingEntity = existingEntity.get();
			// Checking the url is expired or not .
			
			if ((System.currentTimeMillis() - urlMappingEntity.getTimestamp()) > EXPIRY_DURATION*60*1000) {
				urlMappingEntity.setStatus("EXPIRED");
				logger.info("URL {} got expired " ,urlMappingEntity.getLongUrl());
				urlMappingDAO.save(urlMappingEntity);
			}
			else {
				// if it not expired it accessed one more time so I increased the access count and persist the DB.
				urlMappingEntity.setAccessCount(urlMappingEntity.getAccessCount() + 1);
				urlMappingDAO.save(urlMappingEntity);
			}
			BeanUtils.copyProperties(urlMappingEntity, urlMappingBean);
			return urlMappingBean;
		}
		return null;
	}

	
	 /////// Function which  convert the  short URL using URL_Id which is generated when the new entry is made into DB
	public String getShortURL(long urlId) {

		String shortUrl = "";
		while (urlId != 0) {
			long rem = urlId % 62;
			int ind = (int) rem;
			shortUrl = shortUrl + str.charAt(ind);
			urlId = urlId / 62;
		}
		while (shortUrl.length() < 7) {
			shortUrl = shortUrl + "*"; /////// added the padding to make the short URL size equal to 7.
		}
		return shortUrl;
	}

}
