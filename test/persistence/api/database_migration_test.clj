(ns persistence.api.database-migration-test
  (:require [api.core :as core]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import (org.testcontainers.containers PostgreSQLContainer)))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(defn datasource-only-system
  [config]
  (component/system-map
    :data-source (core/datasource-component config)))

(defn create-database-container
  []
  (PostgreSQLContainer. "postgres:18.1"))

(deftest migration-test
  (let [database-container (create-database-container)]
    (try
      (.start database-container)
      (with-system
        [sut (datasource-only-system
               {:db-spec {:jdbcUrl (.getJdbcUrl database-container)
                          :username (.getUsername database-container)
                          :password (.getPassword database-container)}})]
        (let [{:keys [data-source]} sut
              [schema-version :as schema-versions]
              (jdbc/execute!
                (data-source)
                ["SELECT * FROM schema_version"]
                {:builder-fn rs/as-unqualified-lower-maps})]
          (is (= 1 (count schema-versions)))
          (is (= {:description "add todo tables"
                  :script "V1__add_todo_tables.sql"
                  :success true}
                 (select-keys schema-version [:description :script :success])))))
      (finally
        (.stop database-container)))))

(deftest todo-table-test
  (let [database-container (create-database-container)]
    (try
      (.start database-container)
      (with-system
        [sut (datasource-only-system
               {:db-spec {:jdbcUrl (.getJdbcUrl database-container)
                          :username (.getUsername database-container)
                          :password (.getPassword database-container)}})]
        (let [{:keys [data-source]} sut
              insert-results (jdbc/execute!
                               (data-source)
                               ["
                               INSERT INTO todo (title)
                               VALUES ('my todo list'),
                               ('other todo list')
                               returning *
                               "]
                               {:builder-fn rs/as-unqualified-lower-maps})
              select-results (jdbc/execute!
                               (data-source)
                               ["
                               SELECT * FROM todo"]
                               {:builder-fn rs/as-unqualified-lower-maps})]
          (is (= 2
                 (count insert-results)
                 (count select-results)))
          (is (= #{"my todo list"
                    "other todo list"}
                 (->> insert-results (map :title) (into #{}))
                 (->> select-results (map :title) (into #{}))))))
      (finally
        (.stop database-container)))))