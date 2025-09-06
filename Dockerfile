# Estágio 1: Build da aplicação com Maven
# Usamos uma imagem base que já contém o Maven e o JDK 21
FROM maven:3.9.8-eclipse-temurin-21-jammy AS build
  
# Define o diretório de trabalho dentro do container
WORKDIR /app
  
# Copia o pom.xml e o diretório src para o container
COPY pom.xml .
COPY src ./src
  
# Executa o build do projeto, gerando o arquivo .jar. A flag -DskipTests pula a execução dos testes. 
RUN mvn clean package -DskipTests

# Estágio 2: Criação da imagem final 
# Usamos uma imagem base enxuta, contendo apenas o necessário para rodar a aplicação (Java 21 Runtime)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copia o arquivo .jar gerado no estágio de build para a imagem final
COPY --from=build /app/target/sistema-gerenciamento-tarefas-0.0.1-SNAPSHOT.jar app.jar
  
# Expõe a porta 8080, que é a porta padrão do Spring Boot
EXPOSE 8080
  
# Define o comando que será executado quando o container iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]
