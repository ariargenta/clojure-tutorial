(ns api.components.testing
  (:require [com.stuartsierra.component :as component]))

(defrecord TestComponent
  [config]
  component/Lifecycle

  (start [component]
    (println "Starting TestComponent")
    (assoc component :state ::started))

  (stop [component]
    (println "Stopping TestComponent")
    (assoc component :state nil)))

(defn new-test-component
  [config]
  (map->TestComponent {:config config}))