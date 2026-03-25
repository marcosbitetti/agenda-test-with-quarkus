---
title: 'Login embutido com Keycloak via backend'
type: 'feature'
created: '2026-03-24'
status: 'done'
baseline_commit: 'cb58e8dc2b1319002706c85a6e9d6ce89fb4c2cf'
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** A tela inicial expõe dois fluxos de autenticação, incluindo um atalho de desenvolvimento que fala com o Keycloak no navegador e não entrega valor real ao usuário final. Isso vaza o desenho técnico da autenticação, mantém tokens sob responsabilidade do frontend e impede uma experiência de login simples com apenas login ou e-mail e senha.

**Approach:** Substituir a UI atual por um formulário único hospedado na própria aplicação, e mover a autenticação para o backend por meio de um endpoint local que troca credenciais com o Keycloak usando o client confidencial já existente. O navegador interage apenas com a aplicação Agenda; o Keycloak passa a ser um detalhe interno do servidor.

## Boundaries & Constraints

**Always:** manter o Keycloak como fonte única de identidade; evitar qualquer chamada do navegador ao token endpoint do Keycloak; impedir exposição de `client_secret` no HTML ou JavaScript; preservar o endpoint protegido `/api/users/me`; tratar falhas de login com mensagem genérica e sem vazar detalhes internos; manter o fluxo compatível com execução local via Docker Compose.

**Ask First:** mudanças de escopo que exijam logout federado no Keycloak, refresh token persistido em cookie, ou substituição do fluxo por Authorization Code com redirecionamento externo.

**Never:** manter o botão secundário de login dev; armazenar access token em `sessionStorage` ou `localStorage`; enviar credenciais diretamente do navegador para o Keycloak; introduzir um segundo cadastro de usuários fora do Keycloak; reescrever a segurança existente sem necessidade.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| HAPPY_PATH | Usuário abre `/`, informa login ou e-mail e senha válidos | A aplicação envia as credenciais ao backend, autentica no Keycloak, cria sessão local e passa a conseguir consultar `/api/users/me` sem o usuário ver o Keycloak | N/A |
| INVALID_CREDENTIALS | Usuário informa credenciais inválidas | A página permanece no formulário, mostra erro de autenticação e não cria sessão | Retornar `401` do backend com mensagem funcional única, sem propagar resposta crua do Keycloak |
| KEYCLOAK_UNAVAILABLE | Backend não consegue alcançar o Keycloak | A página mostra erro temporário e o usuário permanece deslogado | Retornar `503` do backend com mensagem genérica de indisponibilidade |
| EMPTY_FIELDS | Usuário envia formulário sem login/e-mail ou senha | O frontend bloqueia envio e destaca a obrigatoriedade dos campos | Validar também no backend com `400` |
| AUTHORIZED_REQUEST | Sessão local já existe e usuário chama recurso protegido da própria aplicação | Backend reutiliza a sessão para consultar endpoints internos sem pedir novo login | Se a sessão estiver ausente ou inválida, responder `401` e instruir o frontend a voltar para o formulário |

</frozen-after-approval>

## Code Map

- `quarkus-app/src/main/resources/META-INF/resources/index.html` -- tela inicial e formulário de login atual com dois fluxos expostos.
- `quarkus-app/src/main/java/org/acme/adapters/web/IndexResource.java` -- entrega a página raiz `/`.
- `quarkus-app/src/main/java/org/acme/adapters/web/UserResource.java` -- endpoint autenticado já existente usado para validar sessão e obter o usuário corrente.
- `quarkus-app/src/main/resources/application.properties` -- configuração OIDC atual e ponto natural para propriedades do cliente Keycloak usado pelo backend.
- `quarkus-app/pom.xml` -- dependências Quarkus; pode precisar de cliente HTTP ou suporte a cookies/sessão.
- `keycloak/realm-agenda.json` -- realm e client confidencial usados na autenticação local.

## Tasks & Acceptance

**Execution:**
- [x] `quarkus-app/src/main/java/org/acme/adapters/web` -- adicionar endpoint de autenticação local (`/api/auth/login`) e, se necessário, endpoint de sessão atual -- centralizar a conversa com o Keycloak no backend.
- [x] `quarkus-app/src/main/java/org/acme/adapters/web` -- introduzir o mecanismo de sessão/cookie HTTP-only para reutilizar autenticação sem expor tokens ao browser -- ocultar o Keycloak e reduzir superfície de ataque.
- [x] `quarkus-app/src/main/resources/application.properties` -- declarar propriedades explícitas para URL do realm, client id e secret usados pelo backend de login -- evitar configuração espalhada.
- [x] `quarkus-app/src/main/resources/META-INF/resources/index.html` -- remover o botão secundário e reescrever a tela para um único formulário de login/e-mail e senha, consumindo apenas endpoints locais da aplicação -- entregar UX compatível com o requisito.
- [x] `quarkus-app/src/test/java/org/acme` -- adicionar testes para login bem-sucedido, credenciais inválidas e recurso autenticado com sessão local -- cobrir o comportamento crítico do fluxo novo.

**Acceptance Criteria:**
- Given que o usuário acessa `/`, when a página é renderizada, then apenas um formulário de login ou e-mail e senha é exibido e não há botão ou link visível para o Keycloak.
- Given credenciais válidas no Keycloak, when o formulário é enviado, then a aplicação autentica no backend e estabelece uma sessão local utilizável pelos endpoints protegidos.
- Given credenciais inválidas, when o formulário é enviado, then a aplicação não cria sessão e mostra uma mensagem de falha sem expor detalhes do Keycloak.
- Given que o usuário já possui sessão local válida, when a UI consulta os dados do usuário atual, then a chamada ocorre contra a própria aplicação sem uso de token armazenado no navegador.

## Spec Change Log

## Design Notes

O desenho mais consistente para este contexto é um BFF simples:

1. O navegador envia `username`/`password` para `/api/auth/login` na aplicação Agenda.
2. O backend chama o token endpoint do Keycloak com `grant_type=password`, `client_id` e `client_secret`.
3. Em caso de sucesso, o backend guarda o access token e os dados mínimos da sessão no servidor e entrega ao navegador apenas um identificador de sessão em cookie HTTP-only.
4. Endpoints da aplicação resolvem o usuário autenticado a partir dessa sessão, sem exigir que o frontend monte `Authorization: Bearer`.

Este desenho não elimina uma futura migração para Authorization Code, mas remove imediatamente a exposição do Keycloak e do token ao usuário.

## Verification

**Commands:**
- `docker compose run --rm quarkus-app mvn -DskipTests compile` -- expected: compilação do backend sem erros.
- `docker compose run --rm quarkus-app mvn test` -- expected: testes unitários e de integração do novo fluxo passam.

**Manual checks (if no CLI):**
- Abrir `http://localhost:8080/` e confirmar que a página mostra apenas login/e-mail e senha.
- Efetuar login com um usuário do realm e confirmar que a UI consegue obter o usuário atual sem redirecionar para o Keycloak.

## Suggested Review Order

**Fluxo de autenticacao**

- Entrada principal do login local e emissao do cookie HTTP-only.
	[AuthResource.java:23](../../quarkus-app/src/main/java/org/acme/adapters/web/AuthResource.java#L23)

- Conversa servidor-servidor com o Keycloak e classificacao de falhas.
	[KeycloakPasswordAuthenticator.java:22](../../quarkus-app/src/main/java/org/acme/adapters/keycloak/KeycloakPasswordAuthenticator.java#L22)

- Sessao local em memoria com expiracao e lookup por cookie.
	[AuthSessionService.java:12](../../quarkus-app/src/main/java/org/acme/core/AuthSessionService.java#L12)

**Recurso autenticado**

- `/api/users/me` passa a depender da sessao local, nao de bearer token.
	[UserResource.java:25](../../quarkus-app/src/main/java/org/acme/adapters/web/UserResource.java#L25)

- Sincroniza criacao e atualizacao do perfil local vindo do Keycloak.
	[UserService.java:18](../../quarkus-app/src/main/java/org/acme/core/UserService.java#L18)

**Interface e entrega**

- A raiz `/` serve a tela nova, acessivel direto pelo host.
	[IndexResource.java:19](../../quarkus-app/src/main/java/org/acme/adapters/web/IndexResource.java#L19)

- UI mostra apenas login/e-mail e senha, sem expor o Keycloak.
	[index.html:6](../../quarkus-app/src/main/resources/META-INF/resources/index.html#L6)

- Configuracao separa a URL do Keycloak e as credenciais do cliente backend.
	[application.properties:12](../../quarkus-app/src/main/resources/application.properties#L12)

**Cobertura**

- Testes de integracao validam login, 400, 401 e sessao local.
	[UserResourceIT.java:14](../../quarkus-app/src/test/java/org/acme/integration/UserResourceIT.java#L14)

- Testes unitarios cobrem criacao e sincronizacao do usuario local.
	[UserServiceTest.java:20](../../quarkus-app/src/test/java/org/acme/core/UserServiceTest.java#L20)