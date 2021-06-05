(ns dda.c4k-keycloak.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-keycloak.yaml :as yaml]
  [dda.c4k-keycloak.keycloak :as kc]
  [dda.c4k-keycloak.postgres :as pg]))

(def config? (s/keys :req-un [::kc/fqdn]
                     :opt-un [::kc/issuer]))

(def auth? (s/keys :req-un [::kc/keycloak-admin-user ::kc/keycloak-admin-password
                            ::pg/postgres-db-user ::pg/postgres-db-password]))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (cs/join "\n"
           [(yaml/to-string (pg/generate-config))
            "---"
            (yaml/to-string (pg/generate-secret my-auth))
            "---"
            (yaml/to-string (pg/generate-service))
            "---"
            (yaml/to-string (pg/generate-deployment))
            "---"
            (yaml/to-string (kc/generate-secret my-auth))
            "---"
            (yaml/to-string (kc/generate-certificate my-config))
            "---"
            (yaml/to-string (kc/generate-ingress my-config))
            "---"
            (yaml/to-string (kc/generate-service))
            "---"
            (yaml/to-string (kc/generate-deployment))]))
