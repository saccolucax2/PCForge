FROM maven:3.9.5-eclipse-temurin-21

ENV MONGO_ADDRESS=172.31.6.9
ENV MONGO_PORT=27017
ENV AES_KEY_BASE64="1G27fH7RxJmR7J2XeWZxXUpOB9qX9QF5qMu5N7jZqR0="


RUN mkdir -p /source
WORKDIR /source
ADD . /source/

RUN mvn clean package

EXPOSE 8084

CMD ["java", "-jar", "target/authorization-0.0.1-SNAPSHOT.jar"]