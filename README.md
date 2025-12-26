# Apnea Detector

A Java application that analyzes WAV audio files to detect breathing pauses and classify them as normal or potential sleep apnea events.

## Features

- Detects silence/breathing pauses in WAV audio files using RMS-based analysis
- Classifies pauses as NORMAL or APNEA based on configurable duration thresholds
- Supports both CLI batch processing and REST API modes
- Batch processing via CSV input files
- CSV output with detailed pause information

## Requirements

- **Java 21** or later (LTS)
- **Maven 3.8+**

## Building

```bash
./mvnw clean package
```

## Running

### REST API Mode (default)

Start the server without arguments:

```bash
java -jar target/apnea-1.0.0-SNAPSHOT.jar
```

The server starts on port 8080 by default.

### CLI Batch Processing Mode

```bash
java -jar target/apnea-1.0.0-SNAPSHOT.jar <input.csv> <output.csv>
```

## REST API

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/analyze` | Upload WAV file for analysis |
| GET | `/api/health` | Health check endpoint |

### POST /api/analyze

Upload a WAV file for apnea detection analysis.

**Request:**
```bash
curl -X POST http://localhost:8080/api/analyze \
  -F "file=@recording.wav"
```

**Response:**
```json
{
  "filename": "recording.wav",
  "totalPauses": 5,
  "apneaCount": 2,
  "normalCount": 3,
  "pauses": [
    {
      "index": 1,
      "start": 2.5,
      "end": 3.0,
      "duration": 0.5,
      "type": "NORMAL"
    },
    {
      "index": 2,
      "start": 15.0,
      "end": 20.5,
      "duration": 5.5,
      "type": "APNEA"
    }
  ]
}
```

## CLI Input/Output Formats

### Input CSV Format

```csv
filepath
/path/to/recording1.wav
/path/to/recording2.wav
```

### Output CSV Format

```csv
File Path,Pause #,start [secs],end [secs],duration [secs],type
recording1.wav,1,2.5,3.0,0.5,NORMAL
recording1.wav,2,15.0,20.5,5.5,APNEA
```

## Configuration

Application properties can be configured in `application.properties`:

```properties
# Server port
server.port=8080

# Silence detection threshold (RMS value, lower = more sensitive)
apnea.silence.checker.threshold=0.00001

# Apnea classification threshold in seconds
apnea.classification.threshold=4.5

# File upload limits
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

## Architecture

The application uses Spring Boot with constructor-based dependency injection:

- **ApneaDetectorService** - Main orchestrator for batch CSV processing
- **AudioFileSilenceDetectorService** - Detects silence periods in WAV files
- **BreathingPauseClassificationService** - Classifies pauses as normal/apnea
- **SilenceCheckerService** - RMS-based silence detection
- **BreathingPauseOutputWriter** - CSV output generation
- **ApneaController** - REST API endpoints

### Domain Model

- `BreathingPause` - Immutable record representing a detected pause
- `BreathingPauseType` - Enum: NOT_SET, NORMAL, APNEA
- `WavFile` - Interface for WAV file operations

## Development

### Technology Stack

- Java 21 (LTS)
- Spring Boot 3.4.x
- Apache Commons CSV
- JUnit 5 with Mockito

### Running Tests

```bash
./mvnw test
```

### Code Style

This project uses modern Java features:
- Records for immutable data classes
- `var` for local type inference
- Stream API for collection processing
- Text blocks for multi-line strings
- Pattern matching where applicable

## Example Files

Sample WAV files are included in the `examples/` directory (gzipped).

## Credits

- WAV file parsing based on [Java Wav File IO](http://www.labbookpages.co.uk/audio/javaWavFiles.html)

## License

MIT License
