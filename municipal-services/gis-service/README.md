# GIS Service

A Spring Boot microservice for processing KML polygon files and extracting geographic zone information through WFS (Web Feature Service) integration.

## Overview

The GIS Service processes KML polygon files to determine district and zone information by querying external WFS endpoints. It provides RESTful APIs for uploading KML files, extracting polygon geometries, and returning geographic metadata with comprehensive audit logging.

## Features

- **KML File Processing**: Parse KML files and extract polygon geometries using JTS (Java Topology Suite)
- **WFS Integration**: Query Web Feature Service endpoints for spatial intersections using CQL filters
- **Filestore Integration**: Upload and store KML files through eGov filestore service
- **Audit Logging**: Complete operation tracking with PostgreSQL JSONB storage
- **Zone Detection**: Extract district and zone information from WFS responses
- **Error Handling**: Robust error handling with retry mechanisms and fallback responses
- **Multi-tenant Support**: Tenant-aware operations for multi-tenant deployments

## Technology Stack

- **Java**: 1.8
- **Spring Boot**: 2.2.6.RELEASE
- **Spring WebFlux**: Reactive HTTP client for WFS operations
- **Spring Data JPA**: Database operations with PostgreSQL
- **JTS**: 1.17.1 for geometry operations
- **PostgreSQL**: Database with JSONB support for audit details
- **Flyway**: Database migration management
- **Lombok**: Code generation and boilerplate reduction
- **Jackson**: JSON processing and serialization
- **Hibernate Types**: Enhanced PostgreSQL JSONB support
- **SpringFox Swagger**: API documentation

## API Endpoints

### POST `/gis-service/find-zone`

Find zone information from a polygon KML file.

**Request:**
- **Content-Type**: `multipart/form-data`
- **Parameters**:
  - `file` (required): KML polygon file
  - `gisRequestWrapper` (required): JSON containing RequestInfo and GIS request data

**Request Structure:**
```json
{
  "RequestInfo": {
    "apiId": "gis-api",
    "userInfo": {
      "uuid": "user-uuid",
      "tenantId": "tenant-id"
    }
  },
  "gisRequest": {
    "tenantId": "pg.citya",
    "applicationNo": "APP-123",
    "rtpiId": "RTPI-456"
  }
}
```

**Response:**
```json
{
  "ResponseInfo": {
    "apiId": "gis-api",
    "ts": 1695123456789,
    "status": "SUCCESSFUL"
  },
  "district": "Texas",
  "zone": "TX",
  "wfsResponse": {
    "type": "FeatureCollection",
    "features": [
      {
        "type": "Feature",
        "properties": {
          "STATE_NAME": "Texas",
          "STATE_ABBR": "TX",
          "STATE_FIPS": "48"
        }
      }
    ]
  },
  "fileStoreId": "file-store-id"
}
```

## Configuration

### Application Properties

```properties
# Server Configuration
server.port=8081
server.context-path=/gis-service
server.servlet.context-path=/gis-service

# GIS Service Configuration
gis.wfs-url=https://ahocevar.com/geoserver/wfs
gis.wfs-type-name=topp:states
gis.wfs-geometry-column=the_geom
gis.wfs-district-attribute=STATE_NAME
gis.wfs-zone-attribute=STATE_ABBR

# Filestore Configuration
egov.filestore.host=http://localhost:8083
gis.filestoreEndpoint=/filestore/v1/files

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/master_db
spring.datasource.username=postgres
spring.datasource.password=root

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Database Schema

### ug_gis_log Table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| application_no | VARCHAR(128) | Application number for tracking |
| rtpi_id | VARCHAR(128) | RTPI identifier |
| file_store_id | VARCHAR(512) | Reference to stored file |
| tenant_id | VARCHAR(128) | Tenant identifier |
| status | VARCHAR(50) | Processing status (PENDING/SUCCESS/FAILURE) |
| response_status | VARCHAR(50) | Response outcome status |
| response_json | TEXT | Processing response message |
| createdby | VARCHAR(128) | User UUID who created the record |
| createdtime | BIGINT | Creation timestamp (epoch milliseconds) |
| lastmodifiedby | VARCHAR(128) | User UUID who last modified |
| lastmodifiedtime | BIGINT | Last modification timestamp (epoch milliseconds) |
| details | JSONB | Additional processing details and metadata |

## Architecture

### Core Components

1. **GisController**: REST API controller handling HTTP requests
2. **GisService**: Business logic for polygon processing and zone detection
3. **FilestoreClient**: Client for file storage operations
4. **WfsClient**: Client for Web Feature Service operations
5. **KmlParser**: Utility for parsing KML files and extracting geometries
6. **GisLog**: JPA entity for audit logging and operation tracking

### Processing Flow

1. **File Validation**: Validate KML file type, size, and format
2. **Filestore Upload**: Store KML file in eGov filestore service
3. **Audit Logging**: Create initial log entry with PENDING status
4. **KML Parsing**: Extract polygon geometry using JTS library
5. **WFS Query**: Query WFS service for intersecting features
6. **Zone Extraction**: Extract district and zone from WFS response
7. **Response Cleanup**: Remove unnecessary demographic and geometry data
8. **Audit Update**: Update log entry with final status and results
9. **Response Return**: Return cleaned response to client

### WFS Integration

The service performs spatial intersection queries using CQL filters:

```sql
INTERSECTS(the_geom, POLYGON((coordinates...)))
```

**Query Parameters:**
- `service=WFS`
- `version=2.0.0`
- `request=GetFeature`
- `typeName=topp:states`
- `outputFormat=application/json`
- `srsName=EPSG:4326`

## Development

### Prerequisites

- Java 1.8
- Maven 3.6+
- PostgreSQL 12+
- Access to eGov filestore service

### Running the Application

1. Configure database connection in `application.properties`
2. Start the application: `mvn spring-boot:run`
3. Access Swagger UI: `http://localhost:8081/gis-service/swagger-ui.html`

### Testing

Use the provided test KML files in `src/main/resources/`:
- `test-polygon.kml`
- `test-polygon_2.kml`

### Sample cURL Request

```bash
curl --location 'http://localhost:8081/gis-service/find-zone' \
--form 'file=@"test-polygon.kml"' \
--form 'gisRequestWrapper={"RequestInfo":{"apiId":"gis-api","userInfo":{"uuid":"test-user"}},"gisRequest":{"tenantId":"pg","applicationNo":"XYZ-123","rtpiId":"ABC-123"}}'
```

## API Documentation

Swagger UI is available at: `http://localhost:8081/gis-service/swagger-ui.html`

## Logging

The service provides comprehensive logging at INFO level for:
- File upload operations
- KML parsing activities
- WFS query execution
- Zone extraction results
- Error conditions and exceptions

Configure logging level in `application.properties`:
```properties
logging.level.org.upyog.gis=INFO
```