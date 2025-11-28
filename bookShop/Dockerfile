FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar -x test --no-daemon

FROM amazoncorretto:21.0.4
WORKDIR /app
COPY --from=build /app/build/libs/bookShop-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]