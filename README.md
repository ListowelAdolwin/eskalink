# Eskalink

A comprehensive RESTful API for job posting and application management system built with Java Spring Boot.

## Features

- **User Management**: Role-based authentication (Company/Applicant) with email verification
- **Job Management**: Companies can create, update, delete, and manage job postings
- **Application Management**: Applicants can browse jobs, apply, and track their applications
- **File Upload**: Resume upload to AWS S3 with validation
- **Email Notifications**: Automated emails for verification and application updates
- **Advanced Search**: Filtering, pagination, and sorting on job listings
- **Status Management**: Application status tracking with workflow validation

## Technology Stack

- **Framework**: Spring Boot
- **Language**: Java
- **Database**: MySQL (Production), H2 (Testing)
- **Security**: Spring Security with JWT
- **File Storage**: AWS S3
- **Email**: Spring Mail with SMTP
- **Documentation**: OpenAPI (Swagger)
- **Build Tool**: Maven
- **Libraries**:
    - Spring Data JPA
    - Lombok (boilerplate reduction)
    - MapStruct (DTO-Entity mapping)
    - Validation API
    - Jackson (JSON processing)

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+ (for production)
- AWS Account (for S3 file upload)
- SMTP Email Account (Gmail recommended)

## Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/ListowelAdolwin/eskalink.git
cd eskalink
```

### 2. Environment Variables

Create an `application-dev.properties` file in `src/main/resources/` or set the following environment variables:

```properties
# Database Configuration
DATABASE_URL=jdbc:mysql://localhost:3306/eskalink_db
DATABASE_USERNAME=root
DATABASE_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your-super-secret-key-minimum-256-bits-long

# Email Configuration (Gmail example)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# AWS S3 Configuration
AWS_ACCESS_KEY=your-aws-access-key
AWS_SECRET_KEY=your-aws-secret-key
AWS_REGION=your-aws-region
S3_BUCKET_NAME=your-s3-bucket-name

# Application URLs
APP_BASE_URL=http://localhost:8080
FRONTEND_URL=http://localhost:3000
```

### 3. Database Setup

Create MySQL database:
```sql
CREATE DATABASE eskalate_db;
```

### 4. Build and Run

```bash
# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

##  API Documentation

### Swagger UI
Access the interactive API documentation at: `http://localhost:8080/swagger-ui.html`

### API Endpoints Overview

#### Authentication
- `POST /api/auth/signup` - User registration
- `GET /api/auth/verify-email` - Email verification
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user info

#### Jobs (Company Only)
- `POST /api/jobs` - Create job
- `PUT /api/jobs/{id}` - Update job
- `DELETE /api/jobs/{id}` - Delete job
- `GET /api/jobs/my-jobs` - Get company's jobs

#### Jobs (General)
- `GET /api/jobs/search` - Browse/search jobs (Applicant)
- `GET /api/jobs/{id}` - Get job details

#### Applications
- `POST /api/applications` - Apply for job (Applicant)
- `GET /api/applications/my-applications` - Track applications (Applicant)
- `GET /api/applications/job/{jobId}` - View job applications (Company)
- `PUT /api/applications/{id}/status` - Update application status (Company)

## Project Architecture

### Modular Monolithic Structure
```
src/main/java/com/a2sv/eskalate/
├── application/          # Application domain
│   ├── controller/      # REST controllers
│   ├── dto/            # Data transfer objects
│   ├── entity/         # JPA entities
│   ├── enums/          # Status enums
│   ├── mapper/         # MapStruct mappers
│   ├── repository/     # Data repositories
│   └── service/        # Business logic
├── common/              # Shared components
│   ├── dto/            # Common DTOs
│   └── exception/      # Exception handling
├── config/              # Configuration classes              # File upload service
├── job/                # Job domain
├── security/           # Security components
├── user/               # User domain
└── scheduled/          # Scheduled tasks
```

### Key Design Patterns
- **Domain-Driven Design**: Organized by business domains
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: API data contracts
- **Mapper Pattern**: Object transformation
- **Builder Pattern**: Object construction
- **Strategy Pattern**: Status validation

## Security Features

- **JWT Authentication**: Stateless authentication
- **Role-based Authorization**: Company vs Applicant permissions
- **Password Security**: BCrypt hashing
- **Input Validation**: Comprehensive request validation
- **CORS Protection**: Cross-origin request handling
- **SQL Injection Protection**: JPA/Hibernate ORM

## Email System

- **Welcome Emails**: Account verification
- **Application Notifications**: New application alerts
- **Status Updates**: Interview, rejection, and hiring notifications
- **Token Management**: Time-limited verification tokens

## Database Schema

### Core Entities
- **Users**: Authentication and profile data
- **Jobs**: Job postings with status management
- **Applications**: Job applications with resume links

### Key Relationships
- User (1) → (N) Jobs (created_by)
- User (1) → (N) Applications (applicant)
- Job (1) → (N) Applications
- Unique constraint: (applicant_id, job_id)

## Testing

Run tests with:
```bash
mvn test
```

### Test Coverage
- Unit tests for controllers with mocked services
- Integration tests with H2 in-memory database
- Validation testing for all DTOs
- Security testing for authorization

## Deployment

### Environment Preparation
1. Set up production database (MySQL)
2. Configure AWS S3 bucket
3. Set up SMTP email service
4. Generate strong JWT secret

## Non-Functional Implementations

### Performance & Scalability
- **Lazy Loading**: JPA lazy fetching strategies
- **Pagination**: Efficient large dataset handling

### Security Enhancements
- **Rate Limiting**: Request throttling (can be implemented with Spring Cloud Gateway)
- **Input Sanitization**: XSS protection
- **HTTPS Only**: SSL/TLS enforcement in production
- **Security Headers**: CSRF, XSS, and other security headers

### Monitoring & Logging
- **Structured Logging**: JSON formatted logs
- **Request Tracing**: Correlation IDs
- **Health Checks**: Spring Boot Actuator endpoints
- **Metrics**: Application performance monitoring

##  Error Handling

Comprehensive error handling with consistent response format:
- **Validation Errors**: Field-level validation messages
- **Authentication Errors**: JWT token validation
- **Authorization Errors**: Role-based access control
- **Business Logic Errors**: Domain-specific validations
- **System Errors**: Internal server error handling

## API Response Format

### Success Response
```json
{
    "success": true,
    "message": "Operation completed successfully",
    "object": { ... },
    "errors": null
}
```

### Paginated Response
```json
{
    "success": true,
    "message": "Data retrieved successfully",
    "object": [...],
    "pageNumber": 1,
    "pageSize": 10,
    "totalSize": 100,
    "errors": null
}
```

### Error Response
```json
{
    "success": false,
    "message": "Operation failed",
    "object": null,
    "errors": ["Detailed error message"]
}
```

## Author

**Listowel Adolwin Moro**
- GitHub: [@ListowelAdolwin](https://github.com/ListowelAdolwin)
- LinkedIn: [Listowel Adolwin Moro](https://www.linkedin.com/in/listowel-adolwin)