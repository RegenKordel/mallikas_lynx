# Mallikas

Mallikas is a service in the dependency engine of OpenReq infrastructure that primarily focuses on the contexts, which already contain a large number of existing and dependent requirements, such as large distributed open source projects or large systems engineering projects. For example, the Qt Company has about one hundred-thousand (100,000) issues in its Jira. The dependency engine focuses on the entire body of requirements as an interdependent "requirements model".

This service was created as a result of the OpenReq project funded by the European Union Horizon 2020 Research and Innovation programme under grant agreement No 732463.



# Technical Description

Mallikas should not be accessed directly but it is used only through Milla. Mallikas is a database service for the Milla service used especially in the Qt Jira trial of OpenReq to cache requirements for performance etc. purposes. For further details, see the [swagger documentation](http://217.172.12.199:9203/swagger-ui.html).


# The following technologies are used:
- Java
- Spring Boot
- Maven
- GSON
- H2 database


	
# Public APIs

The API is documented by using Swagger2:

http://217.172.12.199:9204/swagger-ui.html


## How to Install

Run the compiled jar file, e.g., `java -jar Mallikas-1.9.jar`.

Mallikas uses port 9204 that needs to be open. 


## How to Use This Microservice

Mallikas is used only from Milla to store (cache) requirements, see details from [Milla](https://github.com/OpenReqEU/milla/)

The endpoints can be used for testing purposes, see  [swagger documentation](http://217.172.12.199:9203/swagger-ui.html).

# Notes for Developers

None at the moment.

# Sources

None

# How to Contribute

See the OpenReq Contribution Guidelines [here](https://github.com/OpenReqEU/OpenReq/blob/master/CONTRIBUTING.md).

# License

Free use of this software is granted under the terms of the [EPL version 2 (EPL2.0)](https://www.eclipse.org/legal/epl-2.0/).
