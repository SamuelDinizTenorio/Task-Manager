# Sistema de Gerenciamento de Tarefas

API REST para um sistema simples de gerenciamento de tarefas, desenvolvida com Spring Boot. O projeto permite criar, listar, atualizar, deletar e marcar tarefas como concluídas.

## ✨ Funcionalidades

- **CRUD completo de Tarefas**: Crie, Leia, Atualize e Delete tarefas.
- **Conclusão de Tarefas**: Endpoint específico para marcar uma tarefa como concluída.
- **Paginação e Ordenação**: A listagem de tarefas suporta paginação e ordenação para lidar com grandes volumes de dados.
- **Validação de Dados**: Validação robusta dos dados de entrada para garantir a integridade.
- **Tratamento de Exceções**: Respostas de erro padronizadas e claras.
- **Logging**: Logs detalhados para monitoramento e depuração das operações da API.

## 🛠️ Tecnologias Utilizadas

- **Java 21**: Versão mais recente da linguagem Java.
- **Spring Boot 3**: Framework principal para a construção da aplicação.
- **Spring Data JPA**: Para persistência de dados de forma simplificada.
- **PostgreSQL**: Banco de dados relacional utilizado no projeto.
- **Flyway**: Ferramenta para versionamento e migração de schema do banco de dados.
- **Maven**: Gerenciador de dependências e build do projeto.
- **Docker**: Para containerização da aplicação e do banco de dados.
- **Lombok**: Para reduzir código boilerplate (getters, setters, construtores, etc.).

---

## 🚀 Como Executar com Docker (Recomendado)

A forma mais simples de executar o projeto é utilizando Docker e Docker Compose. Isso irá configurar e iniciar tanto a API quanto o banco de dados PostgreSQL automaticamente.

### Pré-requisitos

- **Docker**
- **Docker Compose**

### 1. Clone o Repositório

```bash
git clone <URL_DO_SEU_REPOSITORIO>
cd sistema-gerenciamento-tarefas
```

### 2. Execute com Docker Compose

Na raiz do projeto, execute o seguinte comando:

```bash
docker-compose up -d --build
```

A API estará disponível em `http://localhost:8080`.

### 3. Para Parar a Execução

Para parar e remover os contêineres, redes e volumes, utilize:

```bash
docker-compose down
```

---

## 🔧 Como Executar Manualmente

### Pré-requisitos

- **JDK 21** ou superior.
- **Maven 3.8** ou superior.
- Uma instância do **PostgreSQL** em execução.

### 1. Configure o Banco de Dados

1. Crie um banco de dados no seu PostgreSQL (ex: `tarefas_db`).
2. Configure as variáveis de ambiente que são lidas pelo arquivo `application.properties`. O Spring Boot as utilizará para se conectar ao banco.

   **Exemplo para Linux/macOS:**
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/tarefas_db
   export DB_USERNAME=seu_usuario_postgres
   export DB_PASSWORD=sua_senha_postgres
   ```

   **Exemplo para Windows (PowerShell):**
   ```powershell
   $env:DB_URL="jdbc:postgresql://localhost:5432/tarefas_db"
   $env:DB_USERNAME="seu_usuario_postgres"
   $env:DB_PASSWORD="sua_senha_postgres"
   ```

   > O Flyway criará automaticamente as tabelas necessárias na primeira vez que a aplicação for iniciada.

### 2. Execute a Aplicação

Utilize o Maven para iniciar o servidor Spring Boot.

```bash
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## 📖 Endpoints da API

A URL base para todos os endpoints é `/tasks`.

### Listar todas as tarefas (com paginação)
- **Método**: `GET`
- **Path**: `/tasks`
- **Query Params (Opcionais)**:
  - `page`: Número da página (padrão: 0).
  - `size`: Quantidade de itens por página (padrão: 10).
  - `sort`: Campo para ordenação (ex: `sort=creationDate,desc`).
- **Resposta de Sucesso (200 OK)**:
  ```json
  {
    "content": [
      {
        "id": 1,
        "title": "Configurar ambiente de desenvolvimento",
        "description": "Instalar Java 21 e Maven",
        "createdAt": "2024-10-27T10:00:00",
        "completed": true
      }
    ],
    "pageable": { ... },
    "totalElements": 1,
    ...
  }
  ```

### Buscar tarefa por ID
- **Método**: `GET`
- **Path**: `/tasks/{id}`
- **Resposta de Sucesso (200 OK)**:
  ```json
  {
    "id": 1,
    "title": "Configurar ambiente de desenvolvimento",
    "description": "Instalar Java 21 e Maven",
    "createdAt": "2024-10-27T10:00:00",
    "completed": true
  }
  ```

### Criar uma nova tarefa
- **Método**: `POST`
- **Path**: `/tasks`
- **Corpo da Requisição**:
  ```json
  {
    "title": "Estudar Spring Boot",
    "description": "Ler a documentação sobre REST controllers."
  }
  ```
- **Resposta de Sucesso (201 Created)**: Retorna o objeto da tarefa criada com o header `Location` apontando para o novo recurso.

### Atualizar uma tarefa
- **Método**: `PUT`
- **Path**: `/tasks/{id}`
- **Corpo da Requisição**:
  ```json
  {
    "title": "Estudar Spring Security",
    "description": "Focar em autenticação JWT.",
    "completed": false
  }
  ```
- **Resposta de Sucesso (200 OK)**: Retorna o objeto da tarefa atualizada.

### Deletar uma tarefa
- **Método**: `DELETE`
- **Path**: `/tasks/{id}`
- **Resposta de Sucesso (204 No Content)**: Corpo da resposta vazio.

### Marcar uma tarefa como concluída
- **Método**: `PATCH`
- **Path**: `/tasks/{id}/conclude`
- **Resposta de Sucesso (200 OK)**: Retorna o objeto da tarefa atualizada com o campo `completed` como `true`.