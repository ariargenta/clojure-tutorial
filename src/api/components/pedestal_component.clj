(ns api.components.pedestal-component
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn response
  [status body]
  {:status status
   :body body
   :headers nil})

(def ok (partial response 200))

(def get-todo-handler
  {:name :get-todo-handler
   :enter
   (fn [context]
     (let [request (:request context)
           response (ok context)]
       (assoc context :response response)))})

(comment
  [{:id (random-uuid)
    :name "My todo list"
    :items [{:id (random-uuid)
             :name "Make a new API"
             :status :created}]}
   {:id (random-uuid)
    :name "Empty todo list"
    :items []}])

(defn respond-hello
  [request]
  {:status 200
   :body "Hello, world!"})

(def routes
  (route/expand-routes
    #{["/greet" :get respond-hello :route-name :greet]
      ["/todo/:todo-id" :get get-todo-handler :route-name :get-todo]}))

(defrecord PedestalComponent
  [config
   testing
   in-memory-state-component]

  component/Lifecycle

  (start [component]
    (println "Starting PedestalComponent")
    (let [server (-> {::http/routes routes
                      ::http/type :jetty
                      ::http/join? false
                      ::http/port (-> config :server :port)}
                     (http/create-server)
                     (http/start))]
    (assoc component :server server)))

  (stop [component]
    (println "Stopping PedestalComponent")
    (when-let [server (:server component)]
      (http/stop server))
    (assoc component :server nil)))

(defn new-pedestal-component
  [config]
  (map->PedestalComponent {:config config}))