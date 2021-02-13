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
import ChevronRightIcon from "@material-ui/icons/ChevronRight";
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
  history: {
    padding: "20px 0",
  },
  historyContent: {
    padding: 20,
  },
  historyContentHeader: {
    "margin-bottom": 20,
  },
});

const App: React.FunctionComponent = () => {
  const classes = useStyles();

  const dispatch = useDispatch();
  const wordState = useSelector((state: RootState) => state.word);
  const relatedWordList = wordState.word.relatedWords.map((relatedWord) => (
    <Grid key={`related_word__${relatedWord.name}`} item>
      <Chip
        label={relatedWord.name}
        variant="outlined"
        onClick={() => {
          dispatch(fetchWord(relatedWord.name));
          dispatch(change("search", "query", relatedWord.name));
        }}
      />
    </Grid>
  ));
  const historyList = wordState.history.reduce<React.ReactNode[]>(
    (accumulator, hist, index, array) => {
      if (accumulator.length !== 0) {
        accumulator.push(
          // eslint-disable-next-line react/no-array-index-key
          <Grid key={`history_list__${index}__right`} item>
            <ChevronRightIcon key={hist.name} />
          </Grid>
        );
      }
      accumulator.push(
        // eslint-disable-next-line react/no-array-index-key
        <Grid key={`history_list__${index}__word`} item>
          <Chip
            label={hist.name}
            variant={index === array.length - 1 ? "default" : "outlined"}
            color="primary"
            onClick={() => {
              dispatch(fetchWord(hist.name, hist.index));
              dispatch(change("search", "query", hist.name));
            }}
          />
        </Grid>
      );
      return accumulator;
    },
    []
  );
  return (
    <Container>
      <Box>
        <Search />
      </Box>
      <Box className={classes.history}>
        <Paper className={classes.historyContent}>
          <Typography className={classes.historyContentHeader} variant="h5">
            History
          </Typography>
          <Grid container spacing={2} alignItems="center">
            {historyList}
          </Grid>
        </Paper>
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
