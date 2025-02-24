FROM registry.company.com/gitops/templates:gradle-8-jdk-21
EXPOSE 8080
USER 1001
COPY build/libs/*.jar /app.jar
CMD java -Duser.timezone=UTC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -jar /app.jar