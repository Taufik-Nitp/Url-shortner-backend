package com.parspecassignment.urlshortner.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.parspecassignment.urlshortner.bean.UrlMappingBean;
import com.parspecassignment.urlshortner.entity.UrlMappingEntity;

public interface UrlMappingService {
                // this will simply add the longURL with new urlId 
	            //  and timestamp as currenttime 
	            // and accesscount as 0 
	            //  and shortURL as blank
	            // status as active
	   UrlMappingBean addLongURL(String longURL);
	   
	   
	             // this will take the  shortURL and search in DB 
	              // if the status is expired then it will return nothing.
	              // if found and timestamp is not expired then it return 
	              // otherwise it will not return nothing.
	   UrlMappingBean getLongURLfromShortURL(String shortURL);
	     
	   
//	   //after adding the long URL we will add the short URL which we have generated using UrlID.
//	   UrlMappingBean addShortUrl(UrlMappingEntity entity);
	   
	    
}
