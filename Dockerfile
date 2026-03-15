# Usa uma imagem do Maven para compilar o código Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copia o pom.xml e as dependências primeiro (otimiza a cache do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline
# Copia o código fonte e gera o .jar
COPY src ./src
RUN mvn clean package -DskipTests

# Imagem final leve para rodar a aplicação
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]