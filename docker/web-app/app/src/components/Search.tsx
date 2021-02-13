import React from "react";
import { Field, getFormValues, reduxForm } from "redux-form";
import { useDispatch, useSelector } from "react-redux";
import { Button, Grid, Paper, TextField } from "@material-ui/core";
import { fetchWord } from "../stores/word";

const renderField = ({
  input,
  placeholder,
  type,
}: {
  input: unknown;
  placeholder: string;
  type: string;
}) => (
  <TextField
    type={type}
    placeholder={placeholder}
    variant="outlined"
    /* eslint-disable-next-line react/jsx-props-no-spreading */
    {...input}
  />
);

const Search: React.FunctionComponent = () => {
  /* eslint-disable-next-line @typescript-eslint/no-explicit-any */
  const query: any = useSelector((state) => getFormValues("search")(state));
  const dispatch = useDispatch();
  return (
    <Paper style={{ padding: 20 }}>
      <Grid
        container
        spacing={3}
        direction="row"
        justify="center"
        alignItems="center"
      >
        <Grid item>
          <Field
            name="query"
            type="text"
            component={renderField}
            placeholder="Input word"
          />
        </Grid>
        <Grid item>
          <Button
            variant="contained"
            color="primary"
            onClick={() => query && dispatch(fetchWord(query.query))}
          >
            search
          </Button>
        </Grid>
      </Grid>
    </Paper>
  );
};

export default reduxForm({
  form: "search",
})(Search);
