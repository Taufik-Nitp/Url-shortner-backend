package com.parspecassignment.urlshortner.service;

import java.util.Optional;

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
	@Autowired
	UrlMappingEntity urlMappingEntity;

	@Autowired
	UrlMappingDAO urlMappingDAO;

	@Override
	public UrlMappingBean addLongURL(String longURL) {
		// TODO Auto-generated method stub
		UrlMappingBean urlMappingBean = new UrlMappingBean();

		Optional<UrlMappingEntity> existingEntity = urlMappingDAO.findByLongUrl(longURL);
         /// Here I am checking whether the entry exist or not in DB
		if (existingEntity.isPresent()) {
			// If the long URL exists, return the existing mapping
			UrlMappingEntity urlMappingEntity = existingEntity.get();
			urlMappingEntity.setTimestamp(System.currentTimeMillis());
			urlMappingEntity.setStatus("ACTIVE");
			urlMappingDAO.save(urlMappingEntity);
			BeanUtils.copyProperties(urlMappingEntity, urlMappingBean);
			return urlMappingBean;
		}

		UrlMappingEntity urlMappingEntity = new UrlMappingEntity();
		urlMappingEntity.setLongUrl(longURL);
		urlMappingEntity.setStatus("ACTIVE");
		urlMappingEntity.setAccessCount(0);
		urlMappingEntity.setTimestamp(System.currentTimeMillis());

		urlMappingDAO.save(urlMappingEntity);

		String shortURL = getShortURL(urlMappingEntity.getUrlId());

		logger.info("short url=======>>>>>>>>>>" + shortURL);

		urlMappingEntity.setShortUrl(shortURL);
		urlMappingDAO.saveAndFlush(urlMappingEntity);
		BeanUtils.copyProperties(urlMappingEntity, urlMappingBean);
		return urlMappingBean;
	}

	@Override
	public UrlMappingBean getLongURLfromShortURL(String shortURL) {
		// TODO Auto-generated method stub

		UrlMappingBean urlMappingBean = new UrlMappingBean();

		Optional<UrlMappingEntity> existingEntity = urlMappingDAO.findByShortUrl(shortURL);

		if (existingEntity.isPresent()) {
			// If the short URL exists, return the existing mapping
			UrlMappingEntity urlMappingEntity = existingEntity.get();

			if ((System.currentTimeMillis() - urlMappingEntity.getTimestamp()) > 60000) {
				urlMappingEntity.setStatus("EXPIRED");
				logger.info("Expired:==========>>>>>>>>>>>>>>>>>>>>>>>>>" + urlMappingEntity.getLongUrl());
				urlMappingDAO.save(urlMappingEntity);
			}

			else {
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
