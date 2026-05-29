(ns api.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [malli.core :as m]
            [malli.error :as me]
            [malli.util :as mu]))

(defn read-config
  []
  (-> "config.edn"
      (io/resource)
      (aero/read-config)))

(defmethod aero/reader 'csv-set
  [_opts _tag value]
  (if (str/blank? value)
      #{}
      (->> (str/split value #",")
           (remove str/blank?)
           (map str/trim)
           (into #{}))))

(defmethod aero/reader 'csv-keyword-set
  [_opts _tag value]
  (if (str/blank? value)
    #{}
    (->> (str/split value #",")
         (remove str/blank?)
         (map str/trim)
         (map keyword)
         (into #{}))))

(def kafka-config-base-schema
  (m/schema
    [:map
     [:bootstrap.servers :string]
     [:application.id :string]
     [:auto.offset.reset [:enum "earliest" "latest"]]
     [:producer.acks [:enum "0" "1" "all"]]]))

(def kafka-config-schema
  (m/schema
    [:multi {:dispatch :security.protocol}
     ["SSL"
      (mu/merge
        kafka-config-base-schema
        [:map
         [:security.protocol [:enum "SSL"]]
         [:ssl.keystore.type [:enum "PKCS12"]]
         [:ssl.truestore.type [:enum "JKS"]]
         [:ssl.keystore.location :string]
         [:ssl.keystore.password :string]
         [:ssl.key.password :string]
         [:ssl.truestore.location :string]
         [:ssl.truestore.password :string]])]
     ["PLAINTEXT"
      (mu/merge
        kafka-config-base-schema
        [:map [:security.protocol [:enum "PLAINTEXT"]]])]]))

;; Config schema

(def config-schema
  (m/schema
    [:map
     [:server [:map
               [:port [:int
                       {:min 1
                        :max 10000}]]]]
     [:htmx [:map
             [:server [:map
                       [:port [:int {:min 1 :max 10000}]]]]]]
     [:input-topics [:set :string]]
     [:kafka kafka-config-schema]]))

(def valid-config?
  (m/validator config-schema))

(defn assert-valid-config!
  [config]
  (if (valid-config? config)
    config
    (->> {:error (me/humanize (m/explain config-schema config))}
         (ex-info "Configuration is not valid")
         (throw))))

;:server {:port #long #or [#env CLJAPI_SERVER_PORT 8080]}
;:htmx {:server {:port #long #or [#env CLJAPI_HTMX_SERVER_PORT 8081]}}
;:input-topics #csv-set #env CLJAPI_INPUT_TOPICS

(comment
  (let [config {:server {:port 8080}
                 :htmx {:server {:port 3000}}
                 :input-topics #{"topic-1"}
                 :kafka {:bootstrap.servers "kafka"
                         :application.id "my-app",
                         :auto.offset.reset "earliest",
                         :producer.acks "all"
                         :ssl.keystore.type "PKCS12"
                         :ssl.truestore.type "JKS"
                         :ssl.keystore.location "location"
                         :ssl.keystore.password "password"
                         :ssl.key.password "password"
                         :ssl.truestore.location "location"
                         :ssl.truestore.password "password"
                         :security.protocol "SSL"}}]
  (assert-valid-config! config)))

(comment
  (read-config)
  (aero/reader {} 'csv-set "a, b, c,,d"))