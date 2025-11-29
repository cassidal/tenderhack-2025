import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import {
  useGetResultsQuery,
  useGetFiltersQuery,
  useRegenerateMutation,
  useApproveMutation,
  useRateMutation,
} from '../store/groupingApi';
import { websocketService } from '../services/websocket';
import { useAppDispatch } from '../store/hooks';
import { groupingApi } from '../store/groupingApi';
import type { ProductCard, FilterOption } from '../store/groupingApi';

const CatalogPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const taskId = searchParams.get('taskId');

  const [showRegenerateModal, setShowRegenerateModal] = useState(false);
  const [regenerateQuery, setRegenerateQuery] = useState('');
  const [isRegenerating, setIsRegenerating] = useState(false);
  const [rating, setRating] = useState(3);
  const [showRatingModal, setShowRatingModal] = useState(false);
  const [selectedFilters, setSelectedFilters] = useState<Record<string, string[]>>({});
  const [searchQuery, setSearchQuery] = useState('');

  // Загружаем данные если есть taskId
  const { data: resultsData, isLoading: isLoadingResults, error: resultsError } = useGetResultsQuery(
    taskId || '',
    { skip: !taskId }
  );
  const { data: filtersData, isLoading: isLoadingFilters } = useGetFiltersQuery(
    taskId || '',
    { skip: !taskId }
  );

  const [regenerate] = useRegenerateMutation();
  const [approve] = useApproveMutation();
  const [rate] = useRateMutation();

  // Редирект на главную если нет taskId
  useEffect(() => {
    if (!taskId) {
      navigate('/');
    }
  }, [taskId, navigate]);

  // Очистка WebSocket при размонтировании
  useEffect(() => {
    return () => {
      websocketService.disconnect();
    };
  }, []);

  const handleRegenerate = async () => {
    if (!taskId || !regenerateQuery.trim()) return;

    setIsRegenerating(true);
    setShowRegenerateModal(false);

    websocketService.disconnect();

    try {
      const result = await regenerate({
        taskId,
        body: { query: regenerateQuery.trim() },
      }).unwrap();

      const newTaskId = result.taskId;

      // Функция для получения данных (вынесли, чтобы вызывать из двух мест)
      const fetchNewData = async () => {
        try {
          // Запускаем запросы (без await Promise.all, чтобы получить объекты)
          const resultsQuery = dispatch(groupingApi.endpoints.getResults.initiate(newTaskId));
          const filtersQuery = dispatch(groupingApi.endpoints.getFilters.initiate(newTaskId));

          // Ждем реальные промисы
          await Promise.all([
            resultsQuery.unwrap(),
            filtersQuery.unwrap(),
          ]);

          navigate(`/catalog?taskId=${newTaskId}`, { replace: true });
          setRegenerateQuery('');
          // Важно: выключаем загрузку
          setIsRegenerating(false);
        } catch (err) {
          console.error('Error fetching regenerated results:', err);
          setIsRegenerating(false);
        }
      };

      // Подключаемся (слушаем, вдруг повезет и сообщение придет)
      websocketService.connect(
          newTaskId,
          () => {
            console.log("Server finished regeneration early");
            // Можно вызвать fetchNewData(), но нужно защититься от двойного вызова
          },
          (wsError) => {
            console.error('WebSocket error during regeneration:', wsError);
            // Не выключаем загрузку здесь, ждем таймер
          }
      );

      // ПРИНУДИТЕЛЬНОЕ ЗАВЕРШЕНИЕ ЧЕРЕЗ 5 СЕКУНД
      setTimeout(async () => {
        console.log("Force finishing regeneration after 5s...");
        websocketService.disconnect();
        await fetchNewData();
      }, 5000);

    } catch (err: any) {
      console.error('Error regenerating:', err);
      setIsRegenerating(false);
    }
  };


  const handleApprove = async () => {
    if (!taskId) return;

    try {
      await approve(taskId).unwrap();
      alert('Данные успешно подтверждены!');
    } catch (err: any) {
      console.error('Error approving:', err);
      alert('Ошибка при подтверждении данных');
    }
  };

  const handleRate = async () => {
    if (!taskId) return;

    try {
      await rate({ taskId, body: { rating } }).unwrap();
      alert('Оценка успешно отправлена!');
      setShowRatingModal(false);
    } catch (err: any) {
      console.error('Error rating:', err);
      alert('Ошибка при отправке оценки');
    }
  };

  const handleFilterChange = (filterId: string, value: string) => {
    setSelectedFilters((prev) => {
      const currentValues = prev[filterId] || [];
      const newValues = currentValues.includes(value)
        ? currentValues.filter((v) => v !== value)
        : [...currentValues, value];
      return { ...prev, [filterId]: newValues };
    });
  };

  const cards = resultsData?.cards || [];
  const filters = filtersData?.filters || [];

  // Фильтрация карточек
  const filteredCards = cards.filter((card) => {
    // Фильтр по поисковому запросу
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      const matchesSearch =
        card.name?.toLowerCase().includes(query) ||
        card.description?.toLowerCase().includes(query);
      if (!matchesSearch) return false;
    }

    // Фильтр по выбранным фильтрам
    for (const [filterId, selectedValues] of Object.entries(selectedFilters)) {
      if (selectedValues.length > 0) {
        // Проверяем, есть ли у карточки свойство, соответствующее фильтру
        const cardValue = card[filterId];
        if (cardValue && !selectedValues.includes(String(cardValue))) {
          return false;
        }
      }
    }

    return true;
  });

  if (!taskId) {
    return null;
  }

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
        {/* Действия */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6 flex flex-wrap gap-4 items-center justify-between">
          <div className="flex gap-4 flex-wrap">
            <button
              onClick={() => setShowRegenerateModal(true)}
              disabled={isRegenerating}
              className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed border-none cursor-pointer"
            >
              {isRegenerating ? 'Регенерация...' : 'Перегенерировать данные'}
            </button>
            <button
              onClick={handleApprove}
              className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors border-none cursor-pointer"
            >
              Подтвердить данные
            </button>
            <button
              onClick={() => setShowRatingModal(true)}
              className="px-6 py-3 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition-colors border-none cursor-pointer"
            >
              Поставить оценку
            </button>
          </div>
          {isRegenerating && (
            <div className="flex items-center gap-2 text-gray-600">
              <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span>Идет регенерация данных...</span>
            </div>
          )}
        </div>

        <div className="flex gap-6">
          {/* Фильтры */}
          <aside className="w-64 flex-shrink-0">
            <div className="bg-white rounded-lg shadow-sm p-4 sticky top-6">
              <h2 className="text-lg font-semibold mb-4 text-gray-800">Фильтры</h2>

              {/* Поиск */}
              <div className="mb-6">
                <input
                  type="text"
                  placeholder="Поиск по карточкам..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg outline-none focus:border-blue-500"
                />
              </div>

              {/* Фильтры с бэкенда */}
              {isLoadingFilters ? (
                <div className="text-gray-500">Загрузка фильтров...</div>
              ) : filters.length === 0 ? (
                <div className="text-gray-500">Фильтры не найдены</div>
              ) : (
                <div className="space-y-4">
                  {filters.map((filter: FilterOption) => (
                    <div key={filter.id}>
                      <h3 className="text-sm font-medium text-gray-700 mb-2">{filter.name}</h3>
                      {filter.values && filter.values.length > 0 ? (
                        <div className="space-y-2">
                          {filter.values.map((value) => (
                            <label key={value} className="flex items-center cursor-pointer">
                              <input
                                type="checkbox"
                                checked={selectedFilters[filter.id]?.includes(value) || false}
                                onChange={() => handleFilterChange(filter.id, value)}
                                className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
                              />
                              <span className="ml-2 text-sm text-gray-700">{value}</span>
                            </label>
                          ))}
                        </div>
                      ) : (
                        <div className="text-sm text-gray-500">Нет значений</div>
                      )}
                    </div>
                  ))}
                </div>
              )}

              {/* Сброс фильтров */}
              {(Object.keys(selectedFilters).length > 0 || searchQuery.trim()) && (
                <button
                  onClick={() => {
                    setSelectedFilters({});
                    setSearchQuery('');
                  }}
                  className="mt-4 w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors border-none cursor-pointer text-sm"
                >
                  Сбросить фильтры
                </button>
              )}
            </div>
          </aside>

          {/* Карточки */}
          <main className="flex-1">
            <div className="bg-white rounded-lg shadow-sm p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-blue-600">Карточки товаров</h2>
                <span className="text-gray-600">
                  Найдено: {filteredCards.length} из {cards.length}
                </span>
              </div>

              {isLoadingResults ? (
                <div className="text-center py-16">
                  <svg className="animate-spin h-12 w-12 mx-auto text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <p className="mt-4 text-gray-600">Загрузка карточек...</p>
                </div>
              ) : resultsError ? (
                <div className="text-center py-16">
                  <p className="text-red-600 mb-4">Ошибка при загрузке данных</p>
                  <button
                    onClick={() => window.location.reload()}
                    className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors border-none cursor-pointer"
                  >
                    Обновить страницу
                  </button>
                </div>
              ) : filteredCards.length === 0 ? (
                <div className="text-center py-16 border border-dashed border-gray-200 rounded-lg">
                  <p className="text-lg font-semibold text-gray-800 mb-2">Ничего не найдено</p>
                  <p className="text-gray-500 mb-4">Попробуйте изменить фильтры или поисковый запрос</p>
                  <button
                    onClick={() => {
                      setSelectedFilters({});
                      setSearchQuery('');
                    }}
                    className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors border-none cursor-pointer"
                  >
                    Сбросить фильтры
                  </button>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {filteredCards.map((card: ProductCard) => (
                    <div
                      key={card.id}
                      className="bg-white border border-gray-200 rounded-lg overflow-hidden hover:shadow-lg transition-shadow"
                    >
                      {card.image && (
                        <div className="aspect-square bg-gray-100 flex items-center justify-center">
                          <img
                            src={card.image}
                            alt={card.name || 'Карточка товара'}
                            className="w-full h-full object-cover"
                            onError={(e) => {
                              e.currentTarget.style.display = 'none';
                            }}
                          />
                        </div>
                      )}
                      <div className="p-4">
                        <h3 className="text-lg font-medium text-gray-800 mb-2 line-clamp-2">
                          {card.name || `Карточка ${card.id}`}
                        </h3>
                        {card.description && (
                          <p className="text-sm text-gray-600 line-clamp-3 mb-3">{card.description}</p>
                        )}
                        {card.url && (
                          <a
                            href={card.url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-blue-600 hover:text-blue-700 text-sm font-medium"
                          >
                            Перейти к товару →
                          </a>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </main>
        </div>
      </div>

      {/* Модальное окно регенерации */}
      {showRegenerateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-4">Перегенерировать данные</h2>
            <p className="text-gray-600 mb-4">
              Введите строку с изменением агрегации данных:
            </p>
            <textarea
              value={regenerateQuery}
              onChange={(e) => setRegenerateQuery(e.target.value)}
              placeholder="Например: сгруппировать по размеру и цвету"
              className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:border-blue-500 focus:outline-none resize-none"
              rows={4}
            />
            <div className="flex gap-4 mt-6">
              <button
                onClick={() => {
                  setShowRegenerateModal(false);
                  setRegenerateQuery('');
                }}
                className="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors border-none cursor-pointer"
              >
                Отмена
              </button>
              <button
                onClick={handleRegenerate}
                disabled={!regenerateQuery.trim()}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed border-none cursor-pointer"
              >
                Отправить
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Модальное окно рейтинга */}
      {showRatingModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-4">Оценить данные</h2>
            <p className="text-gray-600 mb-6">
              Выберите оценку от 1 до 5:
            </p>
            <div className="mb-6">
              <input
                type="range"
                min="1"
                max="5"
                value={rating}
                onChange={(e) => setRating(Number(e.target.value))}
                className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
              />
              <div className="flex justify-between mt-2 text-sm text-gray-600">
                <span>1</span>
                <span className="text-2xl font-bold text-blue-600">{rating}</span>
                <span>5</span>
              </div>
            </div>
            <div className="flex gap-4">
              <button
                onClick={() => {
                  setShowRatingModal(false);
                  setRating(3);
                }}
                className="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors border-none cursor-pointer"
              >
                Отмена
              </button>
              <button
                onClick={handleRate}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors border-none cursor-pointer"
              >
                Отправить оценку
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CatalogPage;
