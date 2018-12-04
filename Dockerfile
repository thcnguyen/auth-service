FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app.jar --spring.profiles.active=${ENV_NAME}" ]
