import React from "react";
import "./App.css";
import { useDispatch, useSelector } from "react-redux";
import Search from "./components/Search";
import { RootState } from "./stores";
import { fetchWord } from "./stores/word";

const App: React.FunctionComponent = () => {
  const dispatch = useDispatch();
  const wordState = useSelector((state: RootState) => state.word);
  const relatedWordList = wordState.word.relatedWords.map((relatedWord) => (
    <button
      type="button"
      className="related-word"
      key={relatedWord.name}
      onClick={() => dispatch(fetchWord(relatedWord.name))}
    >
      {relatedWord.name}
    </button>
  ));
  return (
    <div className="App">
      <div className="header">
        <Search />
        <div className="current-word">{wordState.word.name}</div>
      </div>
      <div className="container">
        <ul>{relatedWordList}</ul>
      </div>
    </div>
  );
};

export default App;
