(ns dev
  (:require [com.stuartsierra.component.repl :as component-repl]
            [api.core :as core]))

(component-repl/set-init
  (fn [_]
    (core/api-system {:server {:port 3001}
                      :htmx {:server {:port 3002}}
                      :db-spec {:jdbcUrl "jdbc:postgresql://localhost:5432/cljapi"
                                :username "cljapi"
                                :password "cljapi"}})))