# Fatorial API com Cache Redis Resiliente

Este projeto é uma API REST construída com **Spring Boot 3** e **Java 21**, que calcula o fatorial de um número inteiro e utiliza **Redis como cache** para armazenar os resultados. A aplicação está preparada para **continuar funcionando mesmo se o Redis estiver indisponível**, aplicando as melhores práticas com Lettuce e cache fallback.

---

## 🧠 Funcionalidade

A API:

- Calcula o fatorial de um número inteiro positivo.
- Consulta o cache Redis para verificar se já existe um resultado calculado.
- Salva o resultado no Redis para futuras consultas.
- Se o Redis estiver fora, **calcula o valor normalmente e responde sem erro**.
- Tolerância a falhas implementada com `CacheErrorHandler` personalizado.

---

## 🔧 Tecnologias

- Java 21
- Spring Boot 3
- Spring Cache Abstraction
- Redis 7 (com ACL e autenticação)
- Lettuce Client com pool de conexões
- Docker e Docker Compose

---

## 🚀 Como executar

### 1. Suba os serviços com Docker Compose

```bash
docker-compose up --build
```

Isso iniciará o Redis com autenticação (`appuser` / `senhaF0rte123`) e a aplicação Java.

### 2. Faça uma requisição para calcular o fatorial

```bash
curl http://localhost:8080/api/fatorial/5
```

A resposta será algo como:

```json
{
  "n": 5,
  "fatorial": 120
}
```

---

## 🧪 Testando o fallback (Redis fora)

1. Pare o Redis:
   ```bash
   docker stop redis
   ```

2. Faça a requisição novamente:
   ```bash
   curl http://localhost:8080/api/fatorial/5
   ```

3. O resultado será calculado **sem cache** e a aplicação continuará funcionando normalmente.

---

## 🗂️ Estrutura

- `FatorialController`: expõe a API REST.
- `FatorialService`: contém a lógica de cálculo e uso de cache.
- `SafeCacheErrorHandler`: trata falhas do Redis e garante resiliência.
- `application.yaml`: configura pool Lettuce, timeout, ACL e Redis seguro.

---

## 🔐 Segurança Redis

O Redis é configurado com:

- Usuário ACL: `appuser`
- Senha: `senhaF0rte123`
- Arquivos de configuração:
    - `redis.conf`
    - `users.acl`

---

## 🧰 Endpoints

| Método | URL                     | Descrição                  |
|--------|-------------------------|----------------------------|
| GET    | `/api/fatorial/{n}`    | Calcula e/ou busca no cache |

---


## ⚙️ Configuração Redis (educacional)

O Redis é configurado com autenticação via **ACL (Access Control List)**, protegendo o acesso com usuário e senha. A seguir, explicamos os principais parâmetros usados:

### 🔒 Arquivo `redis.conf`

```conf
bind 0.0.0.0
protected-mode yes
enable-debug-command yes
port 6379
aclfile /usr/local/etc/redis/users.acl
```

- `bind 0.0.0.0`: permite acesso ao Redis de qualquer IP (usado dentro da rede Docker).
- `protected-mode yes`: ativa modo protegido — impede conexões inseguras.
- `enable-debug-command yes`: permite comandos de depuração.
- `port 6379`: porta padrão do Redis.
- `aclfile`: caminho do arquivo ACL que define usuários, senhas e permissões.

### 🔐 Arquivo `users.acl`

```txt
user default off
user appuser on >senhaF0rte123 allcommands allkeys
```

- `user default off`: desativa o usuário padrão para evitar acesso sem permissão.
- `user appuser on`: cria o usuário `appuser` e o ativa.
- `>senhaF0rte123`: define a senha do usuário.
- `allcommands allkeys`: permite que esse usuário execute qualquer comando em qualquer chave.

---

## ⚙️ Configuração Redis no `application.yaml`

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      username: appuser
      password: senhaF0rte123
      timeout: 200ms
      connect-timeout: 2s
      client-type: lettuce
      client-name: factorial-api-client
      lettuce:
        pool:
          enabled: true
          max-active: 50
          max-idle: 20
          min-idle: 5
          max-wait: 2000ms
          time-between-eviction-runs: 60000ms
        shutdown-timeout: 1000ms
```

### 🧠 O que significa cada parâmetro?

| Parâmetro                        | Descrição                                                                 |
|----------------------------------|---------------------------------------------------------------------------|
| `host` / `port`                  | Endereço e porta do Redis                                                |
| `username` / `password`         | Credenciais ACL do Redis (usuário seguro)                                |
| `timeout` / `connect-timeout`   | Tempo de espera para comandos e conexão                                  |
| `client-type`                   | Cliente Redis usado (Lettuce é não-bloqueante e recomendado)             |
| `client-name`                   | Nome identificador visível no Redis (`CLIENT LIST`)                      |
| `pool.enabled`                  | Ativa o uso de pool de conexões Redis                                    |
| `max-active`                    | Máximo de conexões simultâneas no pool                                   |
| `max-idle`, `min-idle`          | Número de conexões ociosas mantidas prontas                              |
| `max-wait`                      | Tempo máximo para esperar uma conexão do pool                            |
| `time-between-eviction-runs`    | Intervalo para limpeza automática de conexões não utilizadas             |
| `shutdown-timeout`              | Tempo para encerramento elegante do pool quando a aplicação for parada   |

Essas configurações garantem que o Redis opere de forma eficiente, segura e resiliente mesmo em produção.

