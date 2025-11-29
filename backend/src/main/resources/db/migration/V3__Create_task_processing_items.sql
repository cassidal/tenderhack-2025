-- Создание таблицы для обработки товаров в рамках задачи группировки
CREATE TABLE IF NOT EXISTS task_processing_items
(
    id              UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    task_id         UUID      NOT NULL,
    raw_product_id  BIGINT, -- Ссылка на исходный сырой товар (для трекинга)

    -- Основные поля, перенесенные из raw_products
    title           VARCHAR(1000),
    image_url       VARCHAR(500),
    model           VARCHAR(500),
    manufacturer    VARCHAR(500),
    country         VARCHAR(255),
    category_name   VARCHAR(500),

    -- Характеристики уже распарсены в JSON
    characteristics JSONB              DEFAULT '{}'::jsonb,

    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Внешние ключи
    CONSTRAINT fk_processing_task FOREIGN KEY (task_id) REFERENCES grouping_tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_processing_raw FOREIGN KEY (raw_product_id) REFERENCES raw_products (id) ON DELETE SET NULL
);

-- Создание индексов
CREATE INDEX IF NOT EXISTS idx_task_processing_items_task_id ON task_processing_items (task_id);
CREATE INDEX IF NOT EXISTS idx_task_processing_items_raw_product_id ON task_processing_items (raw_product_id);
CREATE INDEX IF NOT EXISTS idx_task_processing_items_characteristics ON task_processing_items USING GIN (characteristics);
CREATE INDEX IF NOT EXISTS idx_task_processing_items_created_at ON task_processing_items (created_at);

-- Комментарии к таблице
COMMENT ON TABLE task_processing_items IS 'Товары, обрабатываемые в рамках задачи группировки';
COMMENT ON COLUMN task_processing_items.raw_product_id IS 'Ссылка на исходный товар из raw_products';
COMMENT ON COLUMN task_processing_items.characteristics IS 'Характеристики товара в формате JSONB';

