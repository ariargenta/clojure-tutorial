(ns api.core
  (:require [api.config :as config]
            [com.stuartsierra.component :as component]
            [api.components.testing :as test-component]
            [api.components.pedestal-component :as pedestal-component]
            [api.components.in-memory-state-component :as in-memory-state-component]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defn api-system
  [config]
  (component/system-map
    :test-component
    (test-component/new-test-component config)
    :in-memory-state-component
    (in-memory-state-component/new-in-memory-state-component config)
    :data-source (connection/component HikariDataSource (:db-spec config))
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