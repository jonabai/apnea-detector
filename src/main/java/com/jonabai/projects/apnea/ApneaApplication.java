package com.jonabai.projects.apnea;

import com.jonabai.projects.apnea.services.ApneaDetectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;

@SpringBootApplication
public class ApneaApplication implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ApneaApplication.class);

    private final ApneaDetectorService apneaDetectorService;

    @Autowired
    public ApneaApplication(ApneaDetectorService apneaDetectorService) {
        this.apneaDetectorService = apneaDetectorService;
    }

    public static void main(String[] args) {
		SpringApplication.run(ApneaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
        if(args.length != 2) {
            logger.error("Number of parameters is not valid.");
            logger.info("Usage:  pauseExtractor inputFile.csv outputFile.csv");
            return;
        }
        logger.info("Starting process at: {}", new Date());
        apneaDetectorService.process(args[0], args[1]);
        logger.info("Process finished at: {}", new Date());
	}
}
