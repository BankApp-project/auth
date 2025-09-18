# Stage 1: Build the application using Maven
#FROM maven:4.0-eclipse-temurin-25-alpine AS builder
FROM amazoncorretto:25-alpine AS builder
# Set the working directory in the container
WORKDIR /app

# Copy the parent pom.xml first to leverage Docker layer caching
COPY pom.xml .

# Copy the pom.xml files for each module
COPY core/pom.xml ./core/
COPY infrastructure/pom.xml ./infrastructure/

# Download all dependencies for all modules. This layer is cached
# and will only be re-run if any pom.xml file changes.
RUN mvn dependency:go-offline

# Copy the source code for each module
COPY core/src ./core/src
COPY infrastructure/src ./infrastructure/src

# Build the entire project. Maven will handle the module dependencies.
# The final runnable JAR will be in the 'infrastructure/target' directory.
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight runtime image
#FROM eclipse-temurin:21-jre-alpine
FROM amazoncorretto:25-alpine
WORKDIR /app

# --- IMPORTANT: Copy the JAR from the correct module's target directory ---
# The executable JAR is built within the 'infrastructure' module.
COPY --from=builder /app/infrastructure/target/*.jar app.jar

# Create a dedicated user and group for security
RUN addgroup -S backend \
 && adduser -S -G backend backend

# Create a logs directory and set permissions
RUN mkdir -p /app/logs \
&& chown backend:backend /app/logs

# Switch to the non-root user
USER backend

# Expose the port the application runs on
EXPOSE 8080

# The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
