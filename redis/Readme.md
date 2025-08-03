Teste com Docker Compose

```shell
docker-compose up -d
docker exec -it redis-server redis-cli
```

Dentro do Redis CLI:
```shell
AUTH appuser senhaF0rte123
SET foo bar
GET foo
```