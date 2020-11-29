import "./index.css";
import "antd/dist/antd.css";

import { applyMiddleware, compose, createStore } from "redux";
import { persistReducer, persistStore } from "redux-persist";
import { Provider } from "react-redux";
import React, { useState, useEffect } from "react";
import ReactDOM from "react-dom";
import { BrowserRouter as Router } from "react-router-dom";
import createSagaMiddleware from "redux-saga";
import storage from "redux-persist/lib/storage";
import { Spin } from "antd";
import {authInterceptor} from "./middleware";
import rootReducer from "./reducers/rootReducer";
import rootSaga from "./sagas";
import Layout from "./components/layout/layout";
import * as serviceWorker from "./serviceWorker";

const sagaMiddleware = createSagaMiddleware();
const middlewares = [authInterceptor, sagaMiddleware];

const persistConfig = {
  key: "reducers",
  storage,
  whitelist: ["userReducer", "profileReducer"], // which reducer want to store
};
const pReducer = persistReducer(persistConfig, rootReducer);

const composeEnhancer = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;

export const store = createStore(
  pReducer,
  composeEnhancer(applyMiddleware(...middlewares))
);

sagaMiddleware.run(rootSaga);

const AppProvider = () => {
  const [rehydrated, setRehydrated] = useState(false);

  useEffect(() => {
    persistStore(store, {}, () => {
      setRehydrated(true);
    });
  }, []);

  if (!rehydrated) {
    return <Spin />;
  }
  return (
    <Provider store={store}>
      <Router>
        <Layout />
      </Router>
    </Provider>
  );
};

export default AppProvider;

ReactDOM.render(<AppProvider />, document.getElementById("root"));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
