(ns component.api.info-handler-component-test
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [api.core :as core]
            [clj-http.client :as client]
            [api.components.pedestal-component :refer [url-for]])
  (:import (java.net ServerSocket)
           (org.testcontainers.containers PostgreSQLContainer)))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(defn sut->url
  [sut path]
  (str/join ["http://localhost:"
             (-> sut :pedestal-component :config :server :port) path]))

(defn get-free-port
  []
  (with-open [socket (ServerSocket. 0)]
    (.getLocalPort socket)))

(deftest info-handler-test
  (let [database-container (doto (PostgreSQLContainer. "postgres:18.1")
                            (.withDatabaseName "api-db")
                            (.withUsername "test")
                            (.withPassword "test"))]
    (try
      (.start database-container)
      (with-system
        [sut (core/api-system
               {:server {:port (get-free-port)}
               :db-spec {:jdbcUrl (.getJdbcUrl database-container)
                         :username (.getUsername database-container)
                         :password (.getPassword database-container)}})]
        (is (= {:body "Database server version: 18.1 (Debian 18.1-1.pgdg13+2)"
              :status 200}
             (-> (sut->url sut (url-for :info))
                 (client/get {:accept :json})
                 (select-keys [:body :status])))))
      (finally
        (.stop database-container)))))