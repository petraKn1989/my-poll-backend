# --- BUILD STAGE --------------------------------------------------------
FROM eclipse-temurin:11-jdk AS builder

WORKDIR /app

# Nainstalujeme curl + unzip
RUN apt-get update && apt-get install -y curl unzip gnupg

# Stáhneme a nainstalujeme SBT
RUN curl -L -o sbt.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-1.10.0.deb \
    && apt-get update \
    && apt-get install -y ./sbt.deb \
    && rm sbt.deb

# Zkopírujeme projektové soubory
COPY build.sbt . 
COPY project ./project

# Pre-fetch dependencies (rychlejší build)
RUN sbt update

# Zkopírujeme zbytek projektu
COPY . .

# Build aplikace (Play Framework)
RUN sbt stage

# --- RUNTIME STAGE ------------------------------------------------------
FROM eclipse-temurin:11-jre

WORKDIR /app

# Zkopírujeme připravenou aplikaci ze stage build
COPY --from=builder /app/target/universal/stage ./stage

# Expose port (Railway/localhost)
EXPOSE 9000

# CMD spustí aplikaci s Play secret a portem
CMD ["sh", "-c", "./stage/bin/poll-backend -Dhttp.port=${PORT:-9000} -Dplay.http.secret.key=${PLAY_SECRET}"]
