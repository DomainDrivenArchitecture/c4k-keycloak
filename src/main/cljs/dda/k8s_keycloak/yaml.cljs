(ns dda.k8s-keycloak.yaml
  (:require 
   ["js-yaml" :as yaml]
   [shadow.resource :as rc]
   ))

(def config (rc/inline "config.yaml"))

(def cron (rc/inline "cron.yaml"))

(def deployment (rc/inline "deployment.yaml"))

(defn load-resource [resource-name]
  (case resource-name
    "config.yaml" config
    "cron.yaml" cron
    "deployment.yaml" deployment))

(defn from-string [input]
  (js->clj (yaml/load input)
           :keywordize-keys true))

(defn to-string [edn]
  (yaml/dump (clj->js  edn)))