import { useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';

const HomePage = () => {
  const [showBanner, setShowBanner] = useState(true);
  const [showNewServices, setShowNewServices] = useState(true);

  return (
    <div className="min-h-screen bg-gray-100">
      <Header />
      
      {showBanner && (
        <div className="bg-gradient-to-br from-blue-600 to-blue-700 text-white py-8 px-6 relative">
          <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-[1fr_2fr] gap-8 items-start">
            <div className="flex items-center">
              <h1 className="text-3xl md:text-4xl font-bold m-0 leading-tight">
                ДОБРО ПОЖАЛОВАТЬ НА ПОРТАЛ ПОСТАВЩИКОВ!
              </h1>
            </div>
            <div className="flex flex-col gap-4">
              <p className="text-base leading-relaxed m-0 opacity-95">
                На нашем Портале доступна регистрация как для поставщиков, так и для заказчиков. 
                После успешной регистрации вы сможете создать котировочную сессию или принять участие в ней, 
                а также создать предложение и СТЕ.
              </p>
              <div className="flex gap-3">
                <Link to="/register" className="px-6 py-3 rounded bg-white text-blue-600 text-sm font-medium cursor-pointer transition-colors hover:bg-gray-50 no-underline inline-block">
                  Зарегистрироваться
                </Link>
                <button className="px-6 py-3 rounded bg-white/20 text-white text-sm font-medium border border-white/30 cursor-pointer transition-colors hover:bg-white/30">
                  Инструкция
                </button>
              </div>
            </div>
            <button 
              className="absolute top-4 right-4 bg-white/20 border-none text-white w-8 h-8 rounded-full cursor-pointer text-2xl leading-none flex items-center justify-center transition-colors hover:bg-white/30"
              onClick={() => setShowBanner(false)}
              aria-label="Закрыть"
            >
              ×
            </button>
          </div>
        </div>
      )}

      {showNewServices && (
        <div className="bg-blue-50 py-6 px-6 border-b border-blue-200">
          <div className="max-w-7xl mx-auto bg-white rounded-lg p-6 relative shadow-sm">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold m-0 text-blue-600">Новые сервисы на Портале поставщиков</h2>
              <span className="text-sm text-gray-600">20.11.2025</span>
            </div>
            <p className="text-base leading-relaxed text-gray-800 m-0 mb-4">
              Уважаемые пользователи! На Портале поставщиков появились новые дополнительные сервисы, 
              которые улучшат вашу работу с платформой и станут надежной опорой. Подробнее
            </p>
            <button 
              className="bg-transparent border-none text-blue-600 cursor-pointer text-sm px-4 py-2 rounded transition-colors hover:bg-blue-50"
              onClick={() => setShowNewServices(false)}
            >
              Скрыть
            </button>
          </div>
        </div>
      )}

      <main className="max-w-7xl mx-auto py-12 px-6">
        <div className="flex flex-col gap-8">
          <div className="text-center">
            <h1 className="text-4xl md:text-5xl font-bold text-blue-700 m-0 mb-4 tracking-wide">
              ПОРТАЛ ПОСТАВЩИКОВ
            </h1>
            <p className="text-2xl text-gray-600 m-0">
              Оперативные закупки товаров, работ и услуг
            </p>
          </div>
          
          <div className="flex justify-center gap-12 flex-wrap">
            <div className="flex items-center gap-4">
            <div className="w-[60px] h-[60px] border-[3px] border-red-600 flex items-center justify-center text-3xl font-bold text-red-600 flex-shrink-0">
              M
            </div>
              <p className="text-sm font-semibold text-gray-800 leading-snug m-0">
                ДЕПАРТАМЕНТ ГОРОДА МОСКВЫ<br />ПО КОНКУРЕНТНОЙ ПОЛИТИКЕ
              </p>
            </div>
            <div className="flex items-center gap-4">
            <div className="w-[60px] h-[60px] border-[3px] border-red-600 flex items-center justify-center text-3xl font-bold text-red-600 flex-shrink-0">
              M
            </div>
              <p className="text-sm font-semibold text-gray-800 leading-snug m-0">
                ДЕПАРТАМЕНТ ИНФОРМАЦИОННЫХ<br />ТЕХНОЛОГИЙ ГОРОДА МОСКВЫ
              </p>
            </div>
          </div>

          <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-2xl p-12 mt-8 shadow-lg relative overflow-hidden">
            <div className="absolute -top-1/2 -right-[10%] w-[300px] h-[300px] bg-white/10 rounded-full"></div>
            <div className="grid grid-cols-1 lg:grid-cols-[1fr_auto] gap-12 items-center relative z-10">
              <div className="flex flex-col gap-4">
                <div className="flex justify-end">
                  <span className="bg-green-500 text-white px-3 py-1.5 rounded-full text-sm font-semibold">
                    + 428 за день
                  </span>
                </div>
                <div className="text-6xl font-bold text-blue-700 leading-none">
                  1 534 129
                </div>
                <div className="text-xl text-gray-800 font-medium">
                  опубликованных котировочных сессий
                </div>
                <div className="text-lg text-gray-600 mt-2">
                  Среднее снижение цены в котировочной сессии — 14,1%
                </div>
                <Link 
                  to="/catalog" 
                  className="bg-red-600 text-white px-8 py-4 rounded-lg text-base font-semibold inline-block w-fit mt-4 transition-all hover:bg-red-700 hover:-translate-y-0.5 shadow-lg shadow-red-600/30 hover:shadow-xl hover:shadow-red-600/40 no-underline"
                >
                  Реестр котировочных сессий
                </Link>
              </div>
              <div className="flex flex-col items-center gap-4">
                <div className="w-[200px] h-[150px] bg-white rounded-lg p-4 shadow-lg relative">
                  <div className="w-full h-full bg-gray-100 rounded p-3">
                    <div className="flex flex-col gap-2 h-full">
                      <div className="h-3 bg-gray-300 rounded"></div>
                      <div className="h-3 bg-gray-300 rounded"></div>
                      <div className="h-3 bg-gray-300 rounded"></div>
                      <div className="absolute bottom-6 right-6 w-10 h-10 bg-red-600 text-white rounded flex items-center justify-center font-bold text-xl">
                        %
                      </div>
                    </div>
                  </div>
                </div>
                <div className="flex gap-3 items-center">
                  <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center text-xl shadow-md">
                    🔍
                  </div>
                  <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center text-xl shadow-md">
                    📊
                  </div>
                  <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center text-xl shadow-md">
                    💰
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default HomePage;
