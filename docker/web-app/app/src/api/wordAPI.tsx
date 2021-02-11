import axios from "axios";

axios.defaults.baseURL = "http://localhost:3000";
axios.defaults.headers.get["Content-Type"] = "application/json;charset=utf-8";
axios.defaults.headers.get["Access-Control-Allow-Origin"] = "*";

export interface WordCooccurrence {
  word: string;
  cnt: number;
  // eslint-disable-next-line camelcase
  co_cnt: { [key: string]: number };
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
