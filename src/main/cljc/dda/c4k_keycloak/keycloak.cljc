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

(def config? (s/keys :req-un [::fqdn
                              ::namespace]))

(def auth? (s/keys :req-un [::keycloak-admin-user 
                            ::keycloak-admin-password]))

#?(:cljs
   (defmethod yaml/load-resource :keycloak [resource-name]
     (get (inline-resources "keycloak") resource-name)))

(defn-spec generate-ratelimit-ingress seq?
  [config config?]
  (let [{:keys [fqdn max-rate max-concurrent-requests namespace]} config]
    (ing/generate-simple-ingress (merge
                                  {:service-name "forgejo-service"
                                   :service-port 3000
                                   :fqdns [fqdn]
                                   :average-rate max-rate
                                   :burst-rate max-concurrent-requests
                                   :namespace namespace}
                                  config))))

(defn-spec generate-secret cp/map-or-seq?
  [config config?
   auth auth?]
  (let [{:keys [namespace]} config
        {:keys [keycloak-admin-user keycloak-admin-password postgres-db-user postgres-db-password]} auth]
    (->
     (yaml/load-as-edn "keycloak/secret.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace)
     (cm/replace-all-matching "DBUSER" (b64/encode postgres-db-user))
     (cm/replace-all-matching "DBPW" (b64/encode postgres-db-password))
     (cm/replace-all-matching "ADMIN_USER" (b64/encode keycloak-admin-user))
     (cm/replace-all-matching "ADMIN_PASS" (b64/encode keycloak-admin-password)))))

(defn-spec generate-configmap cp/map-or-seq?
  [config config?]
  (let [{:keys [namespace fqdn]} config]
    (->
     (yaml/load-as-edn "keycloak/configmap.yaml")
     (cm/replace-all-matching "NAMESPACE" namespace)
     (cm/replace-all-matching "FQDN" fqdn)
     (cm/replace-all-matching "ADMIN_FQDN" (str "control." fqdn))))) ; TODO Document this

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
     (cm/replace-all-matching "NAMESPACE" namespace))))
  
