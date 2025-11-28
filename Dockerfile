# Estágio 1: Build da aplicação com Maven
# Usamos uma imagem base que já contém o Maven e o JDK 21
FROM maven:3.9.8-eclipse-temurin-21-jammy AS build

# Define o diretório de trabalho dentro do container
WORKDIR /app

# 1. Copia apenas o pom.xml para aproveitar o cache de camadas do Docker
COPY pom.xml .

# 2. Baixa todas as dependências do projeto. Esta camada será cacheada
# e só será executada novamente se o pom.xml mudar.
RUN mvn dependency:go-offline

# 3. Copia o resto do código-fonte da aplicação
COPY src ./src

# 4. Executa o build do projeto, gerando o arquivo .jar.
# Como as dependências já foram baixadas, este passo é muito mais rápido.
RUN mvn clean package -DskipTests

# Estágio 2: Criação da imagem final
# Usamos uma imagem base enxuta, contendo apenas o necessário para rodar a aplicação (Java 21 Runtime)
FROM eclipse-temurin:21-jre-jammy

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Cria um usuário e grupo não-root para rodar a aplicação
RUN groupadd --system --gid 1001 appgroup && \
    useradd --system --uid 1001 --gid appgroup appuser

# Copia o arquivo .jar gerado no estágio de build para a imagem final
COPY --from=build /app/target/sistema-gerenciamento-tarefas-0.0.1-SNAPSHOT.jar app.jar

# Define o usuário não-root como o usuário que irá rodar a aplicação
USER appuser

# Expõe a porta 8080, que é a porta padrão do Spring Boot
EXPOSE 8080

# Define o comando que será executado quando o container iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]
