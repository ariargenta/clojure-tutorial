(ns api.core
  (:require [api.config :as config]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [com.stuartsierra.component :as component]
            [api.components.testing :as test-component]))

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

(defn api-system
  [config]
  (component/system-map
    :test-component (test-component/new-test-component config)))

(defn -main
  []
  (let [system (-> (config/read-config)
                   (api-system)
                   (component/start-system))]
  (println "Starting Clojure API service with configuration")
  (.addShutdownHook
    (Runtime/getRuntime)
    (new Thread #(component/stop-system system)))))