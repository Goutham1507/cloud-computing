#!/usr/bin/env bash

echo "Account you want to use dev or prod: "

read account

echo "Stack Name: "

read stack_name

echo "region: "

read vpcRegion

stackId=$(aws cloudformation create-stack --stack-name $stack_name --template-body \
 file://microService.json --profile $account --region $vpcRegion --parameters --capabilities CAPABILITY_NAMED_IAM \
--query [StackId] --output text)

echo "Please check the stack ID below"
echo $stackId
echo "**************************"

if [ -z $stackId ]; then
    echo 'Error occurred TERMINATED'
else
    aws cloudformation wait stack-create-complete --stack-name $stackId --profile $account
    echo "Stack Creation complete"
fi