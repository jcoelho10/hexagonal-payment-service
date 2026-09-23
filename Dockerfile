# ==========================================
# Estágio 1: Build da Aplicação (Maven)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. Copia apenas o pom.xml primeiro para aproveitar o cache de camadas do Docker
COPY pom.xml .

# 2. Baixa as dependências do Maven (offline) para acelerar builds futuros
RUN mvn dependency:go-offline -B

# 3. Copia o código-fonte da aplicação
COPY src ./src

# 4. Executa o build gerando o pacote .jar (pulando os testes no container)
RUN mvn clean package -DskipTests

# ==========================================
# Estágio 2: Imagem Final de Execução (JRE 21)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria um usuário não-root por questões de segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia o arquivo .jar compilado no estágio anterior
COPY --from=builder /build/target/*.jar app.jar

# Garante a permissão para o usuário não-root
RUN chown -R appuser:appgroup /app
USER appuser

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Define a codificação UTF-8 para evitar caracteres quebrados nos logs
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# Comando de inicialização da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]