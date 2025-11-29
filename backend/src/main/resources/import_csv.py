#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт для импорта данных из table.csv в базу данных PostgreSQL
Создает таблицу raw_products и загружает туда все данные из CSV
"""

import pandas as pd
import psycopg2
from psycopg2.extras import execute_values
import sys
import os

# --- НАСТРОЙКИ ПОДКЛЮЧЕНИЯ К БД ---
# Для локальной разработки: postgresql://postgres:postgres@localhost:5432/cte_grouping
# Для Docker: postgresql://postgres:postgres@postgres:5432/cte_grouping
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': os.getenv('DB_PORT', '5432'),
    'database': os.getenv('DB_NAME', 'cte_grouping'),
    'user': os.getenv('DB_USER', 'postgres'),
    'password': os.getenv('DB_PASSWORD', 'postgres')
}



def create_raw_products_table(conn):
    """Создает таблицу raw_products если её нет"""
    with conn.cursor() as cur:
        cur.execute("""
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
            )
        """)
        
        # Создаем индексы
        cur.execute("""
            CREATE INDEX IF NOT EXISTS idx_raw_products_ste_id ON raw_products(ste_id)
        """)
        cur.execute("""
            CREATE INDEX IF NOT EXISTS idx_raw_products_category_id ON raw_products(category_id)
        """)
        cur.execute("""
            CREATE INDEX IF NOT EXISTS idx_raw_products_manufacturer ON raw_products(manufacturer)
        """)
        
        conn.commit()


def safe_int(value, default=None):
    """Безопасное преобразование в int"""
    if pd.isna(value) or not value or str(value).strip() == 'NULL':
        return default
    try:
        return int(str(value).strip())
    except (ValueError, TypeError):
        return default


def safe_str(value, max_length=None):
    """Безопасное преобразование в строку"""
    if pd.isna(value) or not value or str(value).strip() == 'NULL':
        return None
    result = str(value).strip()
    if max_length and len(result) > max_length:
        result = result[:max_length]
    return result if result else None


def load_csv_to_db(csv_file_path, task_query=None):
    """
    Загружает данные из CSV в таблицу raw_products
    """
    try:
        print(f"📖 Читаем файл: {csv_file_path}...")
        
        # Пробуем разные кодировки
        encodings = ['utf-8', 'windows-1251', 'cp1251', 'latin-1']
        df = None
        
        for encoding in encodings:
            try:
                df = pd.read_csv(csv_file_path, sep=';', dtype=str, encoding=encoding)
                print(f"   ✓ Файл прочитан с кодировкой: {encoding}")
                break
            except UnicodeDecodeError:
                continue
        
        if df is None:
            print("❌ Ошибка: Не удалось прочитать файл с доступными кодировками")
            return
        
        # Проверяем, есть ли данные
        if df.empty:
            print("❌ Ошибка: Файл пустой")
            return
        
        print(f"   Найдено строк: {len(df)}")
        print(f"   Колонки: {list(df.columns)}")
        
        # Маппинг колонок CSV на колонки БД
        column_mapping = {
            'id сте': 'ste_id',
            'название сте': 'title',
            'ссылка на картинку сте': 'image_url',
            'модель': 'model',
            'страна происхождения': 'country',
            'производитель': 'manufacturer',
            'id категории': 'category_id',
            'название категории': 'category_name',
            'характеристики': 'characteristics'
        }
        
        print("🔌 Подключаемся к базе данных...")
        conn = psycopg2.connect(**DB_CONFIG)
        
        # Создаем таблицу если её нет
        print("📋 Создаем таблицу raw_products...")
        create_raw_products_table(conn)
        print("   ✓ Таблица готова")
        
        print("🔄 Обрабатываем данные...")
        
        # Подготавливаем данные для вставки
        records = []
        processed = 0
        skipped = 0
        
        for idx, row in df.iterrows():
            try:
                # Получаем ID СТЕ (обязательное поле)
                ste_id = safe_int(row.get('id сте', ''))
                if ste_id is None:
                    skipped += 1
                    continue
                
                # Получаем остальные поля
                title = safe_str(row.get('название сте', ''), 1000)
                image_url = safe_str(row.get('ссылка на картинку сте', ''), 500)
                model = safe_str(row.get('модель', ''), 500)
                country = safe_str(row.get('страна происхождения', ''), 255)
                manufacturer = safe_str(row.get('производитель', ''), 500)
                category_id = safe_int(row.get('id категории', ''))
                category_name = safe_str(row.get('название категории', ''), 500)
                characteristics = safe_str(row.get('характеристики', ''))
                
                # Создаем запись для вставки
                record = (
                    ste_id,
                    title,
                    image_url,
                    model,
                    country,
                    manufacturer,
                    category_id,
                    category_name,
                    characteristics
                )
                
                records.append(record)
                processed += 1
                
                if processed % 100 == 0:
                    print(f"   Обработано: {processed}/{len(df)}")
                    
            except Exception as e:
                print(f"   ⚠️  Ошибка при обработке строки {idx + 1}: {e}")
                skipped += 1
                continue
        
        # Вставляем данные батчами
        print(f"💾 Вставляем {len(records)} записей в базу данных...")
        
        with conn.cursor() as cur:
            insert_query = """
                INSERT INTO raw_products 
                (ste_id, title, image_url, model, country, manufacturer, category_id, category_name, characteristics)
                VALUES %s
            """
            
            # Вставляем батчами по 1000 записей
            batch_size = 1000
            for i in range(0, len(records), batch_size):
                batch = records[i:i+batch_size]
                execute_values(cur, insert_query, batch)
                conn.commit()
                print(f"   ✓ Вставлено: {min(i+batch_size, len(records))}/{len(records)}")
        
        conn.close()
        
        print(f"\n✅ Успешно!")
        print(f"   Загружено записей: {len(records)}")
        print(f"   Пропущено строк: {skipped}")
        print(f"\n   Данные сохранены в таблицу raw_products")
        print(f"   Для просмотра используйте SQL запрос:")
        print(f"   SELECT * FROM raw_products LIMIT 10;")
        
    except Exception as e:
        print(f"\n❌ Произошла ошибка:\n{e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Использование: python import_csv.py <путь_к_файлу.csv>")
        print("\nПримеры:")
        print("  python import_csv.py table.csv")
        print("\nПеременные окружения:")
        print("  DB_HOST - хост БД (по умолчанию: localhost)")
        print("  DB_PORT - порт БД (по умолчанию: 5432)")
        print("  DB_NAME - имя БД (по умолчанию: cte_grouping)")
        print("  DB_USER - пользователь БД (по умолчанию: postgres)")
        print("  DB_PASSWORD - пароль БД (по умолчанию: postgres)")
    else:
        file_path = sys.argv[1]
        
        if os.path.exists(file_path):
            load_csv_to_db(file_path)
        else:
            print(f"❌ Ошибка: Файл '{file_path}' не найден.")

