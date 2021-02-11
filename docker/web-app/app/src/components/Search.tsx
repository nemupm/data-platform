import React from "react";
import { Field, getFormValues, reduxForm } from "redux-form";
import { useDispatch, useSelector } from "react-redux";
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
  <div>
    {/* eslint-disable-next-line react/jsx-props-no-spreading */}
    <input {...input} type={type} placeholder={placeholder} />
  </div>
);

const Search: React.FunctionComponent = () => {
  /* eslint-disable-next-line @typescript-eslint/no-explicit-any */
  const query: any = useSelector((state) => getFormValues("search")(state));
  const dispatch = useDispatch();
  return (
    <div>
      <Field
        name="query"
        type="text"
        component={renderField}
        placeholder="Search interest"
      />
      <button
        type="button"
        onClick={() => query && dispatch(fetchWord(query.query))}
      >
        search
      </button>
    </div>
  );
};

export default reduxForm({
  form: "search",
})(Search);
