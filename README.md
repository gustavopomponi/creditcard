# Credit Card Management API

A PCI DSS compliant Spring Boot REST API for secure credit card management with JWT authentication.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Security & Compliance](#security--compliance)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [File Formats](#file-formats)
- [PCI DSS Compliance](#pci-dss-compliance)
- [Contributing](#contributing)
- [License](#license)

## ✨ Features

- 🔐 **JWT Authentication** - Secure token-based authentication
- 💳 **Credit Card Management** - CRUD operations for credit cards
- 🔒 **AES-256-GCM Encryption** - Industry-standard encryption for PAN storage
- 📁 **Batch Upload** - Support for TXT
- 🔍 **Card Search** - Search by full card number
- 📝 **Audit Logging** - Complete audit trail for all operations
- 🎭 **PAN Masking** - PCI DSS compliant display masking
- 🚫 **CVV Never Stored** - Strict PCI DSS compliance

## 🛠️ Tech Stack

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database operations
- **Docker compose mysql** - Mysql container (development)
- **JWT (jjwt)** - Token generation and validation
- **Jackson** - JSON processing
- **Lombok** - Reduce boilerplate code
- **Maven** - Dependency management

## 🔒 Security & Compliance

### PCI DSS Compliance

This API implements the following PCI DSS requirements:

| Requirement | Implementation |
|-------------|----------------|
| **3.2** | CVV/CVC never stored after authorization |
| **3.3** | PAN masked when displayed (show first 6 and last 4) |
| **3.4** | PAN encrypted at rest using AES-256-GCM |
| **4.1** | Ready for TLS 1.2+ transmission encryption |
| **7.1** | Access restricted by business need-to-know |
| **8.1** | Unique user ID via JWT authentication |
| **10.2** | Audit trail for all cardholder data access |

### What Can Be Stored

✅ **Allowed:**
- Primary Account Number (PAN) - **Encrypted**
- Cardholder Name
- Expiration Date
- Service Code

❌ **Prohibited (Never Stored):**
- CVV/CVC/CVV2/CID
- Full magnetic stripe data
- PIN or PIN block

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git
- Docker

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/gustavopomponi/creditcard.git
   cd creditcard
   ```

2. **Generate encryption key**
   ```bash
   mvn compile exec:java
   ```
   
   Copy the generated key and set it as an environment variable (LINUX):
   ```bash
   export ENCRYPTION_KEY="your_generated_key_here"
   ```
   Copy the generated key and set it as an environment variable (WINDOWS):
   ```bash
   $env:ENCRYPTION_KEY = "your_generated_key_here"
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The API will start on `https://localhost:8443`

   The application uses Docker Compose. Once the application is started, a container will be created
   to support MySql Database. Database and all tables will be created.


### Configuration

Update `src/main/resources/application.yml`:

```yaml
spring:
    datasource:
    url: jdbc:mysql://mysql:3306/db_creditcard
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: senha2026
  
encryption:
  key: ${ENCRYPTION_KEY}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000 # 24 hours

server:
  port: 8443
  ssl:
    key-store: classpath:certificate/springboot.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: springboot
  http2:
    enabled: true
```

## 📚 API Documentation

### Base URL

```
https://localhost:8443/api/v1
```

### Authentication

All endpoints (except `/auth/login`) require JWT authentication.

**Header:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```
```

---

## 🔑 Authentication Endpoints

### 1. Login

Authenticate user and receive JWT token.

**Endpoint:** `POST /api/v1/auth/login`

**Request Body:**
```json
{
  "username": "user",
  "password": "password"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Response:** `401 Unauthorized`
```json
{
  "error": "Incorrect username or password"
}
```

** For testing purposes, in-memory user was created in UserConfig file.

---

## 💳 Credit Card Endpoints

### 2. Add Credit Card

Store a new credit card securely.

**Endpoint:** `POST /api/v1/credit-cards`

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "cardNumber": "4111111111111111"
}
```

**Parameters:**

| Field | Type | Required | Description | Validation |
|-------|------|----------|-------------|------------|
| cardNumber | String | ✅ | Full card number | 13-19 digits |


**Response:** `201 Created`
```json
{
    "id": "22146b94-1931-450a-9688-0324d9e5bd63"
}
```

**Error Responses:**

- `400 Bad Request` - Invalid card data
  ```json
  {
    "error": "Invalid card number"
  }
  ```

- `401 Unauthorized` - Missing or invalid token
  ```json
  {
    "error": "Unauthorized"
  }
  ```

---


### 6. Search Card by Full Number

Search for a card using the full card number.

**Endpoint:** `POST /api/v1/credit-cards/search`

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "cardNumber": "4111111111111111"
}
```

**Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| cardNumber | String | ✅ | Full card number (13-19 digits) |

**Response:** `200 OK`
```json
{
  "id": "22146b94-1931-450a-9688-0324d9e5bd63"
}
```

**Error Responses:**

- `404 Not Found` - Card not found
  ```json
  {
    "error": "Card not found",
    "message": "No card found with the provided number"
  }
  ```

- `400 Bad Request` - Invalid card number format
  ```json
  {
    "error": "Invalid card number format"
  }
  ```

---

### 7. Batch Upload Cards

Upload multiple credit cards from a file.

**Endpoint:** `POST /api/v1/credit-cards/batch-upload`

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data
```

**Form Data:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| file | File | ✅ | Credit card file (TXT) |
| fileType | String | ✅ | File format: `txt` |

**Supported Formats:**

#### TXT Format
```txt
4111111111111111
5555555555554444
```


**Response:** `200 OK`
```json
{
  "totalRecords": 5,
  "successCount": 4,
  "failureCount": 1,
  "results": [
    {
      "lineNumber": 1,
      "status": "SUCCESS",
      "maskedPan": "411111******1111",
      "errorMessage": null,
      "cardId": 1
    },
    {
      "lineNumber": 2,
      "status": "FAILURE",
      "cardHolderName": "Invalid Card",
      "errorMessage": "Invalid card number"
    },
    {
      "lineNumber": 3,
      "status": "SUCCESS",
      "maskedPan": "555555******4444",
      "cardHolderName": "Jane Smith",
      "cardId": 2
    }
  ],
  "processingTimeMs": 1234,
  "message": "Processed 5 cards: 4 successful, 1 failed"
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| totalRecords | Integer | Total number of cards in file |
| successCount | Integer | Number of successfully saved cards |
| failureCount | Integer | Number of failed cards |
| results | Array | Detailed result for each card |
| processingTimeMs | Long | Processing time in milliseconds |
| message | String | Summary message |

**Result Object:**

| Field | Type | Description |
|-------|------|-------------|
| lineNumber | Integer | Line number in file |
| status | String | `SUCCESS` or `FAILURE` |
| maskedPan | String | Masked card number (on success) |
| cardHolderName | String | Card holder name |
| cardId | Long | Saved card ID (on success) |
| errorMessage | String | Error description (on failure) |

**Error Responses:**

- `400 Bad Request` - Invalid file type or empty file
  ```json
  {
    "error": "Invalid file type. Supported types: txt, csv, json"
  }
  ```

**Example cURL:**
```bash
curl -X POST https://localhost:8443/api/v1/credit-cards/batch-upload \
  -H "Authorization: Bearer {token}" \
  -F "file=@cards.txt" \
  -F "fileType=txt"
```

---

## 📁 File Formats

### TXT Format

**Format:**
```
cardNumber
```

**Features:**
- Comments supported (lines starting with `#` or `//`)
- Empty lines ignored
- Service code optional

**Example:**
```txt
DESAFIO-HYPERATIVA           20180524LOTE0001000010   // [01-29]NOME   [30-37]DATA   [38-45]LOTE   [46-51]QTD DE REGISTROS
C2     4456897999999999                               // [01-01]IDENTIFICADOR DA LINHA   [02-07]NUMERAÇÃO NO LOTE   [08-26]NÚMERO DE CARTAO COMPLETO
C1     4456897922969999                               // OBS. ORIENTACAO NÚMERICA A ESQUERDA E ARQUIVO INTEIRO COMPLETADO COM ESPAÇOS ATÉ COLUNA 51
C3     4456897999999999
C4     4456897998199999
C10    4456897919999999
LOTE0001000010                   
```

### Example Requests

**1. Login and Store Card:**
```bash
# Login
TOKEN=$(curl -s -X POST https://localhost:8443/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}' \
  | jq -r '.token')

# Add Card
curl -X POST https://localhost:8443/api/v1/credit-cards \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4111111111111111"
  }'
```

**2. Batch Upload:**
```bash
curl -X POST https://localhost:8443/api/v1/credit-cards/batch-upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@cards.txt" \
  -F "fileType=txt"
```

**3. Search Card:**
```bash
curl -X POST https://localhost:8443/api/v1/credit-cards/search \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":"4111111111111111"}'
```

---

## 🔐 PCI DSS Compliance

### Implemented Requirements

#### ✅ Requirement 3: Protect Stored Cardholder Data
- **3.1**: Minimize cardholder data storage (soft delete)
- **3.2**: Do not store sensitive authentication data after authorization (CVV never stored)
- **3.3**: Mask PAN when displayed (first 6 + last 4)
- **3.4**: Render PAN unreadable (AES-256-GCM encryption)

#### ✅ Requirement 4: Encrypt Transmission
- **4.1**: Use strong cryptography (TLS 1.2+ ready)

#### ✅ Requirement 7: Restrict Access
- **7.1**: Restrict access by business need-to-know (JWT authentication)

#### ✅ Requirement 8: Identify Users
- **8.1**: Assign unique ID to each user

#### ✅ Requirement 10: Track Access
- **10.1**: Implement audit trails
- **10.2**: Automated audit trail for cardholder data access

### Production Requirements

⚠️ **Additional requirements for production:**

- [ ] Enable HTTPS/TLS 1.2+
- [ ] Move encryption keys to HSM or secure vault (AWS KMS, Azure Key Vault)
- [ ] Implement key rotation
- [ ] Set up network segmentation
- [ ] Configure firewall rules
- [ ] Implement rate limiting
- [ ] Set up monitoring and alerting
- [ ] Complete annual PCI DSS assessment
- [ ] Conduct quarterly vulnerability scans

---

## 📊 Database Schema

### credit_cards Table

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | BIGINT | No | Primary key |
| user_id | BIGINT | No | Owner user ID |
| first_six_digits | VARCHAR(6) | No | BIN (for display) |
| last_four_digits | VARCHAR(4) | No | Last 4 digits (for display) |
| encrypted_pan | TEXT | No | AES-256 encrypted PAN |
| pan_hash | VARCHAR(64) | No | SHA-256 hash for searching |
| card_holder_name | VARCHAR(100) | No | Name on card |
| expiry_month | INTEGER | No | Expiration month (1-12) |
| expiry_year | INTEGER | No | Expiration year |
| service_code | VARCHAR(3) | Yes | Service code |
| card_brand | VARCHAR(20) | Yes | VISA, MASTERCARD, etc. |
| is_active | BOOLEAN | No | Active status |
| created_at | TIMESTAMP | No | Creation timestamp |
| updated_at | TIMESTAMP | Yes | Last update timestamp |
| last_used_at | TIMESTAMP | Yes | Last usage timestamp |

**Indexes:**
- `idx_user_id` on `user_id`
- `idx_pan_hash` on `pan_hash`
- `idx_last_four` on `last_four_digits`

### audit_logs Table

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | BIGINT | No | Primary key |
| user_id | BIGINT | No | User who performed action |
| action | VARCHAR(50) | No | Action type |
| resource_type | VARCHAR(50) | Yes | Resource type (CREDIT_CARD) |
| resource_id | VARCHAR(100) | Yes | Resource identifier |
| ip_address | VARCHAR(45) | Yes | Client IP address |
| timestamp | TIMESTAMP | No | Action timestamp |

**Indexes:**
- `idx_user_id` on `user_id`
- `idx_timestamp` on `timestamp`

---

## 🏗️ Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTPS (TLS 1.2+)
       │
┌──────▼──────────────────────────────────┐
│         Spring Boot Application          │
│  ┌────────────────────────────────────┐ │
│  │   Security Layer (JWT Filter)      │ │
│  └───��────────┬────────────────────┘ │
│               │                         │
│  ┌────────────▼───────────────────────┐ │
│  │      Controllers                   │ │
│  │  - AuthController                  │ │
│  │  - CreditCardController            │ │
│  └────────────┬───────────────────────┘ │
│               │                         │
│  ┌────────────▼───────────────────────┐ │
│  │      Services                      │ │
│  │  - CreditCardService               │ │
│  │  - EncryptionService (AES-256)     │ │
│  │  - AuditLogService                 │ │
│  │  - BatchUploadService              │ │
│  └────────────┬───────────────────────┘ │
│               │                         │
│  ┌────────────▼───────────────────────┐ │
│  │      Repositories (JPA)            │ │
│  └────────────┬───────────────────────┘ │
└───────────────┼─────────────────────────┘
                │
       ┌────────▼────────┐
       │    Database     │
       │  (MySql)        │
       └─────────────────┘
```

---

## 🔧 Configuration

### Environment Variables

| Variable | Required | Description | Example |
|----------|----------|-------------|---------|
| ENCRYPTION_KEY | ✅ | AES-256 encryption key (Base64) | `kW8jP3mZ9xN2vB7n...` |
| JWT_SECRET | ✅ | JWT signing secret | `mySecretKey123...` |
| DATABASE_URL | ✅ | Database connection URL | `jdbc:postgresql://...` |
| DATABASE_USERNAME | ✅ | Database username | `dbuser` |
| DATABASE_PASSWORD | ✅ | Database password | `dbpass` |

### application.yml

```yaml
spring:
  application:
    name: credit-card-api
  
  datasource:
    url: ${DATABASE_URL:jdbc:h2:mem:creditcard_db}
    username: ${DATABASE_USERNAME:sa}
    password: ${DATABASE_PASSWORD:}
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

encryption:
  key: ${ENCRYPTION_KEY}

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000 # 24 hours

server:
  port: 8443
  ssl:
    key-store: classpath:certificate/springboot.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: springboot
  http2:
    enabled: true
```

---

## 📝 API Response Codes

| Status Code | Description |
|-------------|-------------|
| 200 OK | Request successful |
| 201 Created | Resource created successfully |
| 400 Bad Request | Invalid request data |
| 401 Unauthorized | Missing or invalid authentication |
| 403 Forbidden | User doesn't have permission |
| 404 Not Found | Resource not found |
| 500 Internal Server Error | Server error |

---

## 🐛 Error Handling

All errors follow a consistent format:

```json
{
  "error": "Error type",
  "message": "Detailed error message",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Common Error Responses:**

**Validation Error:**
```json
{
  "error": "Validation failed",
  "message": "Invalid card number format",
  "field": "cardNumber"
}
```

**Authentication Error:**
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

**Not Found Error:**
```json
{
  "error": "Card not found",
  "message": "No card found with ID: 123"
}
```

---


## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## ⚠️ Disclaimer

This is a demonstration project for educational purposes. For production use:

1. Complete full PCI DSS assessment
2. Use external key management service (HSM/KMS)
3. Enable HTTPS/TLS 1.2+
4. Implement comprehensive monitoring
5. Conduct regular security audits
6. Consider using payment processor APIs (Stripe, PayPal) instead of storing cards

---

## 📞 Contact

gustavopomponi - [@gustavopomponi](https://twitter.com/gustavopomponi)

Project Link: [https://github.com/yourusername/credit-card-api](https://github.com/yourusername/credit-card-api)

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [PCI Security Standards Council](https://www.pcisecuritystandards.org/)
- [jjwt](https://github.com/jwtk/jjwt)
