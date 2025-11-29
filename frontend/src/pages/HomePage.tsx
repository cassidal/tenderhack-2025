import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import { useRequestGroupingMutation } from '../store/groupingApi';
import { websocketService } from '../services/websocket';
import { useAppDispatch } from '../store/hooks';
import { groupingApi } from '../store/groupingApi';

const HomePage = () => {
  const [query, setQuery] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [requestGrouping] = useRequestGroupingMutation();

  // Реф для отслеживания монтирования компонента, чтобы избежать утечек памяти
  const isMounted = useRef(true);

  useEffect(() => {
    isMounted.current = true;
    return () => {
      isMounted.current = false;
      websocketService.disconnect();
    };
  }, []);

  const fetchResultsAndNavigate = async (taskId: string) => {
    try {
      // Запускаем получение данных
      const resultsQuery = dispatch(groupingApi.endpoints.getResults.initiate(taskId));
      const filtersQuery = dispatch(groupingApi.endpoints.getFilters.initiate(taskId));

      await Promise.all([
        resultsQuery.unwrap(),
        filtersQuery.unwrap(),
      ]);

      if (isMounted.current) {
        navigate(`/catalog?taskId=${taskId}`);
      }
    } catch (err: any) {
      console.error('Error fetching results:', err);
      if (isMounted.current) {
        setError('Ошибка при получении данных. Попробуйте еще раз.');
        setIsLoading(false);
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!query.trim()) {
      setError('Пожалуйста, введите название товара');
      return;
    }

    setIsLoading(true);
    setError(null);
    websocketService.disconnect();

    try {
      // 1. Отправляем запрос на группировку
      const result = await requestGrouping({ query: query.trim() }).unwrap();
      const taskId = result.taskId;

      // 2. Подключаемся к WebSocket и ждем завершения
      websocketService.connect(
          taskId,
          // Callback успешного завершения (onComplete/onMessage)
          async () => {
            console.log("WebSocket: Задача завершена, загружаем результаты...");
            await fetchResultsAndNavigate(taskId);
          },
          // Callback ошибки (onError)
          (wsError) => {
            console.error('WebSocket error:', wsError);
            if (isMounted.current) {
                // Можно либо показать ошибку, либо попробовать загрузить данные все равно,
                // если WS упал, но данные могли быть готовы.
                // В данном случае просто показываем ошибку:
                setError('Ошибка соединения с сервером уведомлений.');
                setIsLoading(false);
            }
          }
      );

    } catch (err: any) {
      console.error('Error requesting grouping:', err);
      if (isMounted.current) {
        setError(err.data?.message || 'Ошибка при отправке запроса. Попробуйте еще раз.');
        setIsLoading(false);
      }
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <Header />

      <main className="flex items-center justify-center min-h-[calc(100vh-80px)] px-6">
        <div className="w-full max-w-2xl">
          <div className="text-center mb-8">
            <h1 className="text-4xl md:text-5xl font-bold text-blue-700 m-0 mb-4 tracking-wide">
              ПОРТАЛ ПОСТАВЩИКОВ
            </h1>
            <p className="text-2xl text-gray-600 m-0">
              Оперативные закупки товаров, работ и услуг
            </p>
          </div>

          <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-lg p-8">
            <div className="mb-6">
              <label htmlFor="product-query" className="block text-lg font-medium text-gray-700 mb-3">
                Введите товар, который вам нужен
              </label>
              <input
                id="product-query"
                type="text"
                value={query}
                onChange={(e) => {
                  setQuery(e.target.value);
                  setError(null);
                }}
                placeholder="Например: Линейка деревянная"
                className="w-full px-4 py-3 text-lg border-2 border-gray-300 rounded-lg focus:border-blue-500 focus:outline-none transition-colors"
                disabled={isLoading}
              />
            </div>

            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={isLoading}
              className="w-full bg-blue-600 text-white px-6 py-3 rounded-lg text-lg font-semibold hover:bg-blue-700 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Обработка...
                </span>
              ) : (
                'Найти'
              )}
            </button>

            {isLoading && (
              <div className="mt-4 text-center text-gray-600">
                <p>Подключение к серверу...</p>
                <p className="text-sm mt-2">Пожалуйста, подождите, пока обрабатывается ваш запрос</p>
              </div>
            )}
          </form>
        </div>
      </main>
    </div>
  );
};

export default HomePage;
