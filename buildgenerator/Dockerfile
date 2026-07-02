FROM maven:3.9.5-eclipse-temurin-21

ENV MYSQL_ADDRESS=172.31.6.9
ENV MYSQL_PORT=3306

RUN mkdir -p /source
WORKDIR /source
ADD . /source/

RUN mvn clean package -DskipTests

EXPOSE 8083

CMD ["java", "-jar", "target/buildGenerator-0.0.1-SNAPSHOT.jar"]


