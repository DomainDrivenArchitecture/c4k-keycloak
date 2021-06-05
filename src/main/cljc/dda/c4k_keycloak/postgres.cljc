(ns dda.c4k-keycloak.postgres
  (:require
   [clojure.spec.alpha :as s]
   [dda.c4k-keycloak.yaml :as yaml]
   [dda.c4k-keycloak.base64 :as b64]
   [dda.c4k-keycloak.common :as cm]))

(s/def ::postgres-db-user cm/bash-env-string?)
(s/def ::postgres-db-password cm/bash-env-string?)

(defn generate-config []
   (yaml/from-string (yaml/load-resource "postgres/config.yaml")))

(defn generate-secret [my-auth]
  (let [{:keys [postgres-db-user postgres-db-password]} my-auth]
    (->
     (yaml/from-string (yaml/load-resource "postgres/secret.yaml"))
     (cm/replace-key-value :postgres-user (b64/encode postgres-db-user))
     (cm/replace-key-value :postgres-password (b64/encode postgres-db-password)))))

(defn generate-deployment []
  (yaml/from-string (yaml/load-resource "postgres/deployment.yaml")))

(defn generate-service []
  (yaml/from-string (yaml/load-resource "postgres/service.yaml")))
