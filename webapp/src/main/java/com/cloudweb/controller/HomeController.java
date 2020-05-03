package com.cloudweb.controller;

import com.timgroup.statsd.StatsDClient;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;

@RestController
public class HomeController {

    @Autowired
    private StatsDClient stats;

    @Autowired
    Environment env;

    private final static Logger logger = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping("/")
    public String home() throws ParseException {
        stats.incrementCounter("endpoint.getHomeUrl.http.get");
        long startTime = System.currentTimeMillis();
        logger.info(env.getProperty("db.url"));
        logger.info(env.getProperty("bucket.name"));
        logger.info("home controller");
        logger.warn("warn check");
        return "VGR MS US";
    }
}
