(ns dda.c4k-keycloak.keycloak
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [dda.c4k-common.macros :refer-macros [inline-resources]])
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.base64 :as b64]
  [dda.c4k-common.ingress :as ing]
  [dda.c4k-common.predicate :as cp]))

(s/def ::fqdn cp/fqdn-string?)
(s/def ::namespace string?)
(s/def ::issuer cp/letsencrypt-issuer?)
(s/def ::keycloak-admin-user cp/bash-env-string?)
(s/def ::keycloak-admin-password cp/bash-env-string?)

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer
                              ::namespace]))

(def auth? (s/keys :req-un [::keycloak-admin-user 
                            ::keycloak-admin-password]))

#?(:cljs
   (defmethod yaml/load-resource :keycloak [resource-name]
     (get (inline-resources "keycloak") resource-name)))

(defn-spec generate-ingress cp/map-or-seq?
  [config config?]
  (ing/generate-ingress-and-cert
   (merge
    {:service-name "keycloak"
     :service-port 80
     :fqdns [(:fqdn config)]}
    config)))

(defn-spec generate-secret cp/map-or-seq?
  [config config?
   auth auth?]
  (let [{:keys [namespace]} config
        {:keys [keycloak-admin-user keycloak-admin-password]} auth]
    (->
     (yaml/load-as-edn "keycloak/secret.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace)
     (cm/replace-key-value :keycloak-user (b64/encode keycloak-admin-user))
     (cm/replace-key-value :keycloak-password (b64/encode keycloak-admin-password)))))

(defn-spec generate-service cp/map-or-seq? 
  [config config?]
  (let [{:keys [namespace]} config]
    (->
     (yaml/load-as-edn "keycloak/service.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace))))

(defn-spec generate-deployment cp/map-or-seq?
  [config config?]
  (let [{:keys [fqdn namespace]} config]
    (-> 
     (yaml/load-as-edn "keycloak/deployment.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace)
     (cm/replace-all-matching "FQDN" fqdn))))
  
