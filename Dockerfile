FROM java:8-jdk-alpine

COPY ./target/user-profile-1.0.jar /usr/app/

WORKDIR /usr/app

RUN sh -c 'touch user-profile-1.0.jar'

ENTRYPOINT ["java","-jar","user-profile-1.0.jar"]