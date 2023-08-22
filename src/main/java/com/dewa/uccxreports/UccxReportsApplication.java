package com.dewa.uccxreports;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.userdetails.cache.EhCacheBasedUserCache;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableCaching(mode = AdviceMode.ASPECTJ)
public class UccxReportsApplication {

    @Bean
    ModelMapper modelMapper(){
		return new ModelMapper();
	}
	public static void main(String[] args) {
		SpringApplication.run(UccxReportsApplication.class, args);
	}

    @Bean
    EhCacheManagerFactoryBean ehCacheManagerFactory() {
		EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
		cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
		cacheManagerFactoryBean.setShared(true);
		return cacheManagerFactoryBean;
	}

    @Bean
    EhCacheCacheManager ehCacheCacheManager() {
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();
		cacheManager.setCacheManager(ehCacheManagerFactory().getObject());
		cacheManager.setTransactionAware(true);
		return cacheManager;
	}

    @Bean
    EhCacheBasedUserCache userCache() throws Exception {
		EhCacheBasedUserCache userCache = new EhCacheBasedUserCache();
		userCache.setCache(ehCacheManagerFactory().getObject().getCache("userCache"));
		return userCache;
	}
    
    @Bean
    public RestTemplate restTemplate() {
    	return new RestTemplate();
    }

}
