// eslint-disable-next-line import/no-extraneous-dependencies
import { combineReducers } from "redux";
import { configureStore, ThunkAction, Action } from "@reduxjs/toolkit";
import { reducer as formReducer } from "redux-form";
import wordReducer from "./word";

const rootReducer = combineReducers({
  word: wordReducer,
  form: formReducer,
});

const store = configureStore({ reducer: rootReducer });
export default store;

export type RootState = ReturnType<typeof store.getState>;
export type AppThunk<ReturnType = void> = ThunkAction<
  ReturnType,
  RootState,
  unknown,
  Action<string>
>;
