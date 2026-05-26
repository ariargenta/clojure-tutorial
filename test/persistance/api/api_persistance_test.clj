(ns persistance.api.api-persistance-test
  (:require [clojure.test :refer :all]
            [next.jdbc :as jdbc])
  (:import (org.testcontainers.containers PostgreSQLContainer)))

(deftest simple-passing-test
  (let [database-container (doto (PostgreSQLContainer. "postgres:18.1")
                             (.withDatabaseName "api-db")
                             (.withUsername "test")
                             (.withPassword "test"))]
    (try
      (.start database-container)
      (let [ds (jdbc/get-datasource {:jdbcUrl (.getJdbcUrl database-container)
                                     :user (.getUsername database-container)
                                     :password (.getPassword database-container)})]
        (is (= {:r 1} (first (jdbc/execute! ds ["select 1 as r;"])))))
      (finally
        (.stop database-container)))))