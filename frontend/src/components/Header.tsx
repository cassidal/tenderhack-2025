import { Link } from 'react-router-dom';

const Header = () => {
  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto flex items-center justify-between px-6 py-3 gap-5">
        <div className="flex items-center">
          <Link to="/" className="flex items-center gap-3 no-underline text-black">
            <div className="w-10 h-10 relative">
              <div className="w-full h-full bg-red-600 border-4 border-white shadow-[0_0_0_2px_#d32f2f] relative">
                <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[60%] h-[60%] bg-white"></div>
              </div>
            </div>
            <span className="font-semibold text-base text-black">ПОРТАЛ ПОСТАВЩИКОВ</span>
          </Link>
        </div>
        
        <div className="flex-1 flex justify-center">
          <button className="bg-transparent border-none cursor-pointer p-2 flex flex-col gap-1">
            <span className="w-6 h-0.5 bg-gray-800 rounded"></span>
            <span className="w-6 h-0.5 bg-gray-800 rounded"></span>
            <span className="w-6 h-0.5 bg-gray-800 rounded"></span>
          </button>
        </div>
        
        <div className="flex items-center gap-4">
          <div className="flex gap-3 items-center">
            <button className="bg-transparent border-none cursor-pointer p-2 text-gray-600 hover:text-red-600 transition-colors flex items-center justify-center" title="Поиск">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M9 17C13.4183 17 17 13.4183 17 9C17 4.58172 13.4183 1 9 1C4.58172 1 1 4.58172 1 9C1 13.4183 4.58172 17 9 17Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M19 19L14.65 14.65" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
            <button className="bg-transparent border-none cursor-pointer p-2 text-gray-600 hover:text-red-600 transition-colors flex items-center justify-center" title="Поддержка">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M10 18C14.4183 18 18 14.4183 18 10C18 5.58172 14.4183 2 10 2C5.58172 2 2 5.58172 2 10C2 14.4183 5.58172 18 10 18Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M10 14V10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M10 6H10.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
            <button className="bg-transparent border-none cursor-pointer p-2 text-gray-600 hover:text-red-600 transition-colors flex items-center justify-center" title="Предложения">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M10 2L12.09 7.26L18 8.27L14 12.14L14.91 18.02L10 15.77L5.09 18.02L6 12.14L2 8.27L7.91 7.26L10 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
            <button className="bg-transparent border-none cursor-pointer p-2 text-gray-600 hover:text-red-600 transition-colors flex items-center justify-center" title="Отправить">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M18 2L9 11" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                <path d="M18 2L12 18L9 11L2 8L18 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
            <button className="bg-transparent border-none cursor-pointer p-2 text-gray-600 hover:text-red-600 transition-colors flex items-center justify-center" title="Корзина">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M3 3H5L5.4 5M7 13H17L19 5H5.4M7 13L5.4 5M7 13L4.7 15.3C4.3 15.7 4.6 16.5 5.1 16.5H17M17 16.5C16.2 16.5 15.5 17.2 15.5 18C15.5 18.8 16.2 19.5 17 19.5C17.8 19.5 18.5 18.8 18.5 18C18.5 17.2 17.8 16.5 17 16.5Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
          </div>
          
          <div className="flex items-center">
            <button className="bg-transparent border-none cursor-pointer text-xl p-1">🇷🇺</button>
          </div>
          
          <div className="flex gap-3 items-center">
            <Link to="/login" className="px-5 py-2.5 rounded bg-blue-50 text-blue-700 font-medium text-sm transition-colors hover:bg-blue-100 no-underline">
              Войти
            </Link>
            <Link to="/register" className="px-5 py-2.5 rounded bg-red-600 text-white font-medium text-sm transition-colors hover:bg-red-700 no-underline">
              Зарегистрироваться
            </Link>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
