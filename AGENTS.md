# AGENTS.md

Guia de entrada para agentes e novos chats trabalharem neste repositório.

## Projeto

Your Auth é uma API de autenticação para terceiros. A proposta é centralizar e
abstrair autenticação, emissão de tokens, refresh tokens, usuários por projeto,
contas proprietárias, configurações de senha, configurações de autenticação,
sessões e chaves de API de projeto, de forma parecida com serviços como Firebase
Auth.

O projeto está em andamento. O README contém a intenção original, mas o código
atual já possui implementações em `domain`, `infra`, `presentation`, migrations
Flyway e testes unitários iniciais.

## Objetivo

Construir um serviço de autenticação reutilizável por outros projetos, mantendo:

- regras de negócio testáveis e concentradas no domínio;
- contratos HTTP claros e separados das regras internas;
- persistência substituível por meio de interfaces de repositório do domínio;
- segurança consistente para contas, usuários finais, API keys, access tokens e
  refresh tokens;
- evolução incremental, sem refatorações amplas fora do escopo da tarefa.

## Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Flyway
- Bean Validation
- Maven
- H2 local e driver PostgreSQL
- Lombok
- JUnit 5 e Spring Boot Test
- Auth0 Java JWT

Use o Maven Wrapper quando disponível:

```bash
./mvnw test
./mvnw spring-boot:run
```

Para testes focados:

```bash
./mvnw -Dtest=AccountServiceTest test
```

## Arquitetura

Pacote base:

```text
src/main/java/com/samuelmaia1_github/yourauth
```

Camadas principais:

- `domain`: entidades, value objects, services, policies, interfaces de
  repositório, exceptions e regras de negócio.
- `infra`: configurações Spring, segurança, beans, JPA repositories, entities,
  adapters, mappers de persistência, converters, validações técnicas e utilitários.
- `presentation`: controllers HTTP, DTOs, mappers de entrada/saída e handlers de
  exception da API.

Fluxo preferencial:

```text
Controller -> Presentation Mapper -> Domain Service/Policy -> Domain Repository
Domain Repository <- Infra Adapter <- Spring Data JPA Repository/Entity
Domain Exception -> Presentation Exception Handler -> ErrorResponse
```

### Domínio

O domínio deve expressar regras do produto. Entidades como `Account`, `Project`,
`User`, `AuthConfig`, `PasswordConfig`, `ProjectApiKey`, `UserSession` e refresh
tokens devem continuar representando conceitos de negócio.

Prefira manter o domínio independente de HTTP, DTOs, JPA e detalhes de banco. O
projeto ainda possui alguns acoplamentos existentes, especialmente em fluxos de
auth; ao tocar nesses pontos, não aumente esse acoplamento e reduza-o apenas
quando isso fizer parte do escopo.

Use `Policy` para validações de permissão, unicidade e regras de bloqueio que
podem ser consultadas antes da ação principal. Use `Service` para orquestrar
casos de uso, transações, persistência via interfaces e coordenação entre regras.

### Infra

Infra implementa mecanismos técnicos. Repositórios JPA devem ficar em
`infra.repository`, entidades JPA em `infra.repository.entity` e adapters em
`infra.repository.adapter`.

Interfaces de repositório pertencem ao domínio; adapters implementam essas
interfaces e convertem entre domain objects e entities por meio dos mappers em
`infra.mappers`.

Mudanças de schema devem ser feitas com migrations Flyway em
`src/main/resources/db/migration`. Como `spring.jpa.hibernate.ddl-auto=validate`,
não dependa do Hibernate para criar ou alterar tabelas automaticamente.

### Presentation

Controllers devem ser finos: validar entrada com Bean Validation, converter DTO
para domínio, chamar o service e montar `ResponseEntity` com DTO de resposta.

DTOs são preferencialmente `record`. Mappers de presentation convertem entre DTOs
e domínio e devem evitar lógica de negócio. Exceptions de domínio devem ser
traduzidas para HTTP em `presentation.exception`, retornando `ErrorResponse`
quando aplicável.

## Padrões de Código

- Preserve os nomes e a organização por contexto de negócio: `account`,
  `project`, `user`, `auth`, `refreshtoken`, `projectapikey`, `usersession`,
  `projectmember`.
- Use injeção por construtor, preferencialmente com `@RequiredArgsConstructor`.
- Use `@Transactional` em métodos que alteram estado e exigem atomicidade.
- Mantenha controllers sem regra de negócio.
- Mantenha mappers estáticos simples quando esse já for o padrão local.
- Use exceptions específicas de domínio em vez de `RuntimeException` genérica.
- Não exponha hashes, secrets, refresh tokens persistidos ou chaves de API
  armazenadas em responses. Quando uma chave precisa ser exibida, ela deve ser
  retornada apenas no momento de criação.
- Prefira `Optional` nas interfaces de repositório para buscas que podem falhar.
- Preserve mensagens de erro em português, seguindo o estilo atual.
- Evite introduzir bibliotecas novas sem necessidade clara.
- Não faça refatorações cosméticas grandes junto com mudanças funcionais.

## Responsabilidade Única e Tamanho de Métodos

Separe métodos por responsabilidade real: validação, busca obrigatória,
autorização, montagem de objetos, persistência e geração de tokens são bons
candidatos quando deixam o caso de uso mais legível.

Evite extrair métodos triviais que apenas escondem uma linha óbvia ou criam
indireção sem ganho. O equilíbrio desejado é: métodos curtos o suficiente para
ler o fluxo principal sem esforço, mas sem fragmentar a classe em helpers
artificiais.

Em services, o método público deve deixar claro o caso de uso. Métodos privados
devem nomear intenções relevantes, por exemplo `findProjectOrThrow`,
`ensureCanManage` ou `validateCredentials`.

## Fallbacks

Fallbacks são permitidos apenas quando necessários e justificáveis.

Use fallback quando:

- uma dependência externa pode estar indisponível e existe comportamento seguro;
- há diferença legítima entre ambiente local, teste e produção;
- o fallback preserva segurança e consistência dos dados;
- a ausência do fallback quebraria um fluxo esperado do produto.

Evite fallback quando ele mascarar erro de configuração, falha de segurança,
token inválido, segredo ausente, inconsistência de banco ou bug de programação.
Nesses casos, falhe de forma explícita com exception adequada e tratamento HTTP
correspondente.

## Persistência e Migrations

- Nunca altere migrations antigas já existentes para corrigir comportamento novo;
  crie uma nova migration versionada.
- Mantenha entities JPA alinhadas com migrations e com `ddl-auto=validate`.
- O banco local usa H2 em `./.local-data`; não trate esse arquivo como fonte de
  verdade do schema.
- Ao adicionar campos persistidos, atualize domain object, entity, mapper,
  repository/adapters, migration e testes necessários.
- Cuide de tokens, hashes e secrets como dados sensíveis.

## Segurança

O projeto usa Spring Security com filtros próprios para bearer token e API key de
projeto. Antes de alterar autorização, leia `SecurityConfig`, `SecurityFilter`,
`ProjectApiKeyAuthenticationFilter` e os services de auth relacionados.

Não flexibilize endpoints autenticados sem motivo explícito. Não registre secrets,
tokens completos, hashes ou credenciais em logs, mensagens de erro ou responses.

Fluxos de login devem preservar:

- validação de credenciais com `IPasswordEncoder`;
- bloqueio por tentativas falhas quando configurado;
- controle de sessões quando configurado;
- geração de access token conforme `AuthConfig`;
- refresh token com armazenamento seguro por hash.

## Testes

Para mudanças de regra de negócio, adicione ou ajuste testes unitários em
`src/test/java`. Policies e services devem ter testes focados sem subir contexto
Spring quando possível.

Para mudanças em controllers, security, JPA ou migrations, considere testes de
integração ou pelo menos um teste que cubra o contrato alterado.

Sempre rode o menor conjunto útil de testes para a mudança. Para mudanças
transversais, rode `./mvnw test`.

## Estado Atual e Cuidados

- Existem métodos ainda incompletos ou placeholders em alguns adapters/services.
  Ao depender deles, implemente o comportamento real em vez de contornar no
  chamador.
- Alguns mappers retornam `null` para entrada nula; mantenha esse padrão apenas
  quando o campo realmente for opcional.
- O README pode estar atrasado em relação ao código. Use o código como fonte
  principal e atualize documentação quando a tarefa exigir.
- O workspace pode conter alterações locais não relacionadas. Não reverta,
  formate ou reescreva arquivos fora do escopo.

## Checklist para Agentes

Antes de editar:

- entenda o contexto de negócio afetado;
- leia service, policy, repository interface, adapter, mapper, controller e
  testes relacionados;
- verifique se há migration necessária.

Durante a edição:

- mantenha a mudança pequena e coerente com a arquitetura;
- coloque regra de negócio no domínio;
- coloque detalhes técnicos na infra;
- coloque contratos HTTP na presentation;
- preserve nomes, mensagens e estilo do projeto.

Antes de finalizar:

- rode testes relevantes ou explique por que não foram rodados;
- confira se não expôs dado sensível;
- confira se migrations e entities continuam compatíveis;
- descreva objetivamente o que mudou.
