# Your Auth

API de autenticação para terceiros, criada para abstrair a lógica de autenticação de outros projetos de forma semelhante a serviços como Firebase Auth.

![Versão](https://img.shields.io/badge/vers%C3%A3o-0.0.1--SNAPSHOT-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![Status](https://img.shields.io/badge/status-em%20andamento-yellow)

## Tecnologias utilizadas

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Bean Validation
- Maven
- H2 Database
- PostgreSQL Driver
- Lombok
- JUnit 5
- Spring Boot Test

## Arquitetura

O projeto seguirá uma arquitetura com domínio separado da infraestrutura, mantendo regras de negócio no `domain`, detalhes técnicos no `infra` e entrada/saída HTTP no `presentation`.

```text
src/main/java/com/samuelmaia1_github/yourauth
├── YourAuthApplication.java
├── domain
│   └── user
│       ├── User.java
│       ├── UserService.java
│       ├── UserPolicy.java
│       └── exceptions
│           └── UserException.java
├── infra
│   ├── config
│   ├── exceptions
│   ├── repository
│   └── security
└── presentation
    ├── controller
    ├── dto
    └── exception
```

### Camadas

- `domain`: concentra entidades, services, policies e exceptions relacionadas às regras de negócio.
- `infra`: concentra configurações, segurança, repositórios e exceptions de infraestrutura.
- `presentation`: concentra controllers, DTOs e handlers de exception da API.

> Observação: a estrutura acima representa a arquitetura planejada. No estado atual, o projeto ainda possui apenas a classe principal da aplicação e a configuração básica.

## Como rodar o projeto

### Pré-requisitos

- Java 21 instalado
- Maven ou Maven Wrapper do projeto

### Passos

Clone o projeto e acesse a pasta:

```bash
cd your-auth
```

Rode a aplicação com o Maven Wrapper:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

Por padrão, a aplicação ficará disponível em:

```text
http://localhost:8080
```

Para executar os testes:

```bash
./mvnw test
```

## Exemplos de uso da API

Os endpoints ainda não foram implementados. Esta seção ficará pronta para receber os exemplos reais conforme os controllers forem criados.

### Cadastro de usuário

**URL**

```http
POST /api/v1/auth/register
```

**Request**

```json
{
  "name": "Nome do usuário",
  "email": "usuario@email.com",
  "password": "senha-segura"
}
```

**Curl**

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nome do usuário",
    "email": "usuario@email.com",
    "password": "senha-segura"
  }'
```

**Response**

```json
{
  "id": "id-do-usuario",
  "name": "Nome do usuário",
  "email": "usuario@email.com"
}
```

### Login

**URL**

```http
POST /api/v1/auth/login
```

**Request**

```json
{
  "email": "usuario@email.com",
  "password": "senha-segura"
}
```

**Curl**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@email.com",
    "password": "senha-segura"
  }'
```

**Response**

```json
{
  "accessToken": "token-jwt",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Validação de token

**URL**

```http
GET /api/v1/auth/me
```

**Curl**

```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer token-jwt"
```

**Response**

```json
{
  "id": "id-do-usuario",
  "name": "Nome do usuário",
  "email": "usuario@email.com"
}
```

> Os exemplos acima são placeholders da API esperada e devem ser ajustados conforme os contratos reais forem implementados.

## Testes

Atualmente o projeto possui um teste inicial de contexto da aplicação:

```text
src/test/java/com/samuelmaia1_github/yourauth/YourAuthApplicationTests.java
```

Esta seção deverá ser expandida com:

- Cobertura de testes unitários das regras de negócio em `domain`
- Testes de policies
- Testes de services
- Testes de controllers
- Testes de integração com banco de dados
- Mocks para dependências externas
- Relatório de cobertura

## Próximos passos ou melhorias

- Criar a estrutura de pacotes `domain`, `infra` e `presentation`
- Implementar cadastro de usuários
- Implementar login
- Implementar emissão e validação de tokens
- Implementar configuração de segurança
- Implementar persistência com JPA
- Definir configuração local para H2 e configuração de ambiente para PostgreSQL
- Criar handlers globais de exception
- Adicionar testes unitários e testes de integração
- Documentar contratos reais da API
