(ns api.core
  (:require [api.config :as config]))

(defn -main
  []
  (let [config (config/read-config)]
  (println "Starting Clojure API service with configuration" config)))