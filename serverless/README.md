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