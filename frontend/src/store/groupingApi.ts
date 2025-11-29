import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export interface GroupingRequest {
  query: string;
}

export interface GroupingResponse {
  taskId: string;
}

export interface RegenerateRequest {
  query: string;
}

export interface FilterOption {
  id: string;
  name: string;
  values?: string[];
}

export interface ProductCard {
  id: string;
  name: string;
  url?: string;
  image?: string;
  description?: string;
  [key: string]: any;
}

export interface GroupingResults {
  cards: ProductCard[];
}

export interface GroupingFilters {
  filters: FilterOption[];
}

export interface RateRequest {
  rating: number;
}

export const groupingApi = createApi({
  reducerPath: 'groupingApi',
  baseQuery: fetchBaseQuery({
    baseUrl: API_BASE_URL,
    prepareHeaders: (headers) => {
      // Здесь можно добавить авторизационные заголовки если нужно
      headers.set('Content-Type', 'application/json');
      return headers;
    },
  }),
  tagTypes: ['Grouping'],
  endpoints: (builder) => ({
    // 1. Отправка запроса на группировку
    requestGrouping: builder.mutation<GroupingResponse, GroupingRequest>({
      query: (body) => ({
        url: '/grouping/request',
        method: 'POST',
        body,
      }),
    }),

    // 3. Получение результатов группировки
    getResults: builder.query<GroupingResults, string>({
      query: (taskId) => `/grouping/${taskId}/results`,
      providesTags: ['Grouping'],
    }),

    // 3. Получение фильтров
    getFilters: builder.query<GroupingFilters, string>({
      query: (taskId) => `/grouping/${taskId}/filters`,
      providesTags: ['Grouping'],
    }),

    // 4. Регенерация данных
    regenerate: builder.mutation<GroupingResponse, { taskId: string; body: RegenerateRequest }>({
      query: ({ taskId, body }) => ({
        url: `/grouping/${taskId}/regenerate`,
        method: 'POST',
        body,
      }),
      invalidatesTags: ['Grouping'],
    }),

    // 5. Апрув данных
    approve: builder.mutation<void, string>({
      query: (taskId) => ({
        url: `/grouping/${taskId}/approve`,
        method: 'POST',
      }),
    }),

    // 6. Оценка данных
    rate: builder.mutation<void, { taskId: string; body: RateRequest }>({
      query: ({ taskId, body }) => ({
        url: `/grouping/${taskId}/rate`,
        method: 'POST',
        body,
      }),
    }),
  }),
});

export const {
  useRequestGroupingMutation,
  useGetResultsQuery,
  useGetFiltersQuery,
  useRegenerateMutation,
  useApproveMutation,
  useRateMutation,
  useLazyGetResultsQuery,
  useLazyGetFiltersQuery,
} = groupingApi;

