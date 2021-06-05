(ns dda.c4k-keycloak.yaml
  (:require
   ["js-yaml" :as yaml]
   [shadow.resource :as rc]))

(def postgres-config (rc/inline "postgres/config.yaml"))

(def postgres-secret (rc/inline "postgres/secret.yaml"))

(def postgres-deployment (rc/inline "postgres/deployment.yaml"))

(def postgres-service (rc/inline "postgres/service.yaml"))

(def keycloak-secret (rc/inline "keycloak/secret.yaml"))

(def keycloak-deployment (rc/inline "keycloak/deployment.yaml"))

(def keycloak-certificate (rc/inline "keycloak/certificate.yaml"))

(def keycloak-ingress (rc/inline "keycloak/ingress.yaml"))

(def keycloak-service (rc/inline "keycloak/service.yaml"))

(def ingress-test (rc/inline "ingress_test.yaml"))

(defn load-resource [resource-name]
  (case resource-name
    "postgres/config.yaml" postgres-config
    "postgres/secret.yaml" postgres-secret
    "postgres/deployment.yaml" postgres-deployment
    "postgres/service.yaml" postgres-service
    "keycloak/secret.yaml" keycloak-secret
    "keycloak/deployment.yaml" keycloak-deployment
    "keycloak/certificate.yaml" keycloak-certificate
    "keycloak/ingress.yaml" keycloak-ingress
    "keycloak/service.yaml" keycloak-service
    "ingress_test.yaml" ingress-test))

(defn from-string [input]
  (js->clj (yaml/load input)
           :keywordize-keys true))

(defn to-string [edn]
  (yaml/dump (clj->js  edn)))