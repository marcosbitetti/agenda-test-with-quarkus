# Observabilidade Local

Esta stack local usa Loki, Promtail e Grafana junto do `docker compose` principal.

## Servicos

- `grafana`: http://localhost:3000
- `loki`: http://localhost:3100
- `promtail`: coleta logs dos containers Docker do projeto

Credenciais padrao do Grafana:

- usuario: `admin`
- senha: `admin`

## Subir a stack

```bash
docker compose up -d db keycloak quarkus-app loki promtail grafana
```

## Validacao rapida

Gerar um evento de autenticacao:

```bash
curl -H 'Content-Type: application/json' \
  -d '{"username":"joao","password":"senha-errada"}' \
  http://localhost:8080/api/auth/login
```

Consultar labels no Loki:

```bash
curl http://localhost:3100/loki/api/v1/labels
```

## Consulta inicial no Grafana

Use a datasource `Loki` e comece por uma consulta ampla:

```logql
{compose_project="agendas", compose_service="quarkus-app"}
```

## Dashboard provisionado

O Grafana agora sobe com um dashboard pronto na pasta `Agenda`:

- `Agenda Auth Overview`

Esse dashboard inclui:

- serie temporal de `auth.login.succeeded` e `auth.login.failed`
- serie temporal de refresh, logout e cleanup de sessao
- serie temporal separada por `outcome`
- serie temporal separada por `httpStatus`
- cards com contagem dos ultimos 15 minutos
- painel de logs com eventos recentes de autenticacao

Observacao: como a aplicacao ainda roda em `quarkus:dev`, parte da saida inclui ruido do terminal interativo. Os eventos estruturados do backend continuam presentes no payload coletado.