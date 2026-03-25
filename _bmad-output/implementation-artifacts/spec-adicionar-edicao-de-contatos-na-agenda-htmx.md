---
title: 'Adicionar edicao de contatos na agenda HTMX'
type: 'feature'
created: '2026-03-25T11:05:00Z'
status: 'done'
baseline_commit: '138575d7e60fa68aa297d8ed51bec9e245723034'
superseded_on: '2026-03-25'
superseded_by: 'home-estatica-com-adapter-web-json-only'
context:
  - 'quarkus-app/ARCHITECTURE.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** A agenda já permite criar, listar e excluir contatos, mas ainda não permite corrigir ou atualizar um contato existente. Isso força o usuário a excluir e recriar registros, o que empobrece a experiência e deixa a fatia de CRUD incompleta.

**Approach:** Adicionar edição de contatos preservando o fluxo atual da home orientado por HTMX e a API JSON já existente. A atualização deve respeitar dono do contato, regras de domínio, soft-delete por status e a renderização server-driven do painel, sem recarregar a página inteira.

## Boundaries & Constraints

**Always:** manter autenticação por `AGENDA_SESSION`; validar ownership com `ownerUserId`; editar apenas contatos ativos; manter ao menos um telefone por contato; atualizar `updatedAt`; preservar escape HTML e feedback local do painel HTMX; cobrir a nova fatia com testes de service e integração.

**Ask First:** mudar o layout geral da home; introduzir modal, drawer ou navegação separada para edição; alterar a semântica da API JSON já usada externamente; permitir edição de contatos deletados.

**Never:** criar uma segunda modelagem de `Contact`; mover regra de atualização para a camada web; expor token do Keycloak no navegador; expandir esta fatia para reativação de contatos, paginação ou ordenação avançada.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| START_EDIT | Usuário autenticado aciona editar em um contato ativo próprio | O painel troca o formulário de criação por um formulário preenchido com os dados atuais do contato | Se o contato não existir ou não pertencer ao usuário, devolver feedback controlado no painel |
| UPDATE_CONTACT | Formulário válido com novos dados e um ou mais telefones | O contato é atualizado, a lista/painel reflete os novos dados e uma mensagem de sucesso é exibida | Se a validação falhar, o formulário de edição permanece preenchido com feedback local |
| CANCEL_EDIT | Usuário cancela a edição em andamento | O painel volta ao estado padrão com formulário de criação e lista atual | N/A |
| INVALID_UPDATE | Payload inválido ou contato inexistente | Nenhum dado é persistido e o painel continua utilizável | Retornar fragmento HTML controlado ou 404/400 no modo JSON |

</frozen-after-approval>

## Status Update

Este artefato fica mantido apenas como registro historico da iteracao anterior.

Em 2026-03-25, a estrategia de edicao de contatos via painel HTMX server-driven foi abandonada. A implementacao atual usa home estatica com renderizacao no navegador e `ContactResource` restrito a respostas JSON.

## Code Map

- `quarkus-app/src/main/java/org/acme/core/ContactService.java` -- hoje cria/lista/exclui; precisa expor update com regras de negócio e ownership.
- `quarkus-app/src/main/java/org/acme/adapters/persistence/ContactRepositoryImpl.java` -- persiste e faz soft-delete; precisa aplicar update do agregado e renovar telefones ativos.
- `quarkus-app/src/main/java/org/acme/adapters/web/ContactResource.java` -- hoje renderiza o painel HTMX e os endpoints JSON; será o ponto central da edição.
- `quarkus-app/src/main/resources/META-INF/resources/home.html` -- shell da home; deve continuar simples, só consumindo o painel HTMX.
- `quarkus-app/src/test/java/org/acme/core/ContactServiceTest.java` -- deve validar atualização e troca da lista de telefones.
- `quarkus-app/src/test/java/org/acme/integration/UserResourceIT.java` -- deve validar o fluxo de edição no JSON e no painel HTML.

## Tasks & Acceptance

**Execution:**
- [x] `quarkus-app/src/main/java/org/acme/core/ContactService.java` -- adicionar operação de update transacional com validação de dono, campos obrigatórios e nova lista de telefones -- fechar a regra de negócio da edição.
- [x] `quarkus-app/src/main/java/org/acme/adapters/persistence/ContactRepositoryImpl.java` -- implementar atualização do contato ativo e substituição segura dos telefones ativos anteriores -- manter coerência do agregado e soft-delete lógico dos telefones antigos.
- [x] `quarkus-app/src/main/java/org/acme/adapters/web/ContactResource.java` -- expor endpoints JSON e HTMX para iniciar, enviar e cancelar a edição, com formulário preenchido e feedback local -- completar a experiência server-driven da agenda.
- [x] `quarkus-app/src/main/resources/META-INF/resources/home.html` -- manter o shell atual sem voltar a lógica manual de contatos em JavaScript -- preservar a separação entre shell e fragmentos.
- [x] `quarkus-app/src/test/java/org/acme/core/ContactServiceTest.java` -- cobrir update válido e inválido, incluindo troca de telefones -- proteger a camada core.
- [x] `quarkus-app/src/test/java/org/acme/integration/UserResourceIT.java` -- cobrir início da edição, update HTMX, cancelamento e erros controlados -- proteger o fluxo autenticado ponta a ponta.

**Acceptance Criteria:**
- Given um contato ativo do usuário autenticado, when iniciar a edição pela home, then o painel deve exibir um formulário preenchido com os dados atuais desse contato.
- Given dados válidos, when salvar a edição pelo painel HTMX, then o contato deve aparecer atualizado sem recarregar a página inteira.
- Given dados inválidos, when tentar salvar a edição, then o formulário deve permanecer preenchido e mostrar feedback local sem perder o contexto.
- Given um contato inexistente ou de outro usuário, when tentar editar ou salvar, then a aplicação deve responder de forma controlada sem expor dados ou stack trace.

## Spec Change Log

- 2026-03-25: spec marcado como superado pela direcao `home-estatica-com-adapter-web-json-only`; manter apenas como historico.

## Design Notes

O caminho menos arriscado é manter um único painel HTMX e alternar apenas o estado do formulário entre criação e edição. Isso evita criar uma nova página ou complexidade extra no shell, mantém o padrão da fatia anterior e reduz o número de superfícies que precisam ser sincronizadas.

Exemplo de direção esperada:

```html
<button
  hx-get="/api/contacts/panel/42/edit"
  hx-target="#contacts-panel"
  hx-swap="outerHTML">
  Editar
</button>
```

## Verification

**Commands:**
- `docker compose run --rm quarkus-app mvn -Dtest=org.acme.core.ContactServiceTest test` -- expected: cenários de update e troca de telefones verdes.
- `docker compose run --rm quarkus-app mvn -Dtest=org.acme.integration.UserResourceIT test` -- expected: fluxo autenticado de edição HTMX e JSON verde.