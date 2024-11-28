package com.parspecassignment.urlshortner.bean;

public class UrlMappingBean {

	private long urlId;
	private String shortUrl;
	private String longUrl;
	private long timestamp;
	private int accessCount;
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

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

	@Override
	public String toString() {
		return "UrlMappingBean [urlId=" + urlId + ", shortUrl=" + shortUrl + ", longUrl=" + longUrl + ", timestamp="
				+ timestamp + ", accessCount=" + accessCount + ", status=" + status + "]";
	}

}
