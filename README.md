# N26 Challenge

Solution to the [N26](https://n26.com) challenge.

## Getting started

### Prerequisites

This application can be run either by using a JRE (8+) directly, or through [Docker](https://docker.com):

#### Local JRE

1. Install [maven 3](https://maven.apache.org/)
2. Run the following command in the root directory of the project:
	
	```
	mvn package
	```
	
#### Docker

1. Build the image. E.g.:
	
	```
	docker build --tag miguel-aragon-n26-challenge .
	```
  
## Usage

### Running

**Option 1:** Local JRE

```
java -jar target/n26-challenge.jar \
	[-C[property]]
```

**Option 2:** Docker

```
docker run --name miguel-aragon-n26-challenge -p 8080:8080 \
	[--env JAVA_OPTS=""] \
	miguel-aragon-n26-challenge \
	[-C[property]]
```

### Parameters

Properties can be specified by passing a `-C` parameter. E.g.:

```
-Cspring.profiles.active=dev
```

The application uses [Spring Boot](https://spring.io) so any of their properties can be specified 
(see [Common application properties](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)).

Additionally, the following application-specific properties can be defined:

| Property | Description |
| -------- | ----------- |
| `mx.araco.miguel.n26.sampling-period` | How long it takes for the SamplingStatisticsService to sample new statistics (ISO 8601 duration) |
| `mx.araco.miguel.n26.sample-period` | Period of time that the SamplingStatisticsService calculates transaction statistics of (ISO 8601 duration) | |
