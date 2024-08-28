(ns dda.c4k-keycloak.core
 (:require
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.predicate :as cp]
  [dda.c4k-common.monitoring :as mon]
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.postgres :as postgres]
  [dda.c4k-keycloak.keycloak :as kc]
  [dda.c4k-common.namespace :as ns]))

(def default-storage-class :local-path)

(def config-defaults {:issuer "staging",
                      :namespace "keycloak"
                      :postgres-image "postgres:14"
                      :postgres-size :2gb
                      :db-name "keycloak"
                      :pv-storage-size-gb 30
                      :pvc-storage-class-name default-storage-class})

(def config? (s/keys :req-un [::kc/fqdn]
                     :opt-un [::kc/issuer
                              ::mon/mon-cfg
                              ::kc/namespace]))

(def auth? (s/keys :req-un [::kc/keycloak-admin-user ::kc/keycloak-admin-password
                            ::postgres/postgres-db-user ::postgres/postgres-db-password]
                   :opt-un [::mon/mon-auth]))

(defn-spec config-objects cp/map-or-seq?
  [config config?]
  (map yaml/to-string
       (filter
        #(not (nil? %))
        (cm/concat-vec
         (ns/generate config)
         (postgres/generate-config config)
         [(kc/generate-configmap config)
          (kc/generate-service config)
          (kc/generate-deployment config)]
         (kc/generate-ratelimit-ingress config)
         (when (contains? config :mon-cfg)
           (mon/generate-config))))))

(defn-spec auth-objects cp/map-or-seq?
  [config config?
   auth auth?]
  (map yaml/to-string
       (filter
        #(not (nil? %))
        (cm/concat-vec
         (postgres/generate-auth config auth)
         [(kc/generate-secret config auth)]
         (when (and (contains? auth :mon-auth) (contains? config :mon-cfg))
           (mon/generate-auth (:mon-cfg config) (:mon-auth auth)))))))
