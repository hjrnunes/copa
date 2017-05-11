FROM java:8-alpine
MAINTAINER Henrique Nunes <hjrnunes@gmail.com>

ADD target/copa.jar copa.jar

EXPOSE 3000

CMD ["java", "-jar", "copa.jar"]
