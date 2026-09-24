# ==========================================
# Estágio 1: Build da Aplicação (Maven)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. Copia apenas o pom.xml primeiro para aproveitar o cache de camadas do Docker
COPY pom.xml .

# 2. Baixa as dependências do Maven (offline) para acelerar builds futuros (RUN mvn dependency:go-offline -B)
# UP. Pré-carrega as dependências do Maven (acelera re-builds quando o código muda)
RUN mvn dependency:go-offline || true

# 3. Copia o código-fonte da aplicação
COPY src ./src

# 4. Executa o build gerando o pacote .jar (pulando os testes no container durante a criação da imagem)
RUN mvn clean package -DskipTests

# ==========================================
# Estágio 2: Imagem Final de Execução (JRE 21)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Cria um grupo e usuário não-root por boas práticas e segurança do containers
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia o arquivo .jar compilado no estágio anterior
COPY --from=builder /build/target/*.jar app.jar

# Garante a permissão para o usuário não-root
RUN chown -R appuser:appgroup /app
USER appuser

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Define a codificação UTF-8 para evitar caracteres quebrados nos logs
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

# Comando de inicialização da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]