import React from "react";
import logo from "./logo.svg";
import "./App.css";
import Search from "./components/Search";

const App: React.FunctionComponent = () => {
  return (
    <div className="App">
      <header className="App-header">
        <Search />
        <img src={logo} className="App-logo" alt="logo" />
      </header>
    </div>
  );
};

export default App;
