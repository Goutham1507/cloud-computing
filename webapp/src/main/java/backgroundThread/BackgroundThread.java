package backgroundThread;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.cloudweb.awsmetric.SQSConfig;
import com.cloudweb.controller.BillController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

public  class BackgroundThread extends Thread {

    AmazonSQS sqs;
    String queueUrl;

    private static final Logger logger = LoggerFactory.getLogger(BackgroundThread.class);


    @Autowired
    BillController billController;


    public BackgroundThread(AmazonSQS sqs, String queueUrl) {
        // TODO Auto-generated constructor stub
        this.sqs = sqs;
        this.queueUrl = queueUrl;

    }

    public void run() {
        while(true) {

            logger.info("Polling started...");
            final ReceiveMessageRequest receive_request =new ReceiveMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withWaitTimeSeconds(20);

            List<Message> result =  Collections.synchronizedList(sqs.receiveMessage(receive_request).getMessages());
            for (Message message : result) {

                logger.info("INvoking SNS...");
                logger.info("Printing message in queue\t"+message.getBody());
                //billController.emailBills(message.getBody());
                AmazonSNS sns = AmazonSNSClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain()).build();
                sns.publish(new PublishRequest(sns.createTopic("bill_due_topic").getTopicArn(), message.getBody() ));
                logger.info("INvoKED SNS...");
                logger.info("INVOKING DELETE...");
                sqs.deleteMessage(queueUrl, message.getReceiptHandle());
                logger.info("Deleted record from queue...");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
