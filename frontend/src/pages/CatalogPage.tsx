import { useState } from 'react';
import Header from '../components/Header';

interface Category {
  id: string;
  name: string;
  subcategories?: Category[];
}

interface Product {
  id: number;
  name: string;
  brand: string;
  image: string;
  categoryId: string;
  subcategoryId: string;
  hasOffer?: boolean;
  isInDemand?: boolean;
  tags?: string[];
}

const CatalogPage = () => {
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [selectedSubcategory, setSelectedSubcategory] = useState<string | null>(null);
  const [hasOffers, setHasOffers] = useState(false);
  const [isInDemand, setIsInDemand] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const categories: Category[] = [
    {
      id: 'electronics',
      name: 'Электроника',
      subcategories: [
        { id: 'phones', name: 'Телефоны' },
        { id: 'laptops', name: 'Ноутбуки' },
        { id: 'tablets', name: 'Планшеты' },
        { id: 'computers', name: 'Компьютеры' },
      ],
    },
    {
      id: 'furniture',
      name: 'Мебель',
      subcategories: [
        { id: 'office', name: 'Офисная мебель' },
        { id: 'home', name: 'Домашняя мебель' },
        { id: 'chairs', name: 'Стулья' },
      ],
    },
    {
      id: 'office',
      name: 'Канцелярские товары',
      subcategories: [
        { id: 'paper', name: 'Бумага' },
        { id: 'pens', name: 'Ручки' },
        { id: 'folders', name: 'Папки' },
        { id: 'rulers', name: 'Линейки' },
      ],
    },
    {
      id: 'equipment',
      name: 'Оборудование',
      subcategories: [
        { id: 'printers', name: 'Принтеры' },
        { id: 'scanners', name: 'Сканеры' },
        { id: 'projectors', name: 'Проекторы' },
      ],
    },
  ];

  const currentCategory = categories.find(cat => cat.id === selectedCategory);
  const subcategories = currentCategory?.subcategories || [];

  const products: Product[] = [
    {
      id: 1,
      name: 'Линейка деревянная 30 см',
      brand: 'OfficePro',
      image: '📏',
      categoryId: 'office',
      subcategoryId: 'rulers',
      hasOffer: true,
      isInDemand: true,
      tags: ['линейка', 'деревянная', '30 см', 'скидка'],
    },
    {
      id: 2,
      name: 'Линейка пластиковая 50 см усиленная',
      brand: 'SmartLine',
      image: '📏',
      categoryId: 'office',
      subcategoryId: 'rulers',
      hasOffer: true,
      tags: ['линейка', 'пластиковая', '50 см', 'скидка'],
    },
    {
      id: 3,
      name: 'Линейка металлическая 1 метр',
      brand: 'ProMeasure',
      image: '📏',
      categoryId: 'office',
      subcategoryId: 'rulers',
      isInDemand: true,
      tags: ['линейка', 'металлическая', '1 метр'],
    },
    {
      id: 4,
      name: 'Линейка гибкая прозрачная 20 см',
      brand: 'Flexi',
      image: '📏',
      categoryId: 'office',
      subcategoryId: 'rulers',
      tags: ['линейка', 'гибкая', 'прозрачная'],
    },
    {
      id: 5,
      name: 'Линейка алюминиевая с антискользящим покрытием',
      brand: 'MeasureX',
      image: '📏',
      categoryId: 'office',
      subcategoryId: 'rulers',
      hasOffer: true,
      tags: ['линейка', 'алюминиевая', 'скидка'],
    },
    {
      id: 6,
      name: 'Смартфон SmartOne X',
      brand: 'SmartOne',
      image: '📱',
      categoryId: 'electronics',
      subcategoryId: 'phones',
      isInDemand: true,
      tags: ['телефон', 'смартфон'],
    },
    {
      id: 7,
      name: 'Ноутбук UltraBook Pro 15',
      brand: 'UltraTech',
      image: '💻',
      categoryId: 'electronics',
      subcategoryId: 'laptops',
      tags: ['ноутбук'],
    },
    {
      id: 8,
      name: 'Планшет VisionTab S',
      brand: 'Vision',
      image: '📱',
      categoryId: 'electronics',
      subcategoryId: 'tablets',
      hasOffer: true,
      tags: ['планшет', 'скидка'],
    },
    {
      id: 9,
      name: 'Компьютер Monoblock 24"',
      brand: 'MonoTech',
      image: '🖥️',
      categoryId: 'electronics',
      subcategoryId: 'computers',
      tags: ['компьютер'],
    },
    {
      id: 10,
      name: 'Стул офисный эргономичный',
      brand: 'Comfort',
      image: '🪑',
      categoryId: 'furniture',
      subcategoryId: 'office',
      tags: ['мебель', 'стул'],
    },
    {
      id: 11,
      name: 'Принтер лазерный LaserJet 4000',
      brand: 'Printo',
      image: '🖨️',
      categoryId: 'equipment',
      subcategoryId: 'printers',
      tags: ['принтер'],
    },
    {
      id: 12,
      name: 'Сканер документов ScanPro',
      brand: 'ScanPro',
      image: '🖨️',
      categoryId: 'equipment',
      subcategoryId: 'scanners',
      tags: ['сканер'],
    },
  ];

  const normalizedQuery = searchQuery.trim().toLowerCase();
  const filteredProducts = products.filter((product) => {
    const matchesSearch = normalizedQuery
      ? product.name.toLowerCase().includes(normalizedQuery) ||
        product.tags?.some((tag) => tag.toLowerCase().includes(normalizedQuery))
      : true;
    const matchesCategory = selectedCategory ? product.categoryId === selectedCategory : true;
    const matchesSubcategory = selectedSubcategory ? product.subcategoryId === selectedSubcategory : true;
    const matchesOffers = hasOffers ? product.hasOffer : true;
    const matchesDemand = isInDemand ? product.isInDemand : true;

    return matchesSearch && matchesCategory && matchesSubcategory && matchesOffers && matchesDemand;
  });

  const resetFilters = () => {
    setSelectedCategory(null);
    setSelectedSubcategory(null);
    setHasOffers(false);
    setIsInDemand(false);
    setSearchQuery('');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <div className="bg-gradient-to-br from-blue-600 to-blue-700 text-white py-8 px-6">
        <div className="max-w-7xl mx-auto">
          <h1 className="text-3xl font-bold mb-2">ПОРТАЛ ПОСТАВЩИКОВ</h1>
          <p className="text-lg opacity-90">Оперативные закупки товаров, работ и услуг</p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-6 py-6">
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex flex-col md:flex-row gap-4 items-center">
            <button className="px-4 py-3 bg-gray-100 rounded-lg flex items-center gap-2 hover:bg-gray-200 transition-colors border-none cursor-pointer">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <rect x="2" y="2" width="4" height="4" fill="currentColor"/>
                <rect x="8" y="2" width="4" height="4" fill="currentColor"/>
                <rect x="14" y="2" width="4" height="4" fill="currentColor"/>
                <rect x="2" y="8" width="4" height="4" fill="currentColor"/>
                <rect x="8" y="8" width="4" height="4" fill="currentColor"/>
                <rect x="14" y="8" width="4" height="4" fill="currentColor"/>
                <rect x="2" y="14" width="4" height="4" fill="currentColor"/>
                <rect x="8" y="14" width="4" height="4" fill="currentColor"/>
                <rect x="14" y="14" width="4" height="4" fill="currentColor"/>
              </svg>
              <span className="font-medium">Каталог</span>
            </button>
            <div className="flex-1 flex items-center gap-2 bg-white border-2 border-gray-200 rounded-lg px-4 py-3 w-full">
              <input
                type="text"
                placeholder="Введите название категории, товара или ID СТЕ"
                className="flex-1 outline-none text-gray-700 placeholder-gray-400"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <button className="p-2 text-gray-400 hover:text-gray-600 transition-colors border-none bg-transparent cursor-pointer">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <path d="M12 12L18 18M11 16C7.13401 16 4 12.866 4 9C4 5.13401 7.13401 2 11 2C14.866 2 18 5.13401 18 9C18 12.866 14.866 16 11 16Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </button>
              <button className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors border-none cursor-pointer flex items-center gap-2">
                <svg width="16" height="16" viewBox="0 0 20 20" fill="none">
                  <path d="M9 17C13.4183 17 17 13.4183 17 9C17 4.58172 13.4183 1 9 1C4.58172 1 1 4.58172 1 9C1 13.4183 4.58172 17 9 17Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M19 19L14.65 14.65" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                <span>Найти</span>
              </button>
            </div>
          </div>
          <div className="flex gap-4 mt-4 text-sm flex-wrap">
            <button
              className={`transition-colors bg-transparent border-none cursor-pointer ${
                hasOffers ? 'text-red-600 font-semibold' : 'text-blue-600 hover:text-blue-700'
              }`}
              onClick={() => setHasOffers((prev) => !prev)}
            >
              Есть предложения
            </button>
            <button
              className={`transition-colors bg-transparent border-none cursor-pointer ${
                searchQuery.toLowerCase() === 'скидка'
                  ? 'text-red-600 font-semibold'
                  : 'text-blue-600 hover:text-blue-700'
              }`}
              onClick={() => setSearchQuery('скидка')}
            >
              Товар со скидкой
            </button>
            <button
              className={`transition-colors bg-transparent border-none cursor-pointer ${
                isInDemand ? 'text-red-600 font-semibold' : 'text-blue-600 hover:text-blue-700'
              }`}
              onClick={() => setIsInDemand((prev) => !prev)}
            >
              Востребованный товар
            </button>
          </div>
        </div>

        <div className="flex gap-6">
          <aside className="w-64 flex-shrink-0">
            <div className="bg-white rounded-lg shadow-sm p-4">
              <h2 className="text-lg font-semibold mb-4 text-gray-800">Категории</h2>
              <div className="space-y-2">
                {categories.map((category) => (
                  <div key={category.id}>
                    <button
                      className={`w-full text-left px-3 py-2 rounded-lg transition-colors flex items-center justify-between ${
                        selectedCategory === category.id
                          ? 'bg-blue-50 text-blue-600 font-medium'
                          : 'text-gray-700 hover:bg-gray-50'
                      }`}
                      onClick={() => {
                        setSelectedCategory(selectedCategory === category.id ? null : category.id);
                        setSelectedSubcategory(null);
                      }}
                    >
                      <span>{category.name}</span>
                      {category.subcategories && (
                        <svg
                          className={`w-4 h-4 transition-transform ${
                            selectedCategory === category.id ? 'rotate-180' : ''
                          }`}
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                        </svg>
                      )}
                    </button>
                    {selectedCategory === category.id && subcategories.length > 0 && (
                      <div className="ml-4 mt-2 space-y-1">
                        {subcategories.map((subcat) => (
                          <button
                            key={subcat.id}
                            className={`w-full text-left px-3 py-2 rounded-lg transition-colors text-sm ${
                              selectedSubcategory === subcat.id
                                ? 'bg-blue-50 text-blue-600 font-medium'
                                : 'text-gray-600 hover:bg-gray-50'
                            }`}
                            onClick={() => setSelectedSubcategory(subcat.id)}
                          >
                            {subcat.name}
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>

              <div className="mt-6 pt-6 border-t border-gray-200">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-base font-semibold text-gray-800">Фильтры (1)</h3>
                  <button
                    className="text-sm text-blue-600 hover:text-blue-700 bg-transparent border-none cursor-pointer"
                    onClick={resetFilters}
                  >
                    Сбросить все
                  </button>
                </div>
                
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Поиск категории портала
                  </label>
                  <div className="relative">
                    <input
                      type="text"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg pr-10 outline-none focus:border-blue-500"
                      placeholder="Товары"
                    />
                    <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-2">
                      <span className="bg-blue-100 text-blue-700 px-2 py-1 rounded text-xs font-medium">
                        Товары
                        <button className="ml-1 text-blue-700 hover:text-blue-900">×</button>
                      </span>
                      <svg width="16" height="16" viewBox="0 0 20 20" fill="none" className="text-gray-400">
                        <path d="M9 17C13.4183 17 17 13.4183 17 9C17 4.58172 13.4183 1 9 1C4.58172 1 1 4.58172 1 9C1 13.4183 4.58172 17 9 17Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        <path d="M19 19L14.65 14.65" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                      </svg>
                    </div>
                  </div>
                </div>

                <div className="space-y-3">
                  <label className="flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={hasOffers}
                      onChange={(e) => setHasOffers(e.target.checked)}
                      className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
                    />
                    <span className="ml-2 text-sm text-gray-700">Есть предложения</span>
                  </label>
                  <label className="flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      checked={isInDemand}
                      onChange={(e) => setIsInDemand(e.target.checked)}
                      className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
                    />
                    <span className="ml-2 text-sm text-gray-700 flex items-center gap-1">
                      Востребованный товар
                      <span className="text-red-500">🔥</span>
                    </span>
                  </label>
                </div>
              </div>
            </div>
          </aside>

          <main className="flex-1">
            <div className="bg-white rounded-lg shadow-sm p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-blue-600">Товары</h2>
                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <span>Сортировка:</span>
                    <button className="text-blue-600 hover:text-blue-700 flex items-center gap-1 bg-transparent border-none cursor-pointer">
                      По релевантности
                      <svg width="16" height="16" viewBox="0 0 20 20" fill="none">
                        <path d="M5 7.5L10 12.5L15 7.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-between mb-6">
                <span className="text-gray-600">Найдено: {filteredProducts.length}</span>
                <div className="flex gap-2">
                  <button className="p-2 border border-gray-300 rounded hover:bg-gray-50 transition-colors bg-transparent cursor-pointer">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <rect x="2" y="2" width="6" height="6" stroke="currentColor" strokeWidth="2"/>
                      <rect x="12" y="2" width="6" height="6" stroke="currentColor" strokeWidth="2"/>
                      <rect x="2" y="12" width="6" height="6" stroke="currentColor" strokeWidth="2"/>
                      <rect x="12" y="12" width="6" height="6" stroke="currentColor" strokeWidth="2"/>
                    </svg>
                  </button>
                  <button className="p-2 border border-gray-300 rounded hover:bg-gray-50 transition-colors bg-transparent cursor-pointer">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <rect x="2" y="4" width="16" height="2" fill="currentColor"/>
                      <rect x="2" y="9" width="16" height="2" fill="currentColor"/>
                      <rect x="2" y="14" width="16" height="2" fill="currentColor"/>
                    </svg>
                  </button>
                </div>
              </div>

              {filteredProducts.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filteredProducts.map((product) => (
                    <div key={product.id} className="bg-white border border-gray-200 rounded-lg overflow-hidden hover:shadow-lg transition-shadow cursor-pointer">
                      <div className="aspect-square bg-gray-100 flex items-center justify-center text-6xl">
                        {product.image}
                      </div>
                      <div className="p-4">
                        <div className="text-xs text-gray-500 mb-1 flex items-center gap-2">
                          {product.brand}
                          {product.hasOffer && (
                            <span className="text-red-600 text-[11px] font-semibold bg-red-50 px-2 py-0.5 rounded">
                              -10%
                            </span>
                          )}
                          {product.isInDemand && (
                            <span className="text-orange-500 text-[11px] font-semibold flex items-center gap-1">
                              🔥 хит
                            </span>
                          )}
                        </div>
                        <h3 className="text-sm font-medium text-gray-800 line-clamp-2">{product.name}</h3>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-16 border border-dashed border-gray-200 rounded-lg">
                  <p className="text-lg font-semibold text-gray-800 mb-2">Ничего не найдено</p>
                  <p className="text-gray-500 mb-4">Попробуйте изменить запрос или сбросить фильтры</p>
                  <button
                    className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors border-none cursor-pointer"
                    onClick={resetFilters}
                  >
                    Сбросить фильтры
                  </button>
                </div>
              )}
            </div>
          </main>
        </div>
      </div>
    </div>
  );
};

export default CatalogPage;


