package com.parspecassignment.urlshortner.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.parspecassignment.urlshortner.entity.UrlMappingEntity;

import jakarta.transaction.Transactional;


@Transactional
public interface UrlMappingDAO extends JpaRepository<UrlMappingEntity,Integer>{

	Optional<UrlMappingEntity> findByLongUrl(String longURL);
	
	Optional<UrlMappingEntity> findByShortUrl(String shortURL);

}
