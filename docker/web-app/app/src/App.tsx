import React from "react";
import { useDispatch, useSelector } from "react-redux";
import {
  Box,
  Chip,
  Container,
  Grid,
  makeStyles,
  Paper,
  Typography,
} from "@material-ui/core";
import { change } from "redux-form";
import Search from "./components/Search";
import { RootState } from "./stores";
import { fetchWord } from "./stores/word";

const useStyles = makeStyles({
  wordList: {
    height: "80vh",
    padding: "20px 0",
  },
  wordListContent: {
    padding: 20,
    overflow: "scroll",
  },
  wordListContentHeader: {
    "margin-bottom": 20,
  },
});

const App: React.FunctionComponent = () => {
  const classes = useStyles();

  const dispatch = useDispatch();
  const wordState = useSelector((state: RootState) => state.word);
  const relatedWordList = wordState.word.relatedWords.map((relatedWord) => (
    <Grid key={relatedWord.name} item>
      <Chip
        label={relatedWord.name}
        onClick={() => {
          dispatch(fetchWord(relatedWord.name));
          dispatch(change("search", "query", relatedWord.name));
        }}
      />
    </Grid>
  ));
  return (
    <Container>
      <Box>
        <Search />
      </Box>
      <Box className={classes.wordList}>
        <Paper className={classes.wordListContent}>
          <Typography className={classes.wordListContentHeader} variant="h5">
            Related words
          </Typography>
          <Grid container spacing={2}>
            {relatedWordList}
          </Grid>
        </Paper>
      </Box>
    </Container>
  );
};

export default App;
