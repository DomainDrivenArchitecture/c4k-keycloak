(ns dda.c4k-keycloak.keycloak
 (:require
  [clojure.spec.alpha :as s]
  [dda.c4k-keycloak.yaml :as yaml]
  [dda.c4k-keycloak.base64 :as b64]
  [dda.c4k-keycloak.common :as cm]))

(s/def ::keycloak-admin-user cm/bash-env-string?)
(s/def ::keycloak-admin-password cm/bash-env-string?)
(s/def ::fqdn cm/fqdn-string?)
(s/def ::issuer cm/letsencrypt-issuer?)

(defn generate-secret [my-auth]
  (let [{:keys [keycloak-admin-user keycloak-admin-password]} my-auth]
  (->
   (yaml/from-string (yaml/load-resource "keycloak/secret.yaml"))
   (cm/replace-key-value :keycloak-user (b64/encode keycloak-admin-user))
   (cm/replace-key-value :keycloak-password (b64/encode keycloak-admin-password)))))

(defn generate-deployment []
  (yaml/from-string (yaml/load-resource "keycloak/deployment.yaml")))

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
