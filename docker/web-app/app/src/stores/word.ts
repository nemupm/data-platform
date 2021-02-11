import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { AppThunk } from "./index";
import { Word } from "../models/word";
import {
  fetchWordCooccurrence,
  getWordFromResponse,
  WordAPIResponse,
  WordCooccurrence,
} from "../api/wordAPI";

interface History {
  name: string;
  index: number;
}

interface WordState {
  word: Word;
  history: History[];
  error: string;
}

const initialState: WordState = {
  word: <Word>{
    name: "",
    relatedWords: [],
    cnt: 0,
  },
  history: [],
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
      action: PayloadAction<{
        res: WordAPIResponse<WordCooccurrence>;
        index: number;
      }>
    ) {
      // eslint-disable-next-line no-param-reassign
      state.word = getWordFromResponse(action.payload.res);
      if (action.payload.index !== -1) {
        // eslint-disable-next-line no-param-reassign
        state.history = state.history.filter(
          (hist) => hist.index < action.payload.index
        );
      }
      state.history.push({
        name: state.word.name,
        index: state.history.length,
      });
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

export const fetchWord = (query: string, index = -1): AppThunk => async (
  dispatch
) => {
  try {
    const res = await fetchWordCooccurrence(query);
    dispatch(fetchWordCooccurrenceSuccess({ res, index }));
  } catch (err) {
    dispatch(fetchWordCooccurrenceFailed(err.toString()));
  }
};
