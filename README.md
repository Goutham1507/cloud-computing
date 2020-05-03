# AWS AMI for CSYE 6225

## Validate Template

sh
packer validate ubuntu-ami.json


## Build AMI

sh
packer build \
    -var 'aws_access_key=REDACTED' \
    -var 'aws_secret_key=REDACTED' \
    -var 'aws_region=us-east-1' \
    -var 'subnet_id=REDACTED' \
    ubuntu-ami.json


or 

```
packer build -var-file=./vars.json ubuntu-ami.json
```

# AWS CLOUDFORMATION


## Scripts file path: /infrastructure/network
 
 <p>"networking.json"</p>
 <ul>
 	<li>The cloudFormation template for network stack</li>
 </ul>

## "createStack.sh" script will
<ul>
  <li>Create a cloudformation stack taking STACK_NAME as parameter</li>
	<li>Create and configure required networking resources</li>
	<li>Create a Virtual Private Cloud (VPC) resource </li>
	<li>Create Internet Gateway resource called InternetGateway</li>
	<li>Attach the Internet Gateway tocsye6225-vpc VPC</li>
	<li>Create a public Route Table called public-route-table</li>
	<li>Create a public route in csye6225-public-route-table route table with destination CIDR block 0.0.0.0/0 and csye6225-InternetGateway as the target</li>
</ul>


## Termination stack scripts: 
	script should take STACK_NAME as parameter
<ul>
	<li> "terminate-stack.sh": Delete the stack and all networking resources.</li>
</ul>

## Importing a certifcate to aws certificate manager 
<ul>
<li>sudo aws acm import-certificate --certificate fileb://certificate.pem --certificate-chain fileb://certificate_chain.pem --private-key fileb://mysslcertificate.key --profile prod</li>
</ul>

# serverless

## Email Service Using AWS Lambda Function
As a user, You will be able to request bills due in x days.

## Getting Started
Clone the repository

# Task : AWS CLI Command For CloudFormation

#### CREATE CLOUDFORMATION STACK

aws cloudformation create-stack \
  --stack-name csye6225 \
  --parameters ParameterKey=InstanceTypeParameter,ParameterValue=t2.micro \
  --template-body file://Lamdaapplication.json

#### DELETE CLOUDFORMATION STACK

aws cloudformation delete-stack --stack-name csye6225

WAIT FOR CLOUDFORMATION STACK DELETION

aws cloudformation wait stack-delete-complete --stack-name csye6225


# Task : Trigger Circle CI for EmailLambda.jar to update function

 aws lambda update-function-code --function-name  EmailLambda  --s3-bucket ${BUCKET_NAME} --s3-key EmailLambda.jar --region ${AWS_REGION} --profile dev
 
 # CSYE 6225 - Spring 2020
 
 ## Student Information
 
 | Name | NEU ID | Email Address |
 | --- | --- | --- |
 | Goutham Reddy Valagolam | 001449422| valagolam.g@husky.neu.edu |
 | | | |
 
 ## Technology Stack
 - Programming Language: Java 1.11
 - Web Framework: Springboot 2.2.3.RELEASE
 - Database: MySql
 - IDE: IntelliJ
 - Version Control: Git
 - Project Management: Maven
 - Test Tool: Postman
 - Development Environment: Ubuntu
 
 ## Build Instructions
 Clone the repository into a local repository
 
 Use Maven to build:
 <code>$ mvn clean install -Plocal</code>
 
 run the application by executing in AWS EC2 using below command:
 <code>$ java -Dspring.profiles.active=$springprofilesactive -Ddb.url=$dburl -Ddb.username=$springdatasourceusername -Ddb.password=$springdatasourcepassword -Dbucket.name=$bucketname -jar  clouddemo-0.0.1-SNAPSHOT.jar</code>
 
 The server will be run at http://localhost:8080/, test can be done using Postman.
 
 ## Deploy Instructions
 MySQL port is default 3306 for my application.
 
 Server: server side as RESTful architectural style. As a default, it is listening at http://localhost:8080/
 
 
 ## Running Tests
 Our test files are in the file "src/test", all the functional tests and module tests are included in this file.
 
 ## CI/CD
 
 
