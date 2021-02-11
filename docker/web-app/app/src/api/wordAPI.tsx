import axios from "axios";
import { Word } from "../models/word";

axios.defaults.baseURL = "http://localhost:3000";
axios.defaults.headers.get["Content-Type"] = "application/json;charset=utf-8";
axios.defaults.headers.get["Access-Control-Allow-Origin"] = "*";

export interface WordCooccurrence {
  word: string;
  cnt: number;
  // eslint-disable-next-line camelcase
  co_cnt: { [key: string]: number };
}

export interface WordAPIResponse<T> {
  data: T;
  status: string;
}

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export async function fetchWordCooccurrence(query: string) {
  const url = `/api/v1/word_cooccurrences`;

  const { data } = await axios.get(url, {
    params: {
      word: query,
    },
  });
  return data;
}

export function getWordFromResponse(
  res: WordAPIResponse<WordCooccurrence>
): Word {
  return {
    name: res.data.word,
    relatedWords: Object.keys(res.data.co_cnt)
      .map((name) => {
        const cnt = res.data.co_cnt[name];
        return { name, score: cnt * name.length, cnt };
      })
      .sort((a, b) => (a.score < b.score ? 1 : -1)),
    cnt: res.data.cnt,
  };
}
