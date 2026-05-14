(ns api.core
  (:require [api.config :as config]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn respond-hello
  [request]
  {:status 200
   :body "Hello, world!"})

(def routes
  (route/expand-routes
    #{["/greet" :get respond-hello :route-name :greet]}))

(defn create-server
  [config]
  (http/create-server
    {::http/routes routes
     ::http/type :jetty
     ::http/join? false
     ::http/port (-> config :server :port)}))

(defn start
  [config]
  (http/start (create-server config)))

(defn -main
  []
  (let [config (config/read-config)]
  (println "Starting Clojure API service with configuration" config)
  (start config)))