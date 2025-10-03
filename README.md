# Similar Products Service (Spring Boot)

Exposes `GET /product/{id}/similar` on port **5000**, which returns the **details** of products similar to the given product.
It consumes the provided mocks running on **port 3001**.

## How it works
1. Validates that the base product exists (`GET /product/{id}`).
2. Retrieves similar ids (`GET /product/{id}/similarids`).
3. Fetches each similar product detail in parallel.
4. Returns an array of product detail JSON.

## Run the mocks
Make sure you have Docker Desktop running.

```bash
docker-compose up -d simulado influxdb grafana
curl http://localhost:3001/product/1/similarids
```

## Run the app
```bash
./mvnw spring-boot:run
# or build a jar
./mvnw -DskipTests package
java -jar target/similar-products-service-0.0.1-SNAPSHOT.jar
```

The app listens on `http://localhost:5000`.

Try it:
```
curl http://localhost:5000/product/1/similar
```

## Execute the test
```
docker-compose run --rm k6 run scripts/test.js
```
Open Grafana results at:
```
http://localhost:3000/d/Le2Ku9NMk/k6-performance-test
```

## Config
See `src/main/resources/application.yml`:
```yaml
clients:
  products:
    base-url: http://localhost:3001
    timeout-ms: 500
```

## Packaging a container
```
./mvnw -DskipTests package
docker build -t similar-products-service:latest .
docker run -p 5000:5000 --network host similar-products-service:latest
```

## Notes
- Error mapping:
  - Upstream 404 → 404
  - Upstream timeout → 504
  - Upstream 5xx → 502
- Code organized as controller → service → client (ports & adapters friendly).
