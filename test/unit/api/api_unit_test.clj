(ns unit.api.api-unit-test
  (:require [clojure.test :refer :all]
            [api.components.pedestal-component :refer [url-for]]))

(deftest url-for-test
  (testing "Greet endpoint url"
    (is (= "/greet" (url-for :greet)))

    (testing "Get todo by id endpoint url"
      (let [todo-id (random-uuid)]
        (is (= (str "/todo/" todo-id)
               (url-for :get-todo {:path-params {:todo-id todo-id}})))))))

(deftest simple-passing-test
  (is (= 1 1)))