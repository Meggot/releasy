FROM gcr.io/distroless/java:8
# (can use wildcard as) there can be only one (jar in the target)
ADD target/releasy-*.jar       /app/service.jar
ENTRYPOINT ["java", "-jar", "/app/service.jar"]
