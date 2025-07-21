# Enron Email Processor Service

A cloud-native Spring Boot service for processing the [Enron Email Dataset (2015)](https://www.cs.cmu.edu/~enron/enron_mail_20150507.tar.gz).  
The service ingests the dataset and exposes analytics metrics via a RESTful HTTP API.

---

## Features

- Ingests the Enron email dataset
- Tracks and exposes message count and top senders
- REST API with Swagger UI documentation
- Unit tested & Dockerized

---

## Tech Stack

- Java 17+
- Spring Boot 3.5.x
- Maven
- Swagger (Springdoc OpenAPI)
- Docker

---

## Project Structure

```
.
├── src/
│   └── main/java/org/enronprocessor/enronprocessor/
│       └── EnronProcessorApplication.java
│       └── controller/
│       └── service/
├── enron_mail_20150507/
│   └── maildir/                 
├── Dockerfile
├── pom.xml
└── README.md
```

---

## Setup Instructions

### 1. Clone and Download

```bash
git clone https://github.com/vahidpi/EnronProcessor.git
cd EnronProcessor
```

### 2. Download and Extract Dataset

Download from [here](https://www.cs.cmu.edu/~enron/enron_mail_20150507.tar.gz) and extract:

```bash
tar -xvzf enron_mail_20150507.tar.gz
mv enron_mail_20150507 ./enron_mail_20150507
```

Ensure this directory is in the project root:
```
enron_mail_20150507/maildir
```

---

### 3. Build and Run (Locally)

```bash
./mvnw spring-boot:run
```

---

### 4. Run with Docker

#### Build the image:
```bash
docker build -t enron-processor .
```

#### Run the container:
```bash
docker run -p 8080:8080 -v "$PWD/enron_mail_20150507:/app/enron_mail_20150507" enron-processor
```

> Note: The dataset must be mounted into the container under `/app/enron_mail_20150507`.

---

## API Endpoints

| Method | Endpoint         | Description                           |
|--------|------------------|---------------------------------------|
| POST   | `/start`         | Starts ingesting the dataset          |
| GET    | `/status`        | Shows ingestion status & message count |
| GET    | `/top-senders`   | Returns top 10 email senders          |
| GET    | `/swagger-ui.html` | Access Swagger API documentation    |

---

## Swagger UI

Once the service is running, access:

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Testing

Run unit tests using:

```bash
./mvnw test
```

