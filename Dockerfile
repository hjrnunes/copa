FROM openjdk:8-alpine

COPY target/uberjar/copa.jar /copa/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/copa/app.jar"]
