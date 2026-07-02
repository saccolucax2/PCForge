FROM maven:3.9.5-eclipse-temurin-21

ENV MONGO_ADDRESS=mongodb
ENV MONGO_PORT=27017


RUN mkdir -p /source
WORKDIR /source
ADD . /source/

RUN mvn clean package -DskipTests

EXPOSE 8082

CMD ["java", "-jar", "target/reviews-0.0.1-SNAPSHOT.jar"]