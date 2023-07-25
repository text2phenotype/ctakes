# CTAKES - Clinical Text And Knowledge Extraction System

Apache cTAKES™ is a natural language processing system for extraction of information from electronic medical record clinical free-text.

# Dependencies

* JDK 13
* Tomcat 9

# Docker builds

There is now support for building and testing docker containers. This work is based on the github repository [text2phenotype/build-tools](https://github.com/text2phenotype/build-tools). Please refer to the documentation in that repository for more information.

# Tests requirements
* Need to define environment variable NLP_HOST=<your_service_url>

# NPI deployment:

NPI pipeline consumes lot of memory (the dictionary is large). So you are able to deploy NPI pipeline separately. 
To do this use next command: `mvn package -f npi-pom.xml`

UPDATE
