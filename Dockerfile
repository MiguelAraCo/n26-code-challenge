FROM maven:3.5.4-jdk-8-alpine AS BUILD_IMAGE

RUN mkdir -p /opt/code
WORKDIR /opt/code

COPY pom.xml .
# Install dependencies first to cache them in their own layer so they don't need to be downloaded again for every change
RUN [ "mvn", "verify", "clean", "--fail-never" ]

COPY src src
RUN ["mvn", "package" ]

FROM openjdk:8-jre-alpine

WORKDIR /opt/challenge

COPY --from=BUILD_IMAGE /opt/code/target/n26-challenge.jar .

EXPOSE 8080

# Docker's EXEC form doesn't evaluate variables like $JAVA_OPTS or $@ but SHELL form doesn't accept arguments.
# And EXEC is the recommended form to use. So in order for the image to evaluate env variables and also accept arguments
# /bin/ash needs to be used. /bin/ash is Alpine's command line interpreter. The -c argument is used to indicate /bin/ash
# to run the following argument as a command.
# Inside the command we use \"$@\" to effectively send all arguments passed by docker run. But in order for it to work
# the -C argument also needs to be sent to /bin/ash so it accepts further arguments as part of the command
ENTRYPOINT [ "/bin/ash", "-c", "java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap $JAVA_OPTS -jar /opt/challenge/n26-challenge.jar \"$@\"", "-C" ]