package com.parspecassignment.urlshortner.redis;

import com.parspecassignment.urlshortner.bean.UrlMappingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisUrlService {


    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUrlService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveLongUrlToRedis(String longUrl, UrlMappingBean bean) {
         redisTemplate.opsForValue().set("LongURL:"+longUrl, bean, Duration.ofMinutes(2));
    }

    public UrlMappingBean getUrlMappingBeanFromLongUrlFromRedis(String longUrl) {

        Object obj= redisTemplate.opsForValue().get("LongURL:"+longUrl);
        if(obj instanceof UrlMappingBean) {
            return (UrlMappingBean) obj;
        }else{
            return null;
        }
    }

    public void saveShortUrlToRedis(String shortUrl, UrlMappingBean bean) {
        redisTemplate.opsForValue().set("ShortURL: "+shortUrl, bean, Duration.ofMinutes(2)); // TTL = 24 hrs
    }

    public UrlMappingBean getUrlMappingBeanFromShortURLFromRedis(String shortUrl) {
        Object obj = redisTemplate.opsForValue().get("ShortURL: "+shortUrl);
        if (obj instanceof UrlMappingBean) {
             return (UrlMappingBean) obj;
        }
         return null;
    }

}
