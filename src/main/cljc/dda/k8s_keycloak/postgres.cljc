(ns dda.k8s-keycloak.postgres
  (:require
   [clojure.spec.alpha :as s]
   [dda.k8s-keycloak.yaml :as yaml]
   [dda.k8s-keycloak.common :as cm]))

(s/def ::postgres-db-user cm/bash-env-string?)
(s/def ::postgres-db-password cm/bash-env-string?)

(defn generate-config []
   (yaml/from-string (yaml/load-resource "postgres/config.yaml")))

(defn generate-deployment [my-auth]
  (let [{:keys [postgres-db-user postgres-db-password]} my-auth]
    (->
     (yaml/from-string (yaml/load-resource "postgres/deployment.yaml"))
     (cm/replace-named-value "POSTGRES_USER" postgres-db-user)
     (cm/replace-named-value "POSTGRES_PASSWORD" postgres-db-password))))

(defn generate-service []
  (yaml/from-string (yaml/load-resource "postgres/service.yaml")))
