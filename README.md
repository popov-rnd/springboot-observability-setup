# Spring Boot Observability Setup

This project demonstrates a **production-ready observability baseline** for a Spring Boot 3.5+ application:
- Structured logging (Logback with async + JSON in prod, pretty text in dev)
- Metrics collection with Micrometer + Prometheus
- Distributed tracing with Micrometer Tracing + OpenTelemetry
- Profiling approaches

---

## Logging Setup

Logging is configured with **`logback-spring.xml`** (instead of plain `application.yaml`) to allow:
- **Async appenders** → request threads never block on I/O
- **Profile-based config**:
    - **Dev** → human-readable colored logs, includes `traceId`/`spanId` only when available
    - **Prod** → JSON logs (Logstash encoder), one object per line, CloudWatch/Loki friendly
- **MDC integration** → Micrometer/OTel automatically injects `traceId`/`spanId`
- **Noise reduction** → framework packages (`org.springframework`, etc.) pushed down to `WARN`

Logs are emitted to **stdout only**, no file appenders — standard for containers and cloud-native setups.

**Security note**:  
Application logs may contain **sensitive data** (headers, user IDs, payloads).
Best practices:
- Never log passwords, secrets, or tokens
- Sanitize personally identifiable information (PII) before logging
- Restrict log access to **trusted operators only** (via IAM roles, RBAC, or log system ACLs)
- Use **secure transport** (TLS) when shipping logs to external systems

---

## Metrics Setup

Metrics are configured via **Spring Boot Actuator + Micrometer** to allow:

- **Prometheus integration** → `/actuator/prometheus` exposes JVM + app metrics for scraping
- **Global tags** → consistent labels across all metrics (e.g., `application`, `env`)
- **Built-in instrumentation** → JVM, Tomcat, datasource, and HTTP server metrics auto-collected
- **Custom business metrics** → via Micrometer’s `Counter`, `Timer`, `Gauge`, etc.

Metrics are **pull-based** (Prometheus scrapes the endpoint), no push gateway needed by default.

**Security note**:  
Prometheus should run in a **private subnet**, and the backend’s `/actuator/prometheus` endpoint must be 
opened **only to the Prometheus server** (via security groups, firewall rules, or Spring Security). 
Never expose it publicly.


## Distributed Tracing Setup

Tracing is configured via **Micrometer Tracing + OpenTelemetry** to allow:

- **End-to-end request visibility** → each incoming request gets a `traceId` / `spanId`
- **Automatic context propagation** → across Spring MVC, RestTemplate, WebClient, Kafka, etc.
- **OTLP export** → spans sent to an OpenTelemetry Collector (Jaeger, Tempo, X-Ray, etc.)
- **Sampling control** → adjustable probability (e.g., 1.0 in dev, 0.1 in prod)
- **MDC integration** → `traceId` / `spanId` automatically appear in logs for correlation

Tracing provides **causality across services** (where metrics show trends, and logs show events).

**Security note**:  
The backend should export spans only to a **trusted OTLP collector endpoint** inside your private network or via mTLS. 
Never expose tracing exporters directly to the internet.


## Profiling Setup

Profiling is used to gain **deeper runtime insights** that go beyond metrics and tracing.  
Two complementary approaches are recommended:

### a) Ad-hoc Profiling

- **Java Flight Recorder (JFR)** → built into the JVM, very low overhead, good for production snapshots
- **Async Profiler** → flame graphs, allocation profiling, CPU/lock analysis
- **Usage** → attach on demand when investigating a performance issue or regression
- **Export formats** → JFR events, `jfr` files, or flamegraphs in HTML

Ad-hoc profiling is **manual and temporary**, ideal for debugging specific problems.

#### Typical Production Workflow (Ad-hoc JFR)

1. Application runs normally with **no JFR overhead**.
2. When an incident happens, attach dynamically:
   ```bash
   jcmd <pid> JFR.start name=debug duration=5m filename=/tmp/app.jfr
   
jcmd is a JDK tool that lets you send diagnostic and control commands to a running Java process.

- <pid> = process ID of your Java app.
- duration=5m = record for 5 minutes
- Recording saved to /tmp/app.jfr.

3. JFR runs for a short time (5–10 minutes), then stops automatically. 
4. Download the recording file from the server.
5. Analyze locally with JDK Mission Control (jmc) or IntelliJ IDEA profiler.

**Security note**:  
JFR dumps contain **sensitive runtime data**. Ensure they are stored securely and only accessible to trusted engineers.


### b) Continuous Profiling

- **eBPF-based profilers** (e.g., [Parca](https://www.parca.dev/), [Pixie](https://px.dev/)) → low-overhead, always-on
- **Commercial tools** (e.g., Datadog, Pyroscope, Grafana Phlare) → long-term storage and visualization
- **Usage** → capture constant performance data, track regressions, optimize resource usage
- **Export formats** → pprof, flamegraphs, time series

Continuous profiling is **automated and ongoing**, providing a feedback loop for optimization in production.

**Security note**:  
Continuous profilers should run in a **private subnet / trusted cluster**. 
Profiles often include stack traces and method names — never expose them to the public internet.