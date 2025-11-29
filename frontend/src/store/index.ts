import { configureStore } from '@reduxjs/toolkit';
import { groupingApi } from './groupingApi';

export const store = configureStore({
  reducer: {
    [groupingApi.reducerPath]: groupingApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(groupingApi.middleware),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

