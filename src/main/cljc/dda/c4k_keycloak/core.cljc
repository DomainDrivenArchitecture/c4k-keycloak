(ns dda.c4k-keycloak.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.postgres :as postgres]
  [dda.c4k-keycloak.keycloak :as kc]))

(def default-storage-class :local-path)

(def config-defaults {:issuer :staging})

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer]))

(def auth? (s/keys :req-un [::kc/keycloak-admin-user ::kc/keycloak-admin-password]))

(defn-spec k8s-objects any?
  [config config?
   auth auth?]
  (into
   []
   (concat [(yaml/to-string (postgres/generate-config {:postgres-size :2gb :db-name "keycloak"}))
            (yaml/to-string (postgres/generate-secret auth))
            (yaml/to-string (postgres/generate-pvc {:pv-storage-size-gb 30
                                                    :pvc-storage-class-name default-storage-class}))
            (yaml/to-string (postgres/generate-deployment :postgres-image "postgres:14"))
            (yaml/to-string (postgres/generate-service))
            (yaml/to-string (kc/generate-secret auth))
            (yaml/to-string (kc/generate-ingress config))
            (yaml/to-string (kc/generate-service))
            (yaml/to-string (kc/generate-deployment))])))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (cs/join
   "\n---\n"
   (k8s-objects my-config my-auth)))