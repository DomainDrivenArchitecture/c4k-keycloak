(ns dda.k8s-keycloak.core
 (:require
  [clojure.string :as cs] 
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.k8s-keycloak.yaml :as yaml]
   [dda.k8s-keycloak.common :as cm]
   [dda.k8s-keycloak.postgres :as pg]))

(s/def ::keycloak-admin-user cm/bash-env-string?)
(s/def ::keycloak-admin-password cm/bash-env-string?)
(s/def ::fqdn cm/fqdn-string?)
(s/def ::issuer cm/letsencrypt-issuer?)

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer]))

(def auth? (s/keys :req-un [::keycloak-admin-user ::keycloak-admin-password
                            ::pg/postgres-db-user ::pg/postgres-db-password]))

(defn generate-config [my-config my-auth]
  (->
   (yaml/from-string (yaml/load-resource "config.yaml"))
   (assoc-in [:data :config.edn] (str my-config))
   (assoc-in [:data :credentials.edn] (str my-auth))))

(defn generate-deployment [my-auth]
  (let [{:keys [postgres-db-user postgres-db-password
                keycloak-admin-user keycloak-admin-password]} my-auth]
    (->
     (yaml/from-string (yaml/load-resource "keycloak/deployment.yaml"))
     (cm/replace-named-value "KEYCLOAK_USER" keycloak-admin-user)
     (cm/replace-named-value "DB_USER" postgres-db-user)
     (cm/replace-named-value "DB_PASSWORD" postgres-db-password)
     (cm/replace-named-value "KEYCLOAK_PASSWORD" keycloak-admin-password))))

(defn generate-certificate [config]
  (let [{:keys [fqdn issuer]
         :or {issuer :staging}} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "keycloak/certificate.yaml"))
     (assoc-in [:spec :commonName] fqdn)
     (assoc-in [:spec :dnsNames] [fqdn])
     (assoc-in [:spec :issuerRef :name] letsencrypt-issuer))))

(defn generate-ingress [config]
  (let [{:keys [fqdn issuer]
         :or {issuer :staging}} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "keycloak/ingress.yaml"))
     (assoc-in [:metadata :annotations :cert-manager.io/cluster-issuer] letsencrypt-issuer)
     (cm/replace-all-matching-values-by-new-value "fqdn" fqdn))))

(defn generate-service []
  (yaml/from-string (yaml/load-resource "keycloak/service.yaml")))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (cs/join "\n"
           [(yaml/to-string (pg/generate-config))
            "---"
            (yaml/to-string (pg/generate-service))
            "---"
            (yaml/to-string (pg/generate-deployment my-auth))
            "---"
            (yaml/to-string (generate-config my-config my-auth))
            "---"
            (yaml/to-string (generate-certificate my-config))
            "---"
            (yaml/to-string (generate-ingress my-config))
            "---"
            (yaml/to-string (generate-service))
            "---"
            (yaml/to-string (generate-deployment my-auth))]))