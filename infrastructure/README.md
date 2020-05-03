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