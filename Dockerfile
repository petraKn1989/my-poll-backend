# --- BUILD STAGE --------------------------------------------------------
FROM eclipse-temurin:11-jdk AS builder

WORKDIR /app

# Install curl + unzip
RUN apt-get update && apt-get install -y curl unzip

# Install SBT
RUN curl -L -o sbt.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-1.10.0.deb \
    && apt-get update \
    && apt-get install -y ./sbt.deb

# Copy project files
COPY build.sbt .
COPY project ./project

# Pre-fetch dependencies
RUN sbt update

# Copy the rest of project
COPY . .

# Build Play app
RUN sbt stage

# --- RUNTIME STAGE ------------------------------------------------------
FROM eclipse-temurin:11-jre

WORKDIR /app

# Copy hotový stage (binárky + conf)
COPY --from=builder /app/target/universal/stage ./stage

# Expose port pro Render
EXPOSE 9000

# CMD spustí aplikaci s Play secret a configem
CMD ["sh", "-c", "./stage/bin/poll-backend -Dhttp.port=${PORT} -Dplay.http.secret.key=$PLAY_SECRET"]
