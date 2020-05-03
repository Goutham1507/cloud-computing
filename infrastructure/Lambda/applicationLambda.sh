echo "AWS Account Name dev or prod "

read account

echo "Stack Name: "

read stack_name

echo " KeyPair: "

read KeyPair

echo "VPC Name: "

read vpcName

echo "Region "

read vpcRegion

echo "CIDR BLOCK: "

read vpcCidr


echo "Creating PUBLIC Subnet"

echo "Public Subnet-1"

echo "Name: "

read pubSubnet1

echo "CIDR: "

read pubSubnet1CIDR

echo "Public Subnet-2"

echo "Name: "

read pubSubnet2

echo "CIDR: "

read pubSubnet2CIDR

echo "Public Subnet-3"

echo "Name: "

read pubSubnet3

echo "CIDR: "

read pubSubnet3CIDR

echo "DBUsername: "

read DBUsername

echo "DBPassword: "

read DBPassword

echo " "

echo "Creating PRIVATE Subnet"

echo "Private Subnet-1"

echo "Name: "

read pvtSubnet1

echo "CIDR: "

read pvtSubnet1CIDR

echo "Private Subnet-2"

echo "Name: "

read pvtSubnet2

echo "CIDR: "

read pvtSubnet2CIDR

echo "Private Subnet-3"

echo "Name: "

read pvtSubnet3

echo "CIDR: "

read pvtSubnet3CIDR

echo "Enter the AMI ID: "

read ImageID

echo "Enter EC2 instance size: "

read EC2VolumeSize

echo "Enter RDS instance size: "

read RDSVolumeSize

echo "Enter S3 Code Deploy Bucket Name: "

read CodeDeployS3Bucket

echo "Enter the domanin name"

read domainName

echo  "Enter the hosted zone id "

read hostedZoneID

echo "please enter the ssl arn"
read SSLCertificate

echo "Script intilaized"

echo ""

stackId=$(aws cloudformation create-stack --stack-name $stack_name --template-body \
 file://applicationLambda.yaml --profile $account --region $vpcRegion  --capabilities CAPABILITY_NAMED_IAM --parameters ParameterKey=pvtSubnet1,ParameterValue=$pvtSubnet1$stack_name ParameterKey=pvtSubnet2,ParameterValue=$pvtSubnet2$stack_name \
ParameterKey=pvtSubnet3,ParameterValue=$pvtSubnet3$stack_name ParameterKey=EC2VolumeSize,ParameterValue=$EC2VolumeSize ParameterKey=RDSVolumeSize,ParameterValue=$RDSVolumeSize ParameterKey=pubSubnet1CIDR,ParameterValue=$pubSubnet1CIDR \
ParameterKey=pubSubnet2CIDR,ParameterValue=$pubSubnet2CIDR ParameterKey=pubSubnet3CIDR,ParameterValue=$pubSubnet3CIDR ParameterKey=pubSubnet1,ParameterValue=$pubSubnet1$stack_name \
ParameterKey=pubSubnet2,ParameterValue=$pubSubnet2$stack_name ParameterKey=pubSubnet3,ParameterValue=$pubSubnet3$stack_name ParameterKey=pvtSubnet1CIDR,ParameterValue=$pvtSubnet1CIDR \
ParameterKey=pvtSubnet2CIDR,ParameterValue=$pvtSubnet2CIDR ParameterKey=pvtSubnet3CIDR,ParameterValue=$pvtSubnet3CIDR ParameterKey=vpcIdUnique,ParameterValue=vpcId$stack_name \
ParameterKey=CodeDeployS3Bucket,ParameterValue=$CodeDeployS3Bucket ParameterKey=vpcName,ParameterValue=$vpcName ParameterKey=ImageID,ParameterValue=$ImageID \
ParameterKey=vpcCidr,ParameterValue=$vpcCidr ParameterKey=KeyPair,ParameterValue=$KeyPair ParameterKey=DBUsername,ParameterValue=$DBUsername \
ParameterKey=DBPassword,ParameterValue=$DBPassword ParameterKey=domainName,ParameterValue=$domainName ParameterKey=hostedZoneID,ParameterValue=$hostedZoneID ParameterKey=SSLCertificate,ParameterValue=$SSLCertificate \
--query [StackId] --output text)
echo  "stack id "
echo 'Your Stack Id: '$stackId

echo "waiting"

if [ -z $stackId ]; then
    echo 'Error occurred.Dont proceed. TERMINATED'
else
    aws cloudformation wait stack-create-complete --stack-name $stackId --profile $account --region $vpcRegion
    echo "Stack Created Successfully................"
fi