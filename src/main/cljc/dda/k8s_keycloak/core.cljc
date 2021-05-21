(ns dda.k8s-keycloak.core
  (:require
   [clojure.string :as cs]
   [clojure.spec.alpha :as s]
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.k8s-keycloak.yaml :as yaml]))

(def config? any?)

(def auth? any?)

(defn generate-config [my-config my-auth]
  (->
   (yaml/from-string (yaml/load-resource "config.yaml"))
   (assoc-in [:data :config.edn] (str my-config))
   (assoc-in [ :data :credentials.edn] (str my-auth))
   ))

(defn generate-deployment []
  (yaml/from-string (yaml/load-resource "deployment.yaml")))

(defn generate-cron []
  (yaml/from-string (yaml/load-resource "cron.yaml")))

(defn-spec generate any?
  [my-config string?
   my-auth string?] 
  (cs/join "\n" 
           [(yaml/to-string (generate-config my-config my-auth))
            "---"
            (yaml/to-string (generate-cron))]))
