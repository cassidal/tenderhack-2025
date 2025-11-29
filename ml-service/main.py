"""
FastAPI Microservice for Semantic Text Clustering
Uses rubert-tiny2 embeddings for grouping synonymous CSV headers
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.cluster import AgglomerativeClustering
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="ML Clustering Service", version="1.0.0")

# Global model instance (loaded once)
_model = None


def get_model():
    """Lazy loading of the embedding model"""
    global _model
    if _model is None:
        logger.info("Loading rubert-tiny2 model...")
        _model = SentenceTransformer('cointegrated/rubert-tiny2')
        logger.info("Model loaded successfully")
    return _model


class ClusterRequest(BaseModel):
    keys: List[str]
    threshold: float = 0.4


class ClusterResponse(BaseModel):
    clusters: List[List[str]]


@app.get("/health")
async def health():
    """Health check endpoint"""
    return {"status": "healthy", "model_loaded": _model is not None}


@app.post("/cluster_keys", response_model=ClusterResponse)
async def cluster_keys(request: ClusterRequest):
    """
    Cluster synonymous keys using semantic embeddings
    
    Args:
        request: ClusterRequest with keys and threshold
        
    Returns:
        ClusterResponse with list of clusters
    """
    try:
        if not request.keys:
            raise HTTPException(status_code=400, detail="Keys list cannot be empty")
        
        if request.threshold < 0 or request.threshold > 1:
            raise HTTPException(status_code=400, detail="Threshold must be between 0 and 1")
        
        logger.info(f"Clustering {len(request.keys)} keys with threshold {request.threshold}")
        
        # Load model
        model = get_model()
        
        # Generate embeddings
        logger.info("Generating embeddings...")
        embeddings = model.encode(request.keys, convert_to_numpy=True, show_progress_bar=False)
        
        # Perform clustering
        logger.info("Performing clustering...")
        if len(request.keys) == 1:
            # Single key, return as single cluster
            clusters = [[request.keys[0]]]
        else:
            clustering = AgglomerativeClustering(
                metric='cosine',
                linkage='average',
                distance_threshold=request.threshold,
                n_clusters=None
            )
            cluster_labels = clustering.fit_predict(embeddings)
            
            # Group keys by cluster labels
            clusters_dict = {}
            for idx, label in enumerate(cluster_labels):
                if label not in clusters_dict:
                    clusters_dict[label] = []
                clusters_dict[label].append(request.keys[idx])
            
            clusters = list(clusters_dict.values())
        
        logger.info(f"Found {len(clusters)} clusters")
        
        return ClusterResponse(clusters=clusters)
        
    except Exception as e:
        logger.error(f"Error during clustering: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Clustering failed: {str(e)}")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

