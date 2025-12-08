# PC Builder+ Backend

Backend API for the PC Builder+ application - A comprehensive PC component selection and compatibility checking system.

## Features

- Component catalog management
- Build creation and validation
- Compatibility checking
- Price tracking
- Component filtering and search
- Automatic data scraping from PCPartPicker

## Tech Stack

- **Java 17+** with Spring Boot
- **PostgreSQL** (Neon Database)
- **Maven** for dependency management
- **Python** for data import scripts

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database (or Neon account)
- Python 3.x (for dataset import scripts)

### Environment Configuration

1. Copy the `.env.example` file to `.env`:

   ```bash
   cp .env.example .env
   ```

2. Fill in your actual credentials in the `.env` file:

   - **Database credentials**: Get these from your PostgreSQL/Neon database

3. The application will automatically load environment variables from `.env` file.

### Running the Application

1. Install dependencies:

   ```bash
   mvn clean install
   ```

2. Run the application:

   ```bash
   mvn spring-boot:run
   ```

   The server will start on `http://localhost:8080` (or the port specified in `SERVER_PORT`).

### Database Setup

1. The application will automatically create the necessary tables using the `schema.sql` file.

2. To import the dataset from CSV files:
   ```bash
   cd dataset
   pip install psycopg2-binary python-slugify
   python ../dataset-script.py --dsn "postgresql://user:password@host:port/database"
   ```

## API Endpoints

### Components

- `GET /api/components` - List all components with filtering options
- `GET /api/components/{id}` - Get component by ID
- `GET /api/components/category/{category}` - Get components by category
- `GET /api/components/search` - Search components by name

### Builds

- `POST /api/builds` - Create a new build
- `GET /api/builds/{id}` - Get build by ID
- `PUT /api/builds/{id}` - Update build
- `DELETE /api/builds/{id}` - Delete build

## Configuration

Key configuration properties in `application.properties`:

- **Storage**: File storage locations for data and logs
- **Scraper**: Web scraping settings for data collection
- **Database**: PostgreSQL connection settings

All sensitive credentials are managed through environment variables.

## Security Notes

- Never commit the `.env` file to version control
- Keep your database credentials and API keys secure
- Use environment variables for all sensitive configuration

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

[Add your license here]
