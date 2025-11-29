# ML Clustering Service

FastAPI microservice for semantic text clustering using rubert-tiny2 embeddings.

## Installation

```bash
pip install -r requirements.txt
```

## Running

```bash
python main.py
```

Or with uvicorn directly:

```bash
uvicorn main:app --host 0.0.0.0 --port 8000
```

## API Endpoints

### POST /cluster_keys

Clusters synonymous keys using semantic embeddings.

**Request:**
```json
{
  "keys": ["Width", "Weight", "Width (mm)", "Mass"],
  "threshold": 0.4
}
```

**Response:**
```json
{
  "clusters": [
    ["Width", "Width (mm)"],
    ["Weight", "Mass"]
  ]
}
```

### GET /health

Health check endpoint.

## Model

Uses `cointegrated/rubert-tiny2` - a lightweight Russian BERT model optimized for embeddings.

