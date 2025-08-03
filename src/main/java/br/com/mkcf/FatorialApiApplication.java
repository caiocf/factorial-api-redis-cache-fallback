package br.com.mkcf;

import br.com.mkcf.config.redis.SafeCache;
import lombok.extern.log4j.Log4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FatorialApiApplication {

    private static final Logger log = LoggerFactory.getLogger(FatorialApiApplication.class);


    public static void main(String[] args) {
        log.info("===> Antes de aplicar:");
        log.info("networkaddress.cache.ttl = {}", java.security.Security.getProperty("networkaddress.cache.ttl"));
        log.info("networkaddress.cache.negative.ttl = {}", java.security.Security.getProperty("networkaddress.cache.negative.ttl"));

        // Aplicar novos valores
        java.security.Security.setProperty("networkaddress.cache.ttl", "10");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");

        log.info("===> Depois de aplicar:");
        log.info("networkaddress.cache.ttl = {}", java.security.Security.getProperty("networkaddress.cache.ttl"));
        log.info("networkaddress.cache.negative.ttl = {}", java.security.Security.getProperty("networkaddress.cache.negative.ttl"));

        SpringApplication.run(FatorialApiApplication.class, args);
    }

}
