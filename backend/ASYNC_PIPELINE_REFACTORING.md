# Asynchronous ML-driven Pipeline Refactoring

## Overview

This document describes the refactoring of the grouping logic to implement a full **Asynchronous ML-driven Pipeline** for processing raw products into CTE entities.

## Architecture

### Components

1. **GroupingFacadeImpl** - Main facade with async task creation
2. **GroupingPipelineService** - Core async pipeline orchestrator
3. **AsyncConfig** - Configuration for async execution
4. **Status Enum** - Updated with `IN_PROGRESS` status

### Pipeline Flow

```
POST /api/grouping/request
  ↓
GroupingFacadeImpl.createGroupingTask()
  ↓
Create Task (Status: IN_PROGRESS)
  ↓
@Async GroupingPipelineService.runPipeline()
  ↓
┌─────────────────────────────────────┐
│ Step A: Parse Raw Products          │
│ - Fetch raw_products                │
│ - Parse characteristics (TEXT → Map)│
│ - Save to task_processing_items     │
│ - Collect all unique keys           │
└─────────────────────────────────────┘
  ↓
┌─────────────────────────────────────┐
│ Step B: Intelligence Layer           │
│ - Python ML Clustering              │
│ - Significance Filtering            │
│ - LLM Schema Generation             │
└─────────────────────────────────────┘
  ↓
┌─────────────────────────────────────┐
│ Step C: Normalize & Group           │
│ - Normalize values using schemas    │
│ - Group by important attributes     │
│ - Create CTE entities               │
└─────────────────────────────────────┘
  ↓
┌─────────────────────────────────────┐
│ Step D: Finalization                │
│ - Update task status to COMPLETED   │
│ - (or FAILED on error)              │
└─────────────────────────────────────┘
```

## Key Features

### 1. Asynchronous Execution

- Tasks are created immediately with `IN_PROGRESS` status
- Pipeline runs in background thread pool
- Client receives `taskId` immediately
- Status can be checked via task repository

### 2. Transaction Management

- Each step has its own `@Transactional` boundary
- Step A: Transaction for parsing and saving processing items
- Step C: Transaction for grouping and saving CTE entities
- Status updates are transactional

### 3. Error Handling

- Pipeline failures set task status to `FAILED`
- Exceptions are logged and propagated via `CompletableFuture`
- No partial data corruption (transactions ensure consistency)

### 4. Data Flow

#### Step A: Parse Raw Products
- Input: `raw_products` table (TEXT characteristics)
- Output: `task_processing_items` table (JSONB characteristics)
- Side effect: Collects all unique keys for clustering

#### Step B: Intelligence Layer
- **B1**: Python ML Clustering (`PythonClusterClient`)
  - Input: Set of unique raw keys
  - Output: List of semantic clusters
- **B2**: Significance Filtering (`SignificanceFilterService`)
  - Input: Clusters + raw data rows
  - Output: Significant clusters only
- **B3**: LLM Schema Generation (`OllamaService`)
  - Input: Significant clusters
  - Output: Map<firstKey, SchemaConfig>

#### Step C: Normalize & Group
- Normalize all attributes using schemas
- Group products by normalized important attributes
- Create `CteEntity` for each group:
  - `important_attributes`: Used for grouping
  - `secondary_attributes`: Other normalized attributes
  - `product_ids`: List of raw product IDs in group
  - `image_url`: Best image from group

## Database Schema

### Tables Used

1. **raw_products**: Source data
   - `characteristics`: TEXT (format: "Key:Value;Key2:Value2")

2. **grouping_tasks**: Task tracking
   - `status`: PENDING, IN_PROGRESS, PROCESSING, COMPLETED, FAILED, APPROVED

3. **task_processing_items**: Intermediate storage
   - `characteristics`: JSONB (parsed Map<String, String>)

4. **cte_entities**: Final results
   - `important_attributes`: JSONB (List<AttributeJson>)
   - `secondary_attributes`: JSONB (List<AttributeJson>)
   - `product_ids`: JSONB (List<Long>)

## API Changes

### Before (Synchronous)
```java
@PostMapping("/request")
public ResponseEntity<TaskResponse> createGroupingTask(...) {
    // Blocking call - waits for completion
    TaskResponse response = groupingFacade.createGroupingTask(query);
    return ResponseEntity.ok(response);
}
```

### After (Asynchronous)
```java
@PostMapping("/request")
public ResponseEntity<TaskResponse> createGroupingTask(...) {
    // Non-blocking - returns immediately
    TaskResponse response = groupingFacade.createGroupingTask(query);
    // Task is IN_PROGRESS, pipeline runs in background
    return ResponseEntity.ok(response);
}
```

### Status Checking

Clients can check task status:
```java
GroupingTaskEntity task = taskRepository.findById(taskId);
Status status = task.getStatus(); // IN_PROGRESS, COMPLETED, FAILED, etc.
```

## Configuration

### AsyncConfig

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "groupingPipelineExecutor")
    public Executor groupingPipelineExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        return executor;
    }
}
```

## Migration Notes

1. **Status Enum**: Added `IN_PROGRESS` (alias for `PROCESSING`)
2. **GroupingFacade**: New implementation `GroupingFacadeImpl` marked as `@Primary`
3. **Old Stub**: `GroupingFacadeStub` still exists but is not used (can be removed)

## Testing

### Unit Tests
- Test each step independently
- Mock external services (Python ML, Ollama)
- Verify transaction boundaries

### Integration Tests
- Test full pipeline with test data
- Verify async execution
- Check error handling and status updates

## Performance Considerations

1. **Batch Processing**: Steps A and C use batch saves (1000 items per batch)
2. **Micro-batching**: LLM calls are batched (5 clusters per batch)
3. **Thread Pool**: Configurable executor for parallel task processing
4. **Database Indexes**: GIN indexes on JSONB columns for fast queries

## Future Improvements

1. **Progress Tracking**: Add progress percentage to task entity
2. **Cancellation**: Support task cancellation
3. **Retry Logic**: Automatic retry on transient failures
4. **Metrics**: Add metrics for pipeline performance
5. **Streaming**: Stream results as they're generated

