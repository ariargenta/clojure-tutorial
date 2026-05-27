(ns api.core
  (:require [api.config :as config]
            [com.stuartsierra.component :as component]
            [api.components.testing :as test-component]
            [api.components.pedestal-component :as pedestal-component]
            [api.components.in-memory-state-component :as in-memory-state-component]
            [next.jdbc.connection :as connection]
            [clojure.tools.logging :as log])
  (:import (com.zaxxer.hikari HikariDataSource)
           (org.flywaydb.core Flyway)))

(defn datasource-component
  [config]
  (connection/component
    HikariDataSource
    (assoc (:db-spec config)
      :init-fn (fn [datasource]
                 (log/info "Running database init")
                 (.migrate
                   (.. (Flyway/configure)
                       (dataSource datasource)
                       (locations (into-array String
                                              ["classpath:database/migrations"]))
                       (table "schema_version")
                       (load)))))))

(defn api-system
  [config]
  (component/system-map
    :test-component
    (test-component/new-test-component config)
    :in-memory-state-component
    (in-memory-state-component/new-in-memory-state-component config)
    :data-source (datasource-component config)
    :pedestal-component
    (component/using
      (pedestal-component/new-pedestal-component config)
                     [:test-component
                      :data-source
                      :in-memory-state-component])))

(defn -main
  []
  (let [system (-> (config/read-config)
                   (api-system)
                   (component/start-system))]
  (println "Starting Clojure API service with configuration")
  (.addShutdownHook
    (Runtime/getRuntime)
    (new Thread #(component/stop-system system)))))