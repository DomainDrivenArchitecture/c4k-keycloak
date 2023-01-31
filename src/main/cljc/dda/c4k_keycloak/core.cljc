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
  [dda.c4k-keycloak.keycloak :as kc]))

(def default-storage-class :local-path)

(def config-defaults {:issuer "staging"})

(def config? (s/keys :req-un [::kc/fqdn]
                     :opt-un [::kc/issuer
                              ::mon/mon-cfg]))

(def auth? (s/keys :req-un [::kc/keycloak-admin-user ::kc/keycloak-admin-password
                            ::postgres/postgres-db-user ::postgres/postgres-db-password]
                   :opt-un [::mon/mon-auth]))

(defn-spec k8s-objects cp/map-or-seq?
  [config config?
   auth auth?]
  (map yaml/to-string
       (filter
        #(not (nil? %))
        (cm/concat-vec
         [(postgres/generate-config {:postgres-size :2gb :db-name "keycloak"})
          (postgres/generate-secret auth)
          (postgres/generate-pvc {:pv-storage-size-gb 30
                                  :pvc-storage-class-name default-storage-class})
          (postgres/generate-deployment :postgres-image "postgres:14")
          (postgres/generate-service)
          (kc/generate-secret auth)
          (kc/generate-service)
          (kc/generate-deployment config)]
         (kc/generate-ingress config)
         (when (:contains? config :mon-cfg)
           (mon/generate (:mon-cfg config) (:mon-auth auth)))))))
