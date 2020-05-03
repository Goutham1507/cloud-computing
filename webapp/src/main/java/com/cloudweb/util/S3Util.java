package com.cloudweb.util;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3Util {


    public static String productRetrieveFileFromS3( String fileName, String app_username , String bucketName) {
        AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();
        S3Object retrievedName = null;


        String storedName;
        for( S3ObjectSummary sumObj : S3Objects.inBucket(s3client, bucketName) ) {
            storedName = sumObj.getKey();
            if( storedName.equals(app_username) ) {
                retrievedName = s3client.getObject( bucketName, storedName );
                break;
            }
        }
        if( retrievedName != null ) {
            return retrievedName.getObjectContent().getHttpRequest().getURI().toString();
        }
        else {
            return null;
        }


    }
}
