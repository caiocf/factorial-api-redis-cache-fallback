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
````

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

* `FatorialController`: expõe a API REST.
* `FatorialService`: contém a lógica de cálculo e uso de cache.
* `SafeCacheErrorHandler`: trata falhas do Redis e garante resiliência.
* `application.yaml`: configura pool Lettuce, timeout, ACL e Redis seguro.

---

## 🔐 Segurança Redis

O Redis é configurado com:

* Usuário ACL: `appuser`
* Senha: `senhaF0rte123`
* Arquivos de configuração:

    * `redis.conf`
    * `users.acl`

---

## 🧰 Endpoints

| Método | URL                 | Descrição                   |
| ------ | ------------------- | --------------------------- |
| GET    | `/api/fatorial/{n}` | Calcula e/ou busca no cache |

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

| Parâmetro                           | Descrição                                                              |
| ----------------------------------- | ---------------------------------------------------------------------- |
| `host` / `port`                     | Endereço e porta do Redis                                              |
| `username` / `password`             | Credenciais ACL do Redis (usuário seguro)                              |
| `timeout` / `connect-timeout`       | Tempo de espera para comandos e conexão                                |
| `client-type`                       | Cliente Redis usado (Lettuce é não-bloqueante e recomendado pela AWS)  |
| `client-name`                       | Nome identificador visível no Redis (`CLIENT LIST`)                    |
| `pool.enabled`                      | Ativa o uso de pool de conexões Redis                                  |
| `max-active`                        | Máximo de conexões simultâneas no pool                                 |
| `max-idle`, `min-idle`              | Número de conexões ociosas mantidas prontas                            |
| `max-wait`                          | Tempo máximo para esperar uma conexão do pool                          |
| `time-between-eviction-runs`        | Intervalo para limpeza automática de conexões não utilizadas           |
| `shutdown-timeout`                  | Tempo para encerramento elegante do pool quando a aplicação for parada |
| `networkaddress.cache.ttl`          | (JVM) Tempo de cache de IPs DNS (padrão: -1 → infinito)                |
| `networkaddress.cache.negative.ttl` | (JVM) Tempo de cache de falhas de DNS (padrão: 10 segundos)            |

🔧 Para configurar os parâmetros da JVM, use:

```bash
-Dnetworkaddress.cache.ttl=10
-Dnetworkaddress.cache.negative.ttl=0
```

---

## ⚠️ Riscos do padrão da JVM (DNS cache)

Por padrão, a JVM mantém indefinidamente os IPs resolvidos por DNS em cache. Isso pode causar **problemas graves** em ambientes dinâmicos, como Redis em nuvem com failover (Ex: ElastiCache AWS).

### Valores padrão da JVM:

| Parâmetro                           | Padrão da JVM  | Efeito                                      |
| ----------------------------------- |----------------| ------------------------------------------- |
| `networkaddress.cache.ttl`          | `-1` ou `null` | Mantém IP em cache indefinidamente          |
| `networkaddress.cache.negative.ttl` | `10`           | Cacheia falhas de resolução por 10 segundos |

O valor null significa que o parâmetro não foi definido explicitamente, mas a JVM ainda aplicará um valor padrão interno (tipicamente 30s, ou infinito se houver SecurityManage

A Oracle depreciou o SecurityManager no Java 17:
    “SecurityManager is deprecated and will be removed in a future release.”
    📚 JEP 411

E ele foi removido no Java 18. Mas continua funcionando os parâmetros networkaddress, sem as feature do SecurityManager.


Para Java 8 ou inferior (openjdk-8/jre/lib/security/java.security):
```text
#
# The Java-level namelookup cache policy for successful lookups:
#
# any negative value: caching forever
# any positive value: the number of seconds to cache an address for
# zero: do not cache
#
# default value is forever (FOREVER). For security reasons, this
# caching is made forever when a security manager is set. When a security
# manager is not set, the default behavior in this implementation
# is to cache for 30 seconds.
#
# NOTE: setting this to anything other than the default value can have
#       serious security implications. Do not set it unless
#       you are sure you are not exposed to DNS spoofing attack.
#
#networkaddress.cache.ttl=-1
```

Para Java 11 ou superior (openjdk-11/conf/security/java.security):
```text
#
# The Java-level namelookup cache policy for successful lookups:
#
# any negative value: caching forever
# any positive value: the number of seconds to cache an address for
# zero: do not cache
#
# default value is forever (FOREVER). For security reasons, this
# caching is made forever when a security manager is set. When a security
# manager is not set, the default behavior in this implementation
# is to cache for 30 seconds.
#
# NOTE: setting this to anything other than the default value can have
#       serious security implications. Do not set it unless
#       you are sure you are not exposed to DNS spoofing attack.
#
#networkaddress.cache.ttl=-1

# The Java-level namelookup cache policy for failed lookups:
#
# any negative value: cache forever
# any positive value: the number of seconds to cache negative lookup results
# zero: do not cache
#
# In some Microsoft Windows networking environments that employ
# the WINS name service in addition to DNS, name service lookups
# that fail may take a noticeably long time to return (approx. 5 seconds).
# For this reason the default caching policy is to maintain these
# results for 10 seconds.
#
networkaddress.cache.negative.ttl=10
```

### Riscos:

* Após failover, a aplicação pode continuar usando um IP antigo e falhar.
* O Redis pode mudar de IP e a aplicação não reconhecerá, gerando `TimeoutException`.

### Recomendação:

Configure os parâmetros para forçar a JVM a revalidar os IPs:

```bash
-Dnetworkaddress.cache.ttl=10
-Dnetworkaddress.cache.negative.ttl=0
```

📚 Referência oficial:
🔗 [Oracle Networking Properties Link icon](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/doc-files/net-properties.html)


---

## 📢 Por que usar Lettuce com Redis na AWS?

A própria AWS recomenda o uso do **cliente Lettuce** em aplicações Java, por ser:

* Não-bloqueante (reactivo)
* Compatível com ambientes com **failover automático**, como ElastiCache
* Compatível com **topologia estática** e **conexões TLS**

📚 Referência oficial:
🔗 [AWS Docs – Lettuce Best Practices](https://docs.aws.amazon.com/AmazonElastiCache/latest/dg/BestPractices.Clients-lettuce.html)

No contexto de AWS ElastiCache (inclusive Serverless), você **não deve usar Redis Sentinel**. Basta apontar para o **endpoint DNS principal**, deixar o Lettuce gerenciar a conexão, e ajustar o TTL da JVM conforme indicado acima.

---

