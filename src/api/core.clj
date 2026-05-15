(ns api.core
  (:require [api.config :as config]
            [com.stuartsierra.component :as component]
            [api.components.testing :as test-component]
            [api.components.pedestal-component :as pedestal-component]))

(defn api-system
  [config]
  (component/system-map
    :test-component (test-component/new-test-component config)
    :pedestal-component
    (component/using (pedestal-component/new-pedestal-component config)
                     [:test-component])))

(defn -main
  []
  (let [system (-> (config/read-config)
                   (api-system)
                   (component/start-system))]
  (println "Starting Clojure API service with configuration")
  (.addShutdownHook
    (Runtime/getRuntime)
    (new Thread #(component/stop-system system)))))