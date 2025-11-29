-- Создание таблицы для хранения сырых данных из CSV
CREATE TABLE IF NOT EXISTS raw_products (
    id BIGSERIAL PRIMARY KEY,
    ste_id BIGINT NOT NULL,
    title VARCHAR(1000),
    image_url VARCHAR(500),
    model VARCHAR(500),
    country VARCHAR(255),
    manufacturer VARCHAR(500),
    category_id BIGINT,
    category_name VARCHAR(500),
    characteristics TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов
CREATE INDEX IF NOT EXISTS idx_raw_products_ste_id ON raw_products(ste_id);
CREATE INDEX IF NOT EXISTS idx_raw_products_category_id ON raw_products(category_id);
CREATE INDEX IF NOT EXISTS idx_raw_products_manufacturer ON raw_products(manufacturer);
CREATE INDEX IF NOT EXISTS idx_raw_products_created_at ON raw_products(created_at);

-- Комментарии к таблице
COMMENT ON TABLE raw_products IS 'Сырые данные о товарах из CSV файла';
COMMENT ON COLUMN raw_products.ste_id IS 'ID СТЕ из CSV';
COMMENT ON COLUMN raw_products.title IS 'Название СТЕ';
COMMENT ON COLUMN raw_products.characteristics IS 'Характеристики в формате "Ключ:Значение;Ключ:Значение"';
