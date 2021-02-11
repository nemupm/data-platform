import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { AppThunk } from "./index";
import { Word } from "../models/word";
import {
  fetchWordCooccurrence,
  getWordFromResponse,
  WordAPIResponse,
  WordCooccurrence,
} from "../api/wordAPI";

interface WordState {
  word: Word;
  error: string;
}

const initialState: WordState = {
  word: <Word>{
    name: "test",
    relatedWords: [],
    cnt: 0,
  },
  error: "",
};

export const wordSlice = createSlice({
  name: "word",
  initialState,
  reducers: {
    setWord: (state, action: PayloadAction<Word>) => {
      // eslint-disable-next-line no-param-reassign
      state.word = action.payload;
    },
    fetchWordCooccurrenceSuccess(
      state,
      action: PayloadAction<WordAPIResponse<WordCooccurrence>>
    ) {
      // eslint-disable-next-line no-param-reassign
      state.word = getWordFromResponse(action.payload);
    },
    fetchWordCooccurrenceFailed(state, action: PayloadAction<string>) {
      // eslint-disable-next-line no-param-reassign
      state.error = action.payload;
    },
  },
});

export const {
  setWord,
  fetchWordCooccurrenceSuccess,
  fetchWordCooccurrenceFailed,
} = wordSlice.actions;

// export const selectCount = (state: RootState) => state.counter.value;

export default wordSlice.reducer;

export const fetchWord = (query: string): AppThunk => async (dispatch) => {
  try {
    const res = await fetchWordCooccurrence(query);
    dispatch(fetchWordCooccurrenceSuccess(res));
  } catch (err) {
    dispatch(fetchWordCooccurrenceFailed(err.toString()));
  }
};
