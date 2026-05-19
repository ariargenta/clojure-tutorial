(ns component.api.api-component-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [api.core :as core]
            [clj-http.client :as client]))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(deftest greeting-test
  (with-system
    [sut (core/api-system {:server {:port 8088}})]
    (is (= {:body "Hello, world!"
            :status 200}
           (-> (str "http://localhost:" 8088 "/greet")
           (client/get)
           (select-keys [:body :status]))))))


(deftest get-todo-test
  (let [todo-id-1 (random-uuid)
        todo-1 {:id todo-id-1
                :name "To do test"
                :items [{:id (random-uuid)
                         :name "Finish the test"}]}]
    (with-system
      [sut (core/api-system {:server {:port 8088}})]
      (reset! (-> sut :in-memory-state-component :state-atom) [todo-1])
      (is (= {:body (pr-str todo-1)
              :status 200}
             (-> (str "http://localhost:" 8088 "/todo/" todo-id-1)
                 (client/get)
                 (select-keys [:body :status]))))
     (testing "Empty body returned for random id")(is (= {:body ""
              :status 200}
             (-> (str "http://localhost:" 8088 "/todo/" (random-uuid))
                 (client/get)
                 (select-keys [:body :status])))))))

(deftest simple-api-test
  (is (= 1 1)))