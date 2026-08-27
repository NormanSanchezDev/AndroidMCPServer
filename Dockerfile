FROM eclipse-temurin:22-jre-alpine AS runtime

LABEL maintainer="normansanchez"
LABEL description="Android Corporate MCP Server"

WORKDIR /app

COPY build/libs/android-corporate-mcp-server-*.jar app.jar

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
