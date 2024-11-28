package com.parspecassignment.urlshortner.entity;

import org.springframework.stereotype.Component;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


@Component
@Entity
public class UrlMappingEntity {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long urlId;
    private String  shortUrl;
    private String longUrl;
    private long timestamp;
    private int accessCount;
    private String status;
	public long getUrlId() {
		return urlId;
	}
	public void setUrlId(long urlId) {
		this.urlId = urlId;
	}
	public String getShortUrl() {
		return shortUrl;
	}
	public void setShortUrl(String shortUrl) {
		this.shortUrl = shortUrl;
	}
	public String getLongUrl() {
		return longUrl;
	}
	public void setLongUrl(String longUrl) {
		this.longUrl = longUrl;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getAccessCount() {
		return accessCount;
	}
	public void setAccessCount(int accessCount) {
		this.accessCount = accessCount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "UrlMappingEntity [urlId=" + urlId + ", shortUrl=" + shortUrl + ", longUrl=" + longUrl + ", timestamp="
				+ timestamp + ", accessCount=" + accessCount + ", status=" + status + "]";
	}
	
    
    
}
