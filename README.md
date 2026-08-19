# IoT Appliance Platform

A Java 21 / Spring Boot backend for managing connected household appliances across mocked vendors. It provides appliance registration and management, configurable background collection, historical metric storage, daily reporting, and on-demand reporting for custom date ranges.

The application is intentionally self-contained: it uses H2 as its local database and deterministic mock vendor data, so reviewers can exercise the complete workflow without credentials, hardware, or cloud services.

## Capabilities

| Scenario | Implementation | Review endpoint |
|---|---|---|
| Register and manage appliances | Create, list, update, enable/disable, and delete appliance configurations. | `GET|POST /api/appliances`, `PUT|DELETE /api/appliances/{id}` |
| Collect appliance metrics on configurable intervals | Spring scheduler checks every five seconds and collects an enabled appliance when its individual interval is due. | `POST /api/collections/run` forces collection for review. |
| Keep historical appliance data | Every collected metric is stored with appliance ID, collection time, name, value, and unit. | `GET /api/metrics?start&end` |
| Generate daily reports | Aggregates every metric for a UTC calendar day. | `GET /api/reports/daily/{YYYY-MM-DD}` |
| Generate custom-range reports | Aggregates count, min, max, and average per appliance metric. | `GET /api/reports?start&end` |
| Mock external vendors | A pluggable `VendorMetricClient` abstraction returns deterministic normalized mock data. | Register a refrigerator, AC, or another appliance type. |

## Prerequisites

- Java 21
- Maven 3.9 or later

Check the installed versions:

```bash
java --version
mvn --version
```

On macOS with Homebrew:

```bash
brew install openjdk@21 maven
```

## Start the Application

From the repository root, set Java 21 and start Spring Boot:

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
export PATH="$JAVA_HOME/bin:$PATH"
mvn spring-boot:run
```

The API starts at `http://localhost:8080`. Open `http://localhost:8080/` to see a JSON endpoint index. Stop the application with `Ctrl+C`.

Local startup requires no environment variables: the default configuration starts an in-memory H2 database and uses non-secret simulated vendor credentials. Operational health is available at `http://localhost:8080/actuator/health`.

The development H2 console is available at `http://localhost:8080/h2-console`:

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:appliances` |
| User name | `sa` |
| Password | empty |

The database remains available while the process runs and is reset when the process stops.

### Configuration and Deployment

Every operational setting has a local default and can be overridden with an environment variable:

| Setting | Local default | Production use |
|---|---|---|
| `DATABASE_URL` | In-memory H2 | PostgreSQL JDBC URL. |
| `DATABASE_USERNAME`, `DATABASE_PASSWORD` | `sa`, empty password | Database credentials injected by the deployment platform. |
| `JPA_DDL_AUTO` | `update` | Use `validate` after applying versioned Flyway or Liquibase migrations. |
| `H2_CONSOLE_ENABLED` | `true` | Set to `false`. |
| `ACME_BEARER_TOKEN`, `NORTHWIND_API_KEY` | Non-secret local simulation values | Inject values from a secret manager; do not commit real credentials. |
| `NORTHWIND_MAX_REQUESTS_PER_MINUTE`, `NORTHWIND_FAIL_EVERY_REQUESTS` | `60`, `0` | Set vendor operating limits; keep failure simulation disabled outside tests. |
| `COLLECTION_SCHEDULER_DELAY_MS` | `5000` | Tune scheduler polling after considering vendor limits and fleet size. |

The project includes the PostgreSQL JDBC driver. A PostgreSQL deployment also requires a managed database and versioned schema migrations; H2 is only the local-review default.

## Verify the Complete Workflow

Run the integration tests:

```bash
mvn test
```

The test suite verifies API discovery, appliance registration, metric collection, historical reporting, and expected `400`/`404` error responses.

For manual review, start the service and run these commands in a second terminal:

```bash
BASE_URL=http://localhost:8080
START=$(date -u -v-5M +%Y-%m-%dT%H:%M:%SZ)
END=$(date -u -v+5M +%Y-%m-%dT%H:%M:%SZ)
```

For Linux, create timestamps with `date -u -d '5 minutes ago'` and `date -u -d '5 minutes'` instead of macOS `date -v`.

### 1. Discover the API

```bash
curl "$BASE_URL/"
```

The response includes links to all review endpoints.

### 2. Register appliances

Create a refrigerator. The required fields are `name`, `type`, `vendor`, and a positive `collectionIntervalSeconds`.

```bash
curl -X POST "$BASE_URL/api/appliances" \
	-H 'Content-Type: application/json' \
	-d '{
		"name": "Kitchen refrigerator",
		"type": "refrigerator",
		"vendor": "acme",
		"collectionIntervalSeconds": 60
	}'
```

Example response:

```json
{
	"id": 1,
	"name": "Kitchen refrigerator",
	"type": "refrigerator",
	"vendor": "acme",
	"collectionIntervalSeconds": 60,
	"enabled": true,
	"lastCollectedAt": null
}
```

Create an air conditioner from another mock vendor:

```bash
curl -X POST "$BASE_URL/api/appliances" \
	-H 'Content-Type: application/json' \
	-d '{"name":"Bedroom AC","type":"air_conditioner","vendor":"northwind","collectionIntervalSeconds":30}'
```

Mocked metric behavior:

| Appliance type | Metrics collected |
|---|---|
| `refrigerator` or `fridge` | `temperature` in `C`, `door_open` as `0` or `1` in `boolean` |
| `air_conditioner` or `ac` | `temperature` in `C`, `power` in `W` |
| `television` or `tv` | `power` in `W`, `volume` in `percent` |
| `oven` | `temperature` in `C`, `door_open` as `0` or `1` in `boolean` |
| `washer` or `washing_machine` | `cycle_progress` in `percent`, `water_usage` in `L` |
| `dryer` | `cycle_progress` in `percent`, `humidity` in `percent` |
| Any other type | `power` in `W`, `status` as `0` or `1` in `boolean` |

### 3. Manage appliances

List registered appliances:

```bash
curl "$BASE_URL/api/appliances"
```

Update the appliance configuration. `PUT` replaces its configuration; use `enabled: false` to stop both scheduled and manual collection for that appliance.

```bash
curl -X PUT "$BASE_URL/api/appliances/1" \
	-H 'Content-Type: application/json' \
	-d '{
		"name":"Kitchen refrigerator",
		"type":"refrigerator",
		"vendor":"acme",
		"collectionIntervalSeconds":120,
		"enabled":false
	}'
```

Delete an appliance:

```bash
curl -X DELETE "$BASE_URL/api/appliances/1"
```

Deleting or updating an unknown appliance returns `404`.

### 4. Collect metrics on configured intervals

Spring's scheduler checks every five seconds. It collects only appliances that are enabled and whose `collectionIntervalSeconds` has elapsed since `lastCollectedAt`.

For an immediate end-to-end review, trigger a collection run manually. This bypasses the interval check but still does not collect disabled appliances:

```bash
curl -X POST "$BASE_URL/api/collections/run"
```

Example response for two enabled appliances:

```json
{
	"appliancesCollected": 2,
	"metricsStored": 4,
	"failedCollections": 0
}
```

### 5. Retrieve historical time-bounded metrics

Every collection stores raw observations. Query metrics with ISO-8601 timestamps. The range is half-open: it includes `$START` and excludes `$END`, expressed as $[start, end)$.

```bash
curl --get "$BASE_URL/api/metrics" \
	--data-urlencode "start=$START" \
	--data-urlencode "end=$END"
```

Example response:

```json
[
	{
		"applianceId": 1,
		"collectedAt": "2026-08-18T16:06:00Z",
		"metricName": "temperature",
		"value": 2.4,
		"unit": "C"
	}
]
```

An invalid or empty range, where `start` is equal to or after `end`, returns `400`.

### 6. Generate an on-demand report for a custom date range

Reports group historical observations by appliance, metric name, and unit. Each report row has `samples`, `minimum`, `maximum`, and `average`.

```bash
curl --get "$BASE_URL/api/reports" \
	--data-urlencode "start=$START" \
	--data-urlencode "end=$END"
```

Example response:

```json
{
	"start": "2026-08-18T16:00:00Z",
	"end": "2026-08-18T16:10:00Z",
	"metrics": [
		{
			"applianceId": 1,
			"applianceName": "Kitchen refrigerator",
			"metricName": "temperature",
			"unit": "C",
			"samples": 3,
			"minimum": 2.1,
			"maximum": 2.8,
			"average": 2.5
		}
	]
}
```

### 7. Generate a daily report

Daily reports use the UTC interval from midnight to midnight for the requested date:

```bash
curl "$BASE_URL/api/reports/daily/2026-08-18"
```

## API Reference

| Method and path | Request | Description | Success |
|---|---|---|---:|
| `GET /` | none | Browser-friendly API discovery. | `200` |
| `GET /api/appliances` | none | Lists appliance configurations. | `200` |
| `POST /api/appliances` | appliance JSON | Registers an appliance. | `201` |
| `PUT /api/appliances/{id}` | appliance JSON | Replaces configuration. | `200` |
| `DELETE /api/appliances/{id}` | none | Deletes an appliance. | `204` |
| `POST /api/collections/run` | none | Immediately collects enabled appliances; response includes successful, stored, and failed collection counts. | `200` |
| `GET /api/metrics?start&end` | ISO-8601 timestamps | Retrieves raw history. | `200` |
| `GET /api/reports?start&end` | ISO-8601 timestamps | Produces custom-range aggregates. | `200` |
| `GET /api/reports/daily/{date}` | UTC date | Produces one daily aggregate. | `200` |

## Key Design Choices

- **Persistence:** Spring Data JPA stores `Appliance` and `MetricObservation` entities in H2. This is a local-review choice; H2 can be replaced by a production database configuration.
- **Operational safety:** Database, vendor, and scheduler settings are environment-overridable. Actuator exposes `/actuator/health`; H2 console access is configurable; Open Session in View is disabled; historical range reads fetch required appliance data explicitly; and collection failures are logged with appliance context.
- **Background job:** Spring scheduling invokes the collection coordinator every five seconds. Each appliance retains its own collection interval, which prevents every device being polled at the same rate.
- **Vendor integration:** `CollectionService` selects a `VendorMetricClient` by the appliance's vendor. Each adapter owns its authentication, source API style, capabilities, rate-limit behavior, reliability behavior, and conversion to normalized metrics; collection persistence remains vendor-neutral.
- **Vendor reliability:** Expected vendor failures are typed as authentication, capability, rate-limit, or temporary-availability errors. They are counted and logged per appliance, while database and other internal failures are allowed to fail the transaction instead of being misreported as vendor errors.
- **Reports:** The service stores raw observations and derives reports at request time, preserving history and allowing any supported date range.
- **Reviewability:** `POST /api/collections/run` allows immediate data collection instead of waiting for a configured interval.

## Assumptions and Non-Goals

Tenant isolation, secret-manager integration, versioned production database migrations, durable retry/backoff queues, distributed scheduler locking, pagination, and persistent storage across process restarts are outside this take-home scope. The application is prepared for these concerns through environment-based configuration, PostgreSQL JDBC support, configurable H2-console exposure, health probes, and explicit JPA fetch behavior. The local `application.yml` contains non-secret simulated vendor credentials and controls for Northwind's per-minute rate limit and deterministic transient failures. Boolean-like vendor state is stored as numeric `0` or `1` so it can be included in the uniform report format.

### Mock vendor contracts

| Vendor | Simulated API style and authentication | Capabilities | Reliability and rate limit |
|---|---|---|---|
| `acme` | REST payload with bearer-token authentication | Refrigerators and air conditioners; normalized `temperature` and `power` | Authentication failure when the local token is blank. |
| `northwind` | GraphQL payload with API-key authentication | Washers, dryers, televisions; normalized `cycle_progress` and `power` | Configurable per-minute request limit and optional deterministic temporary failures. |
| Other vendor name | Generic local mock, no credential | Any documented appliance type | No simulated transport failure or rate limit. |

## Further Documentation

[DESIGN.md](DESIGN.md) contains the architecture diagrams, data model, complete source-code walkthrough, SOLID review, and production extension guidance.

## AI usage

This implementation was created with AI assistance and reviewed through the included end-to-end test path.