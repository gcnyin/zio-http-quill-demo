# zio-http-quill-demo

`curl localhost:8080/user/list`

## SQL scripts

```sql
create table "user"
(
    user_id  serial primary key,
    username varchar(255) not null unique,
    password varchar(255) not null
);
```

## Tracing

```shell
docker run -d --name jaeger \                                                                                                                                                         4s
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 14250:14250 \
  -p 14268:14268 \
  -p 14269:14269 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.47.0
 
sbt clean stage

export OTEL_SERVICE_NAME=zio-http-demo OTEL_TRACES_EXPORTE=oltp OTEL_METRICS_EXPORTER=prometheus

./target/universal/stage/bin/zio-http-quill-demo
```

Open `localhost:16686` in browser.
