# Sistema de Gerenciamento de Tarefas

[![Build, Test & Scan](https://github.com/SamuelDinizTenorio/Task-Manager/actions/workflows/ci.yml/badge.svg)](https://github.com/SamuelDinizTenorio/Task-Manager/actions)

Este é o backend de uma API REST para um Sistema de Gerenciamento de Tarefas, construído com Spring Boot. A aplicação permite que os usuários se registrem, façam login, gerenciem suas tarefas e, para usuários administradores, gerenciem outros usuários.

O projeto é totalmente containerizado com Docker para facilitar a configuração e execução do ambiente de desenvolvimento.

---

## ✨ Principais Funcionalidades

- **Autenticação e Autorização:** Sistema completo de autenticação baseado em JWT (JSON Web Tokens).
- **Gerenciamento de Usuários:**
  - Registro e Login de usuários.
  - CRUD completo de usuários (disponível para administradores).
  - Atualização de perfil pelo próprio usuário.
  - Sistema de papéis (roles): `USER` e `ADMIN`.
- **Gerenciamento de Tarefas:**
  - CRUD completo de tarefas.
  - Atribuição de tarefas a usuários.
  - Marcação de tarefas como concluídas.
- **Segurança:**
  - Senhas criptografadas com BCrypt.
  - Endpoints protegidos com base no papel do usuário.
  - Validação de entrada de dados.
- **DevOps:**
  - **Containerização:** Configuração completa com `Dockerfile` e `docker-compose.yml` para um ambiente de desenvolvimento fácil de replicar.
  - **CI/CD:** Pipeline automatizada com GitHub Actions que executa testes e análise de segurança (CodeQL) a cada push e pull request.
- **Testes:** Cobertura de testes robusta, incluindo testes unitários, de integração e da camada de persistência.

---

## 🛠️ Tecnologias Utilizadas

- **Backend:**
  - Java 21
  - Spring Boot 3
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - Spring Boot Actuator (para Health Checks)
- **Banco de Dados:**
  - PostgreSQL
  - Flyway (para migrações de banco de dados)
- **Testes:**
  - JUnit 5
  - Mockito
  - Spring Test
- **Autenticação:**
  - JSON Web Token (JWT)
- **DevOps:**
  - Docker & Docker Compose
  - GitHub Actions (CI/CD)
  - Maven (gerenciador de dependências)
- **Outros:**
  - Lombok

---

## 🚀 Como Executar (Ambiente de Desenvolvimento)

Este projeto é configurado para ser executado facilmente com Docker. Siga os passos abaixo.

### Pré-requisitos

- [Docker](https://www.docker.com/get-started/) e [Docker Compose](https://docs.docker.com/compose/install/) instalados.
- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21) (opcional, para desenvolvimento fora do Docker).
- [Maven](https://maven.apache.org/download.cgi) (opcional, para desenvolvimento fora do Docker).

### 1. Clone o Repositório

```bash
git clone <url-do-seu-repositorio>
cd sistema-gerenciamento-tarefas
```

### 2. Crie o Arquivo de Ambiente (`.env`)

Na raiz do projeto, crie um arquivo chamado `.env`. Este arquivo conterá as variáveis de ambiente e segredos para o seu ambiente de desenvolvimento.

> **Importante:** O arquivo `.env` está listado no `.gitignore` e **nunca** deve ser comitado no seu repositório Git.

Copie o conteúdo abaixo para o seu arquivo `.env`, e acrescente valores seguros:

```env
# =================================================
# =             ENVIRONMENT VARIABLES             =
# =================================================
# Este arquivo NUNCA deve ser comitado no Git.
# Ele contém os segredos para o ambiente de desenvolvimento.
# Para produção, use valores fortes e únicos.

# --- Banco de Dados ---
DB_NAME=tarefasdb
DB_USER=user
DB_PASSWORD=password

# --- Aplicação ---
ADMIN_DEFAULT_PASSWORD=password
JWT_SECRET_KEY=my-super-secret-and-long-key-for-jwt-that-is-at-least-256-bits
FRONTEND_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
```

### 3. Inicie a Aplicação com Docker Compose

Com o Docker Desktop em execução, rode o seguinte comando na raiz do projeto:

```bash
docker-compose up -d --build
```

- `up`: Inicia os containers.
- `-d`: "Detached mode", roda os containers em segundo plano.
- `--build`: Força a reconstrução da imagem da sua API, garantindo que as últimas alterações do código sejam incluídas.

A primeira execução pode demorar alguns minutos, pois o Docker irá baixar as imagens base e construir a imagem da sua aplicação.

### 4. Verifique se Tudo Está Rodando

Para verificar o status dos seus containers, use:

```bash
docker-compose ps
```

Você deve ver dois serviços, `tarefas-db` e `tarefas-api`, com o status `running` ou `Up`.

A API estará disponível em `http://localhost:8080`.

---

## 🧪 Como Executar os Testes

Para rodar a suíte completa de testes (unitários e de integração) localmente, use o seguinte comando do Maven:

```bash
mvn test
```

Os testes também são executados automaticamente a cada push e pull request para a branch `main` através da pipeline de CI/CD no GitHub Actions.

---

## 🗺️ Visão Geral dos Endpoints da API

A seguir, uma lista dos principais endpoints disponíveis.

### Autenticação

- `POST /auth/login`: Autentica um usuário e retorna um token JWT.
- `POST /auth/register`: Registra um novo usuário com a role `USER`.

### Tarefas (`/tasks`)

- `GET /tasks`: Lista todas as tarefas de forma paginada.
- `GET /tasks/{id}`: Busca uma tarefa pelo ID.
- `POST /tasks`: Cria uma nova tarefa.
- `PUT /tasks/{id}`: Atualiza uma tarefa existente.
- `DELETE /tasks/{id}`: Deleta uma tarefa.
- `PATCH /tasks/{id}/conclude`: Marca uma tarefa como concluída.

### Usuários (`/users`)

- `GET /users`: (Admin) Lista todos os usuários de forma paginada.
- `GET /users/me`: Retorna os dados do usuário atualmente autenticado.
- `GET /users/{id}`: (Admin) Busca um usuário pelo ID.
- `PATCH /users/{id}`: Atualiza o perfil de um usuário (login/senha). Um usuário pode atualizar seu próprio perfil, e um admin pode atualizar qualquer perfil.
- `PATCH /users/{id}/role`: (Admin) Atualiza a role de um usuário.
- `DELETE /users/{id}`: (Admin) Deleta um usuário.
