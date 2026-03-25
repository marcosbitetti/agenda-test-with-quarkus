---
title: 'Migrar home da agenda para fragments HTMX'
type: 'refactor'
created: '2026-03-25T10:30:00Z'
status: 'done'
baseline_commit: '236b9bcda9d72f6c2201cc6456082bacc3c859ae'
superseded_on: '2026-03-25'
superseded_by: 'home-estatica-com-adapter-web-json-only'
context:
  - 'quarkus-app/ARCHITECTURE.md'
  - 'docs/observability.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** A tela [quarkus-app/src/main/resources/META-INF/resources/home.html](quarkus-app/src/main/resources/META-INF/resources/home.html) concentra carregamento de sessão, listagem de contatos, submissão de formulário, exclusão e renderização HTML em JavaScript inline. Isso funciona, mas foge do direcionamento do projeto para endpoints amigáveis a HTMX e aumenta o acoplamento entre markup, estado e comportamento.

**Approach:** Extrair a área da agenda para fragments renderizados no servidor e trocar as mutações de contatos para requisições HTMX, preservando a sessão local, as regras de domínio já implementadas e a experiência visual atual da home. O JavaScript inline deve ficar restrito ao que não couber naturalmente no fluxo HTMX, preferencialmente só para hidratação leve de dados do usuário ou ser removido por completo se o servidor puder renderizar tudo.

## Boundaries & Constraints

**Always:** manter autenticação via cookie `AGENDA_SESSION`; reutilizar `ContactService` e `UserService` sem mover regra de negócio para a camada web; continuar exibindo apenas contatos ativos do usuário autenticado; manter a navegação `/` e `/home`; preservar o visual geral já aprovado; cobrir o fluxo novo com testes de integração.

**Ask First:** adicionar dependência externa de HTMX por CDN ou empacotar asset local se ainda não existir no projeto; alterar significativamente o layout da home; introduzir novos endpoints além do necessário para listagem e mutações da área de contatos.

**Never:** reintroduzir uma modelagem alternativa de `Contact`; voltar a um front orientado exclusivamente por fetch/render manual para contatos; misturar credenciais ou tokens do Keycloak no navegador; expandir escopo para edição de contatos nesta fatia.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| LIST_ACTIVE_CONTACTS | Usuário autenticado acessa `/home` com zero ou mais contatos ativos | A área da agenda é renderizada pelo servidor e mostra lista ou estado vazio sem JS de montagem manual | Se a sessão estiver inválida, redirecionar para `/` |
| CREATE_CONTACT | Formulário válido com nome, sobrenome, data e um ou mais telefones | O contato é persistido, a lista é atualizada por fragment e a mensagem de sucesso aparece na própria área da agenda | Payload inválido retorna fragment de erro sem quebrar a página |
| SOFT_DELETE_CONTACT | Usuário autenticado aciona exclusão de um contato ativo próprio | O contato deixa de aparecer na lista atualizada e o backend mantém soft-delete | Contato inexistente ou de outro usuário retorna resposta controlada sem expor dados |
| EMPTY_STATE | Usuário autenticado não possui contatos ativos | O fragment principal mostra estado vazio e contador zerado | N/A |

</frozen-after-approval>

## Status Update

Este artefato fica mantido apenas como registro historico da decisao tomada naquele momento.

Em 2026-03-25, a arquitetura da agenda de contatos foi corrigida para um frontend estatico consumindo apenas a API JSON autenticada. A implementacao corrente nao usa mais fragments HTMX renderizados no backend, nem endpoints HTML em `ContactResource`.

## Code Map

- `quarkus-app/src/main/java/org/acme/adapters/web/IndexResource.java` -- entrega a home autenticada; pode passar a servir uma versão menos dependente de JS.
- `quarkus-app/src/main/java/org/acme/adapters/web/ContactResource.java` -- API JSON atual; candidato a receber endpoints/produces para fragments HTMX.
- `quarkus-app/src/main/resources/META-INF/resources/home.html` -- shell da home; hoje contém formulário, lista e scripts inline.
- `quarkus-app/src/test/java/org/acme/integration/UserResourceIT.java` -- cobertura de login/home/contatos; deve validar os novos retornos HTML e mutações.

## Tasks & Acceptance

**Execution:**
- [x] `quarkus-app/src/main/java/org/acme/adapters/web/ContactResource.java` -- adicionar endpoints HTMX-friendly para renderizar lista/estado vazio e processar criação/exclusão com respostas HTML parciais -- migrar a agenda para server-driven UI.
- [x] `quarkus-app/src/main/java/org/acme/adapters/web/IndexResource.java` -- garantir que a home entregue o shell correto para carregar os fragments autenticados -- centralizar a composição da tela mantendo o redirecionamento atual.
- [x] `quarkus-app/src/main/resources/META-INF/resources/home.html` -- reduzir a lógica inline e transformar a área de contatos em shell HTMX com targets explícitos para lista, formulário e mensagens -- simplificar a página e alinhar ao padrão arquitetural.
- [x] `quarkus-app/src/test/java/org/acme/integration/UserResourceIT.java` -- adaptar e ampliar os testes para os novos retornos HTML parciais e cenários de erro/estado vazio -- evitar regressão no fluxo autenticado.

**Acceptance Criteria:**
- Given um usuário autenticado, when abrir `/home`, then a agenda deve aparecer renderizada por fragments do servidor sem depender de montagem manual da lista via JavaScript.
- Given um formulário válido, when criar um contato pela própria home, then a lista exibida deve refletir o novo item sem recarregar a página inteira.
- Given um contato ativo do usuário, when excluí-lo pela interface da home, then ele deve sair da lista renderizada e permanecer ausente nas consultas subsequentes.
- Given payload inválido ou contato inexistente, when a ação HTMX falhar, then a página deve continuar utilizável e mostrar feedback local sem expor stack trace.

## Spec Change Log

- 2026-03-25: spec marcado como superado pela direcao `home-estatica-com-adapter-web-json-only`; manter apenas como historico.

## Design Notes

Manter a `home` como shell estático e mover a área da agenda para fragments reduz o acoplamento sem forçar uma reestruturação completa do login nesta fatia. O alvo principal é a agenda de contatos; perfil e logout podem permanecer como controles simples enquanto a parte de contatos passa a ser server-driven.

Exemplo de composição esperada:

```html
<section
  hx-get="/api/contacts/panel"
  hx-trigger="load"
  hx-target="this"
  hx-swap="outerHTML">
</section>
```

## Verification

**Commands:**
- `docker compose run --rm quarkus-app mvn -Dtest=org.acme.integration.UserResourceIT test` -- expected: suíte de integração autenticada verde com a nova composição HTML.
- `docker compose run --rm quarkus-app mvn -Dtest=org.acme.core.ContactServiceTest,org.acme.adapters.persistence.ContactEntityTest test` -- expected: sem regressão na fatia de domínio/persistência já existente.

## Suggested Review Order

**Entry Point**

- Veja primeiro a fronteira web que mantém JSON e adiciona fragments HTMX.
  [ContactResource.java:94](../../quarkus-app/src/main/java/org/acme/adapters/web/ContactResource.java#L94)

- Confira onde o painel inteiro e o formulário HTMX são renderizados no servidor.
  [ContactResource.java:213](../../quarkus-app/src/main/java/org/acme/adapters/web/ContactResource.java#L213)

**Shell HTMX**

- O shell da home agora delega a agenda ao fragment carregado no `load`.
  [home.html:21](../../quarkus-app/src/main/resources/META-INF/resources/home.html#L21)

- Aqui ficam os fallbacks do shell para 401 e falhas de carga.
  [home.html:118](../../quarkus-app/src/main/resources/META-INF/resources/home.html#L118)

**Page Delivery**

- Este ponto deixa explícito que `/home` entrega só o shell autenticado.
  [IndexResource.java:74](../../quarkus-app/src/main/java/org/acme/adapters/web/IndexResource.java#L74)

**Verification**

- Este teste cobre criação via fragment HTML e valida o swap útil.
  [UserResourceIT.java:163](../../quarkus-app/src/test/java/org/acme/integration/UserResourceIT.java#L163)

- Este teste cobre exclusão via fragment e confirma o estado vazio renderizado.
  [UserResourceIT.java:259](../../quarkus-app/src/test/java/org/acme/integration/UserResourceIT.java#L259)