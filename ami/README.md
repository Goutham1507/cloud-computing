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