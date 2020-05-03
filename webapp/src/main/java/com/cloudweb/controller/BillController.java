package com.cloudweb.controller;


import backgroundThread.BackgroundThread;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.cloudweb.awsmetric.SQSConfig;
import com.cloudweb.entity.Bill;
import com.cloudweb.entity.BillSQSPojo;
import com.cloudweb.entity.User;
import com.cloudweb.repository.BillRepositry;
import com.cloudweb.repository.FileRepositry;
import com.cloudweb.repository.UserRepositry;
import com.google.gson.Gson;
import com.timgroup.statsd.StatsDClient;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class BillController {
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    BillRepositry billRepositry;
    @Autowired
    FileRepositry fileRepositry;
    @Autowired
    UserRepositry userRepositry;
    @Autowired
    Environment environment;
    @Autowired(required = false)
    FileController fileController;
    @Autowired(required = false)
    FileS3Controller fileS3Controller;
    @Autowired(required = false)
    private StatsDClient stats;

    @Autowired(required = false)
    SQSConfig sqs;
    //static String queueUrl;

    BackgroundThread backgroundThread;
    static boolean start = false;


    @Autowired
    Environment env;
    private final static Logger logger = LoggerFactory.getLogger(HomeController.class);


    @PostMapping(path = "/v1/bill", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> addBill(@RequestHeader HttpHeaders headers, @RequestBody Bill bill) {
        if (env.getActiveProfiles().equals("aws"))
            stats.incrementCounter("endpoint.postBill.http.post");
        long startTime = System.currentTimeMillis();

        if (Strings.isBlank(bill.getVendor()) || Strings.isBlank(bill.getBill_date()) || Strings.isBlank(bill.getDue_date()) ||
                bill.getAmount_due() < 0.01 || bill.getCategories() == null || bill.getCategories().size() < 1 || bill.getPaymentStatus() == null) {
            stats.recordExecutionTime("postBillLatency", System.currentTimeMillis() - startTime);
            logger.warn("required fields are missing", logger.getClass());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("please enter all the required fields");

        }

        try {
            formatter.parse(bill.getBill_date());
            formatter.parse(bill.getDue_date());
        } catch (ParseException e) {
            stats.recordExecutionTime("postBillLatency", System.currentTimeMillis() - startTime);
            logger.warn("date format is wrong entered", logger.getClass());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Date format is wrong");
            //If input date is in different format or invalid.
        }

        //dd/MM/yyyy
        byte[] actualByte = Base64.getDecoder().decode(headers.getFirst("authorization").substring(6));
        String decodedToken = new String(actualByte);
        String[] credentials = decodedToken.split(":");
        User user = userRepositry.findByEmailAddress(credentials[0]);
        if (user == null) {
            logger.warn("user doesn't exist in the database", logger.getClass());
            stats.recordExecutionTime("postBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User doesn't exists in the data base");
        }
        if (BCrypt.checkpw(credentials[1], user.getPassword())) {
            Bill billDb = new Bill();
            billDb.setOwnerId(user.getId());
            billDb.setVendor(bill.getVendor());
            billDb.setBill_date(bill.getBill_date());
            billDb.setDue_date(bill.getDue_date());
            billDb.setAmount_due(bill.getAmount_due());
            Set<String> set = new HashSet<>();
            for (String s : bill.getCategories()) {
                if (!set.add(s.trim())) {
                    logger.warn("unique categories are not entered", logger.getClass());
                    stats.recordExecutionTime("postBillLatency", System.currentTimeMillis() - startTime);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please add unique categories");
                }
            }
            billDb.setCategories(bill.getCategories());
            billDb.setPaymentStatus(bill.getPaymentStatus());
            billDb.setCreated_ts(new Date().toString());
            billDb.setUpdated_ts(new Date().toString());
            billRepositry.save(billDb);
            JSONObject jsonObject = new JSONObject(billDb);
            jsonObject.accumulate("attachment", new JSONObject());
            stats.recordExecutionTime("postBillLatency", System.currentTimeMillis() - startTime);
            logger.info("Bill created succesfully", logger.getClass());
            return ResponseEntity.status(HttpStatus.CREATED).body(jsonObject.toString());
        } else {
            logger.warn("unauthorized user tried to access", logger.getClass());
            stats.recordExecutionTime("postBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credentials are wrong");
        }

    }

    @GetMapping(path = "/v1/bills", produces = "application/json")
    public ResponseEntity<String> getBills(@RequestHeader HttpHeaders headers) {
        if (env.getActiveProfiles().equals("aws"))
            stats.incrementCounter("endpoint.getBills.http.get");
        long startTime = System.currentTimeMillis();
        byte[] actualByte = Base64.getDecoder().decode(headers.getFirst("authorization").substring(6));
        String decodedToken = new String(actualByte);
        String[] credentials = decodedToken.split(":");
        User user = userRepositry.findByEmailAddress(credentials[0]);
        if (user == null) {
            logger.warn("user doesn't exist in the database", logger.getClass());
            stats.recordExecutionTime("getBillsLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unauthorized user request failed");
        }

        JSONArray addbilljsonArray = new JSONArray();
        if (BCrypt.checkpw(credentials[1], user.getPassword())) {
            List<Bill> bill = billRepositry.findByOwnerId(user.getId());

            JSONArray billtojsonArray = new JSONArray(bill);

            for (int i = 0; i < bill.size(); i++) {
                JSONObject getJSONObject = billtojsonArray.getJSONObject(i);
                if (!getJSONObject.has("attachment")) {
                    getJSONObject.accumulate("attachment", new JSONObject());
                }
                addbilljsonArray.put(getJSONObject);
            }
            logger.info("bills returned", logger.getClass());
            stats.recordExecutionTime("getBillsLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.OK).body(addbilljsonArray.toString());
        } else {
            logger.warn("unauthorized user tried to access", logger.getClass());
            stats.recordExecutionTime("getBillsLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credentials are wrong");

        }

    }

    @DeleteMapping(path = "/v1/bill/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> deleteBillByID(@RequestHeader HttpHeaders headers, @PathVariable(value = "id") UUID billid) throws IOException {
        if (env.getActiveProfiles().equals("aws"))
            stats.incrementCounter("endpoint.deleteBill.http.delete");
        long startTime = System.currentTimeMillis();
        byte[] actualByte = Base64.getDecoder().decode(headers.getFirst("authorization").substring(6));
        String decodedToken = new String(actualByte);
        String[] credentials = decodedToken.split(":");
        User user = userRepositry.findByEmailAddress(credentials[0]);
        if (user == null) {
            stats.recordExecutionTime("deleteBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unauthorized user request failed");
        }

        if (!BCrypt.checkpw(credentials[1], user.getPassword())) {
            logger.warn("wrong password entered", logger.getClass());
            stats.recordExecutionTime("deleteBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unauthorized user request failed");

        }
        Bill billdb = billRepositry.findById(billid);
        if (billdb == null) {
            stats.recordExecutionTime("deleteBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("bill with the id is not present");
        }
        if (!billdb.getOwnerId().equals(user.getId())) {
            stats.recordExecutionTime("deleteBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user request failed");
        }

        String[] env = environment.getActiveProfiles();
        List<String> list = Arrays.asList(env);
        if (list.contains("local") && fileRepositry.findByBillId(billid) != null) {

            fileController.deleteFile(headers, billid, billdb.getAttachment().getId());
            Long file_del = billRepositry.deleteBillById(billid);
        }
        if (list.contains("aws") && fileRepositry.findByBillId(billid) != null) {

            fileS3Controller.deleteFile(headers, billid, billdb.getAttachment().getId());
            Long file_del = billRepositry.deleteBillById(billid);
        }

        Long l = billRepositry.deleteBillById(billid);
        if (l == 1) {
            stats.recordExecutionTime("deleteBillLatency", System.currentTimeMillis() - startTime);
            logger.info("bill deleted succssefully", logger.getClass());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Deleted the user");

        }
        stats.recordExecutionTime("deleteBillLatency", System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Deleted User");

    }


    @GetMapping(path = "/v1/bill/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getBillByID(@RequestHeader HttpHeaders headers, @PathVariable(value = "id") UUID billid) {
        if (env.getActiveProfiles().equals("aws"))
            stats.incrementCounter("endpoint.getBill.http.get");
        long startTime = System.currentTimeMillis();
        byte[] actualByte = Base64.getDecoder().decode(headers.getFirst("authorization").substring(6));
        String decodedToken = new String(actualByte);
        String[] credentials = decodedToken.split(":");
        User user = userRepositry.findByEmailAddress(credentials[0]);
        if (user == null) {
            logger.warn("user doesn't exists in the database", logger.getClass());
            stats.recordExecutionTime("getBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unauthorized user request failed");
        }

        if (!BCrypt.checkpw(credentials[1], user.getPassword())) {
            stats.recordExecutionTime("getBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unauthorized user request failed");

        }

        Bill billdb = billRepositry.findById(billid);
        if (billdb == null) {
            stats.recordExecutionTime("getBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("bill with the id is not present");
        }
        if (!billdb.getOwnerId().equals(user.getId())) {
            stats.recordExecutionTime("getBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user request failed");
        }


        JSONObject jsonObject = new JSONObject(billdb);
        if (!jsonObject.has("attachment")) {
            jsonObject.accumulate("attachment", new JSONObject());
        }
        logger.info("bill returned", logger.getClass());
        stats.recordExecutionTime("getBillLatency", System.currentTimeMillis() - startTime);
        return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());


    }


    @PutMapping(path = "/v1/bill/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> updateBillbyId(@RequestHeader HttpHeaders headers, @PathVariable(value = "id") UUID billid,
                                                 @RequestBody Bill bill) {
        if (env.getActiveProfiles().equals("aws"))
            stats.incrementCounter("endpoint.updateBill.http.put");
        long startTime = System.currentTimeMillis();
        if (Strings.isBlank(bill.getVendor()) || Strings.isBlank(bill.getBill_date()) ||
                Strings.isBlank(bill.getDue_date()) || bill.getCategories() == null || bill.getCategories().size() < 1 || bill.getPaymentStatus() == null) {
            stats.recordExecutionTime("updateBillLatency", System.currentTimeMillis() - startTime);
            logger.warn("Not all the required fields entered", logger.getClass());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please return all the fields");
        }


        byte[] actualByte = Base64.getDecoder().decode(headers.getFirst("authorization").substring(6));
        String decodedToken = new String(actualByte);
        String[] credentials = decodedToken.split(":");
        User user = userRepositry.findByEmailAddress(credentials[0]);
        if (user == null) {
            stats.recordExecutionTime("updateBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unauthorized user request failed");
        }

        if (!BCrypt.checkpw(credentials[1], user.getPassword())) {
            stats.recordExecutionTime("updateBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unauthorized user request failed");

        }

        Bill billD = billRepositry.findById(billid);

        if (billD == null) {
            stats.recordExecutionTime("updateBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("bill with the id is not present");
        }
        if (!billD.getOwnerId().equals(user.getId())) {
            stats.recordExecutionTime("updateBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user request failed");
        }

        try {
            formatter.parse(bill.getBill_date());
            formatter.parse(bill.getDue_date());
        } catch (ParseException e) {
            stats.recordExecutionTime("updateBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Date format is wrong");
            //If input date is in different format or invalid.
        }

        billD.setVendor(bill.getVendor());
        billD.setBill_date(bill.getBill_date());
        billD.setDue_date(bill.getDue_date());
        Set<String> set = new HashSet<>();
        for (String s : bill.getCategories()) {
            if (!set.add(s)) {
                stats.recordExecutionTime("updateBillLatency", System.currentTimeMillis() - startTime);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Categories should be unique");
            }
        }

        billD.setCategories(bill.getCategories());
        billD.setPaymentStatus(bill.getPaymentStatus());
        billD.setUpdated_ts(new Date().toString());
        if (bill.getAmount_due() > 0.01)
            billD.setAmount_due(bill.getAmount_due());
        else {
            stats.recordExecutionTime("updateBillLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The amount should be greater than 0.01");
        }
        billRepositry.save(billD);
        stats.recordExecutionTime("updateBillLatency", System.currentTimeMillis() - startTime);
        logger.info("bill updated succesfully", logger.getClass());
        return ResponseEntity.status(HttpStatus.OK).body("User updated succesfully");

    }

    @GetMapping(path = "/v1/bills/due/{x}", produces = "application/json")
    public ResponseEntity<String> getBills(@RequestHeader HttpHeaders headers, @PathVariable(value = "x") int x) {

        if (env.getActiveProfiles().equals("aws"))
            stats.incrementCounter("endpoint.getBills.due.http.get");

        long startTime = System.currentTimeMillis();
        byte[] actualByte = Base64.getDecoder().decode(headers.getFirst("authorization").substring(6));
        String decodedToken = new String(actualByte);
        String[] credentials = decodedToken.split(":");
        User user = userRepositry.findByEmailAddress(credentials[0]);
        if (user == null) {
            logger.warn("user doesn't exist in the database", logger.getClass());
            stats.recordExecutionTime("getBillsLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unauthorized user request failed");
        }
        logger.info("Bill sucess api");
        try {
            if (x < 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please give valid x");
            int a = (int) x;
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please give valid x");
        }
        if (BCrypt.checkpw(credentials[1], user.getPassword())) {
            List<Bill> bill = billRepositry.findByOwnerId(user.getId());
            List<Bill> bills = new ArrayList<>();
            List<UUID> listofduebills = new ArrayList<>();
            for (int i = 0; i < bill.size(); i++) {
                String billDueDate = bill.get(i).getDue_date();
                Date todayDate = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String d1 = formatter.format(todayDate);
                DateTime dt1 = new DateTime(d1);
                DateTime dt2 = new DateTime(billDueDate);
                Days d = Days.daysBetween(dt1, dt2);
                int duetime = d.getDays();
                System.out.println(duetime);

                if (duetime <= x && duetime >= 0) {
                    bills.add(bill.get(i));
                    listofduebills.add(bill.get(i).getId());
                }
            }
            BillSQSPojo billSQSPojo = new BillSQSPojo();
            billSQSPojo.setEmail(user.getEmailAddress());
            billSQSPojo.setBillList(listofduebills);
            emailQueueBills(billSQSPojo);
            logger.info("bills due returned", logger.getClass());
            stats.recordExecutionTime("getBillsLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.OK).body("Email will be sent shortly");
        } else {
            logger.warn("unauthorized user tried to access", logger.getClass());
            stats.recordExecutionTime("getBillsLatency", System.currentTimeMillis() - startTime);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credentials are wrong");
        }
    }
    public ResponseEntity<String> emailQueueBills(BillSQSPojo billSQSPojo) {
        logger.info("sqs started  ");
        logger.warn(sqs + "object");
        String queueUrl = sqs.sqsClient().getQueueUrl("BillQueue").getQueueUrl();
        logger.warn("queue url -->" + queueUrl);
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(new Gson().toJson(billSQSPojo))
                .withDelaySeconds(0);
        sqs.sqsClient().sendMessage(send_msg_request);
        logger.warn("message is sent to quueue");
        AmazonSQS sqs1 = AmazonSQSClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).build();
        BackgroundThread mySQSPolling = new BackgroundThread(sqs1, queueUrl);
        mySQSPolling.start();
        return null;
    }
}



