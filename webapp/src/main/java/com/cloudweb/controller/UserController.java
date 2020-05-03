package com.cloudweb.controller;

import com.cloudweb.entity.User;
import com.cloudweb.repository.UserRepositry;
import com.google.gson.Gson;

import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.util.Strings;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController

public class UserController {
    @Autowired
    UserRepositry userRepositry;

    @Autowired(required = false)
    private StatsDClient stats;

    @Autowired
    Environment env;

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);
    private static String regex = "^(.+)@(.+)$";
    private static String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
    Pattern pattern = Pattern.compile(regex);
    Pattern passwordPattern = Pattern.compile(PASSWORD_REGEX);

    @PostMapping(path = "/v1/user", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> addUser(@RequestBody User user) {
        if (env.getActiveProfiles().equals("aws"))
            stats.incrementCounter("endpoint.createUser.http.post");
            long startTime = System.currentTimeMillis();

        if (!Strings.isBlank(user.getEmailAddress()) && !Strings.isBlank(user.getFirst_Name()) && !Strings.isBlank(user.getLast_Name()) && !Strings.isBlank(user.getPassword())) {
            if (userRepositry.findByEmailAddress(user.getEmailAddress()) != null) {
                logger.warn("Email already exists in the data base",logger.getClass());
                stats.recordExecutionTime("createUserLatency", System.currentTimeMillis() - startTime);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");

            }
            user.setAccount_created(new Date().toString());
            user.setAccount_updated(new Date().toString());
            Matcher matcher = pattern.matcher(user.getEmailAddress());
            if (!matcher.matches()) {
                stats.recordExecutionTime("createUserLatency", System.currentTimeMillis() - startTime);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please enter the valid email ");
            }
            String originalPassword = user.getPassword();
            Matcher passwordMatcher = passwordPattern.matcher(originalPassword);
            if (!passwordMatcher.matches()) {
                stats.recordExecutionTime("createUserLatency", System.currentTimeMillis() - startTime);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("please keep strong password ");
            }
            String generatedSecuredPasswordHash = BCrypt.hashpw(originalPassword, BCrypt.gensalt(12));
            user.setPassword(generatedSecuredPasswordHash);
            boolean matched = BCrypt.checkpw(originalPassword, generatedSecuredPasswordHash);
            userRepositry.save(user);
            User storedUser = userRepositry.findByEmailAddress(user.getEmailAddress());
            JSONObject json = new JSONObject(storedUser);
            json.remove("password");
            stats.recordExecutionTime("createUserLatency", System.currentTimeMillis() - startTime);
            logger.info("user created",logger.getClass());
            return ResponseEntity.status(HttpStatus.CREATED).body(json.toString());
        } else {
            stats.recordExecutionTime("createUserLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not created ");
        }
    }

    @GetMapping(path = "/v1/user/self", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getUser(@RequestHeader HttpHeaders headers) {
        if (env.getActiveProfiles().equals("aws"))
            stats.incrementCounter("endpoint.getUser.http.post");
        long startTime = System.currentTimeMillis();

        byte[] actualByte = Base64.getDecoder().decode(headers.getFirst("authorization").substring(6));
        String decodedToken = new String(actualByte);
        String[] credentials = decodedToken.split(":");
        User user = userRepositry.findByEmailAddress(credentials[0]);
        if(user==null)
        {
            logger.warn("User not found ",logger.getClass());
            stats.recordExecutionTime("getUserLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }
        if (BCrypt.checkpw(credentials[1], user.getPassword())) {
            User getUser = new User();
            getUser.setId(user.getId());
            getUser.setFirst_Name(user.getFirst_Name());
            getUser.setLast_Name(user.getLast_Name());
            getUser.setEmailAddress(user.getEmailAddress());
            getUser.setAccount_created(user.getAccount_created());
            getUser.setAccount_updated(user.getAccount_updated());
            Gson gson = new Gson();
            String json = gson.toJson(getUser);
            if(env.getActiveProfiles().equals("aws"))
            stats.recordExecutionTime("getUserLatency", System.currentTimeMillis() - startTime);
            logger.info("user is returned ",logger.getClass());
            return  ResponseEntity.status(HttpStatus.OK).body(json);
        }
        stats.recordExecutionTime("getUserLatency", System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Credentials are wrong");

    }

    @PutMapping(path = "/v1/user/self" ,consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> updateUser(@RequestHeader HttpHeaders headers, @RequestBody User user) {
        if (env.getActiveProfiles().equals("aws"))
            stats.incrementCounter("endpoint.updateUser.http.put");
        long startTime = System.currentTimeMillis();
        byte[] actualByte = Base64.getDecoder().decode(headers.getFirst("authorization").substring(6));
        String decodedToken = new String(actualByte);
        String[] credentials = decodedToken.split(":");
        User userDB = userRepositry.findByEmailAddress(credentials[0]);
        if(userDB==null)
        {
            logger.warn("user doesn't exists in the database",logger.getClass());
            stats.recordExecutionTime("updateUserLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not registered ");
        }
        if (user.getAccount_created() != null || user.getAccount_updated() != null || user.getId() != null) {
            stats.recordExecutionTime("updateUserLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("please enter valid Data");
        }
        if(user.getEmailAddress()==null || !user.getEmailAddress().equals(credentials[0])){
            stats.recordExecutionTime("updateUserLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("please send the valid email ");
        }

        if (BCrypt.checkpw(credentials[1], userDB.getPassword())) {
            if (user.getFirst_Name() != null && user.getLast_Name() != null && user.getPassword() != null) {
                    userDB.setFirst_Name(user.getFirst_Name());
                    userDB.setLast_Name(user.getLast_Name());
                    Matcher passwordMatcher = passwordPattern.matcher(user.getPassword());
                    if (!passwordMatcher.matches()) {
                        stats.recordExecutionTime("updateUserLatency", System.currentTimeMillis() - startTime);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("please keep strong password ");
                    }
                    userDB.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));
                    userDB.setAccount_updated(new Date().toString());
                userRepositry.save(userDB);
                stats.recordExecutionTime("updateUserLatency", System.currentTimeMillis() - startTime);
                logger.info("user updated",logger.getClass());
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(" User object Updated succesfully");
            } else {
                stats.recordExecutionTime("updateUserLatency", System.currentTimeMillis() - startTime);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User Data is not updated ");
            }
        }
        logger.info("user not updated",logger.getClass());
        stats.recordExecutionTime("updateUserLatency", System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User Data is not updated ");
    }

//    private String[] getuserNameandPassword(String ){
//
//    }


}


