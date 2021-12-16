# $name$

## Setup
### Keycloak
https://www.keycloak.org/getting-started/getting-started-docker
```shell
docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin quay.io/keycloak/keycloak:15.1.0
```

## Compile
```shell
./gradlew compile
```

## Test
```shell
./gradlew test
```

## Docker
```shell
 ./gradlew :modules:server:dockerBuildImage
  docker run --rm -p 8080:8080 org.example/server:1.0
```

## Routes
- `GET localhost:8080/ping`
- `GET localhost:8080/metrics`

