FROM maven:3.9.3 AS build
WORKDIR /app
#CREATE A VARIABLE
ARG CONTAINER_PORT
COPY pom.xml /app
#download dependency
RUN mvn dependency:resolve
COPY . /app
RUN mvn clean
RUN mvn package -DskipTests -X

FROM openjdk:20
COPY --from=build /app/target/*.jar app.jar
#so here we expose the port of our application to call our application from outside the container
EXPOSE ${CONTAINER_PORT}
CMD  ["java", "-jar", "app.jar"]


