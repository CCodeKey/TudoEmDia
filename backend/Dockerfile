# Etapa de build: compila e empacota o .jar
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /app

# Dependências primeiro, para aproveitar o cache quando só o código muda
COPY .mvn .mvn
COPY mvnw pom.xml checkstyle.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests

# Etapa final: só o JRE e o .jar, executando sem privilégios de root
FROM eclipse-temurin:21-jre-noble
WORKDIR /app

ENV TZ=America/Sao_Paulo
RUN groupadd --system app && useradd --system --gid app app \
	&& mkdir -p /app/uploads && chown app:app /app/uploads

COPY --from=build /app/target/*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
