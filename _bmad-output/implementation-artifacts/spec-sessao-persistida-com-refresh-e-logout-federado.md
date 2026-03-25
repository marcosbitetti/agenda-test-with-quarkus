---
title: 'Sessao persistida com refresh e logout federado'
type: 'feature'
created: '2026-03-24'
status: 'done'
baseline_commit: 'cb58e8dc2b1319002706c85a6e9d6ce89fb4c2cf'
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O login embutido atual ainda depende de sessão em memória, o que perde autenticação ao reiniciar a aplicação e não funciona bem fora de uma única instância. Além disso, a sessão local expira sem renovação transparente e o logout atual remove apenas o cookie local, sem encerrar a sessão correspondente no Keycloak.

**Approach:** Persistir a sessão autenticada no Postgres, incluindo os metadados necessários para refresh token no backend, e completar o ciclo de autenticação com renovação transparente do access token e logout federado no Keycloak. O navegador continua falando apenas com a Agenda, enquanto o backend mantém a sessão válida e a encerra corretamente nos dois lados.

## Boundaries & Constraints

**Always:** manter o Keycloak como fonte única de identidade; armazenar refresh token apenas no backend; persistir a sessão em estrutura compatível com o padrão atual de persistência Quarkus/Panache; renovar token apenas no servidor; preservar o cookie HTTP-only atual; em logout, tentar encerrar a sessão no Keycloak e sempre limpar a sessão local e o cookie; manter compatibilidade com execução local via Docker Compose e Postgres já existente.

**Ask First:** troca de Postgres por Redis ou outro store distribuído; rotação de chaves, criptografia at-rest de refresh token fora do escopo atual; adoção de Authorization Code Flow no lugar do fluxo backend com senha; mudança de semântica visível da tela de login.

**Never:** voltar a guardar token no navegador; expor refresh token ao frontend; depender de memória local como fonte primária da sessão; quebrar o endpoint `/api/users/me`; exigir interação visível do usuário com o Keycloak; falhar no logout local apenas porque o Keycloak está momentaneamente indisponível.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| LOGIN_HAPPY_PATH | Usuário envia login ou e-mail e senha válidos | Backend autentica no Keycloak, persiste sessão com tokens e expiração, responde com cookie HTTP-only e usuário autenticado | N/A |
| RESTART_WITH_ACTIVE_SESSION | Aplicação reinicia e o navegador ainda possui cookie válido | Backend recarrega a sessão do Postgres e mantém o acesso sem pedir novo login | Se a sessão persistida estiver ausente, responder `401` |
| ACCESS_TOKEN_EXPIRED | Cookie válido, access token expirado, refresh token ainda válido | Backend renova o token no Keycloak de forma transparente, atualiza a sessão persistida e atende a requisição | Se o refresh falhar por token inválido/expirado, invalidar a sessão local e responder `401` |
| FEDERATED_LOGOUT | Usuário aciona logout com sessão local válida | Backend chama o endpoint de logout/revogação do Keycloak, remove a sessão persistida e expira o cookie | Se Keycloak falhar, ainda remover sessão local e retornar sucesso funcional ao navegador |
| EXPIRED_SESSION | Cookie aponta para sessão vencida ou refresh token vencido | Backend nega acesso e exige novo login | Limpar sessão persistida e responder `401` |

</frozen-after-approval>

## Code Map

- `quarkus-app/src/main/java/org/acme/core/AuthSessionService.java` -- serviço atual de sessão em memória que precisa migrar para persistência real.
- `quarkus-app/src/main/java/org/acme/adapters/keycloak/KeycloakPasswordAuthenticator.java` -- cliente atual do Keycloak; precisa passar a capturar refresh token, renovar access token e encerrar sessão remota.
- `quarkus-app/src/main/java/org/acme/adapters/web/AuthResource.java` -- endpoint de login/logout que emite cookie e controla a sessão do navegador.
- `quarkus-app/src/main/java/org/acme/adapters/web/UserResource.java` -- principal consumidor da sessão autenticada; precisa aceitar renovação transparente.
- `quarkus-app/src/main/resources/application.properties` -- propriedades do cliente Keycloak e ponto natural para configurar endpoints e comportamento de refresh/logout.
- `quarkus-app/src/main/java/org/acme/adapters/persistence` -- padrão já existente de entity/repository Panache a ser reutilizado para sessão persistida.
- `quarkus-app/src/test/java/org/acme/integration/UserResourceIT.java` -- teste atual do fluxo autenticado; precisa cobrir persistência, refresh e logout federado.

## Tasks & Acceptance

**Execution:**
- [x] `quarkus-app/src/main/java/org/acme/adapters/persistence` -- adicionar entity e suporte de persistência para sessões autenticadas com expiração e refresh token -- substituir o store em memória por Postgres.
- [x] `quarkus-app/src/main/java/org/acme/core/AuthSessionService.java` -- migrar o serviço para usar persistência, recuperação após restart, invalidação e atualização de tokens -- tornar a sessão reutilizável e durável.
- [x] `quarkus-app/src/main/java/org/acme/adapters/keycloak/KeycloakPasswordAuthenticator.java` -- suportar login, refresh token e logout federado com o Keycloak -- completar o ciclo de sessão no backend.
- [x] `quarkus-app/src/main/java/org/acme/adapters/web/AuthResource.java` e `quarkus-app/src/main/java/org/acme/adapters/web/UserResource.java` -- usar sessão persistida, renovar access token quando necessário e encerrar sessão local/remota no logout -- manter a API funcional sem expor detalhes ao frontend.
- [x] `quarkus-app/src/main/resources/application.properties` -- declarar propriedades explícitas para refresh/logout e tempos de sessão -- centralizar a configuração operacional do fluxo.
- [x] `quarkus-app/src/test/java/org/acme` -- adicionar ou ampliar testes para sessão persistida, refresh bem-sucedido, expiração e logout federado com fallback local -- cobrir os cenários de maior risco.

**Acceptance Criteria:**
- Given que o usuário faz login com sucesso, when a aplicação reinicia, then a sessão continua válida enquanto os dados persistidos e o refresh token ainda permitirem renovação.
- Given que o access token expira e a sessão local ainda possui refresh token válido, when o usuário acessa `/api/users/me`, then o backend renova o token automaticamente e atende a requisição sem exigir novo login.
- Given que o refresh token também expirou ou foi revogado, when o usuário acessa um endpoint autenticado, then a sessão persistida é invalidada e a resposta é `401`.
- Given que o usuário aciona logout, when a requisição é processada, then a sessão local é removida, o cookie é expirado e o backend tenta encerrar a sessão correspondente no Keycloak sem depender do frontend.

## Spec Change Log

## Design Notes

O incremento mais consistente com a arquitetura atual é manter o padrão BFF e trocar apenas a implementação da sessão:

1. `AuthResource` continua sendo a porta de entrada do login.
2. `KeycloakPasswordAuthenticator` passa a devolver access token, refresh token e tempos de expiração.
3. `AuthSessionService` persiste a sessão em uma tabela própria, consultada por cookie.
4. Ao consumir a sessão, o backend renova tokens quando necessário antes de responder.
5. No logout, o backend primeiro tenta encerrar a sessão no Keycloak e depois sempre limpa a sessão local.

Isso preserva a UX já entregue e corrige o problema operacional sem reabrir a interface de login.

## Verification

**Commands:**
- `docker compose run --rm quarkus-app mvn -DskipTests compile` -- expected: compilação da aplicação sem erros.
- `docker compose run --rm quarkus-app mvn -Dtest=org.acme.core.UserServiceTest,org.acme.integration.UserResourceIT test` -- expected: testes existentes e novos do fluxo autenticado passam.

**Manual checks (if no CLI):**
- Fazer login em `http://localhost:8080/`, reiniciar o container da aplicação e confirmar que `/api/users/me` continua funcionando.
- Fazer logout e confirmar que o cookie some e uma nova chamada autenticada volta a responder `401`.

## Suggested Review Order

**Fluxo principal**

- Entrada do login agora grava sessão durável e cookie com vida útil do refresh.
	[AuthResource.java:23](../../quarkus-app/src/main/java/org/acme/adapters/web/AuthResource.java#L23)

- Serviço central decide persistência, refresh transparente e logout local/remoto.
	[AuthSessionService.java:13](../../quarkus-app/src/main/java/org/acme/core/AuthSessionService.java#L13)

- Cliente Keycloak concentra password grant, refresh token e logout federado.
	[KeycloakPasswordAuthenticator.java:22](../../quarkus-app/src/main/java/org/acme/adapters/keycloak/KeycloakPasswordAuthenticator.java#L22)

**Persistência**

- Entidade define a tabela de sessões autenticadas no Postgres.
	[AuthSessionEntity.java:14](../../quarkus-app/src/main/java/org/acme/adapters/persistence/AuthSessionEntity.java#L14)

- Repositório persiste, atualiza e remove sessões por `session_id`.
	[AuthSessionRepositoryImpl.java:11](../../quarkus-app/src/main/java/org/acme/adapters/persistence/AuthSessionRepositoryImpl.java#L11)

**Consumo da sessão**

- Endpoint autenticado reutiliza a sessão persistida e responde `503` em falha de refresh.
	[UserResource.java:25](../../quarkus-app/src/main/java/org/acme/adapters/web/UserResource.java#L25)

- Configuração expõe o skew de refresh e mantém centralizadas as propriedades do backend Keycloak.
	[application.properties:16](../../quarkus-app/src/main/resources/application.properties#L16)

**Cobertura**

- Testes de integração cobrem refresh, expiração definitiva e logout federado.
	[UserResourceIT.java:18](../../quarkus-app/src/test/java/org/acme/integration/UserResourceIT.java#L18)

- Helper de teste controla expiração de tokens sem poluir código de produção.
	[AuthSessionTestSupport.java:10](../../quarkus-app/src/test/java/org/acme/integration/AuthSessionTestSupport.java#L10)