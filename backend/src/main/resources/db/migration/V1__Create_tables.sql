-- Создание таблицы grouping_tasks
CREATE TABLE IF NOT EXISTS grouping_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    query TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    rating INTEGER
);

-- Создание индексов для grouping_tasks
CREATE INDEX IF NOT EXISTS idx_grouping_tasks_status ON grouping_tasks(status);
CREATE INDEX IF NOT EXISTS idx_grouping_tasks_created_at ON grouping_tasks(created_at);

-- Создание таблицы cte_entities
CREATE TABLE IF NOT EXISTS cte_entities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    image_url VARCHAR(500),
    important_attributes JSONB DEFAULT '[]'::jsonb,
    secondary_attributes JSONB DEFAULT '[]'::jsonb,
    product_ids JSONB DEFAULT '[]'::jsonb,
    CONSTRAINT fk_cte_task FOREIGN KEY (task_id) REFERENCES grouping_tasks(id) ON DELETE CASCADE
);

-- Создание индексов для cte_entities
CREATE INDEX IF NOT EXISTS idx_cte_entities_task_id ON cte_entities(task_id);
CREATE INDEX IF NOT EXISTS idx_cte_entities_important_attributes ON cte_entities USING GIN (important_attributes);
CREATE INDEX IF NOT EXISTS idx_cte_entities_secondary_attributes ON cte_entities USING GIN (secondary_attributes);
CREATE INDEX IF NOT EXISTS idx_cte_entities_product_ids ON cte_entities USING GIN (product_ids);

-- Комментарии к таблицам
COMMENT ON TABLE grouping_tasks IS 'Задачи группировки товаров в СТЕ';
COMMENT ON TABLE cte_entities IS 'Канонические торговые сущности (группированные карточки товаров)';
COMMENT ON COLUMN cte_entities.important_attributes IS 'Важные характеристики товара в формате JSONB';
COMMENT ON COLUMN cte_entities.secondary_attributes IS 'Второстепенные характеристики товара в формате JSONB';
COMMENT ON COLUMN cte_entities.product_ids IS 'Массив ID товаров, входящих в СТЕ, в формате JSONB';

