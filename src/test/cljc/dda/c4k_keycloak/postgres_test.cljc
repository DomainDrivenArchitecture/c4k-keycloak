(ns dda.c4k-keycloak.postgres-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.c4k-keycloak.postgres :as cut]))

(deftest should-generate-secret
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "postgres-secret"}
          :type "Opaque"
          :data
          {:postgres-user "cHNxbHVzZXI="
           :postgres-password "dGVzdDEyMzQ="}}
         (cut/generate-secret {:postgres-db-user "psqluser" :postgres-db-password "test1234"}))))

(deftest should-generate-postgres-deployment
  (is (= {:apiVersion "apps/v1"
          :kind "Deployment"
          :metadata {:name "postgresql"}
          :spec
          {:selector {:matchLabels {:app "postgresql"}}
           :strategy {:type "Recreate"}
           :template
           {:metadata {:labels {:app "postgresql"}}
            :spec
            {:containers
             [{:image "postgres"
               :name "postgresql"
               :env
               [{:name "POSTGRES_USER"
                 :valueFrom
                 {:secretKeyRef
                  {:name "postgres-secret", :key "postgres-user"}}}
                {:valueFrom
                 {:secretKeyRef
                  {:name "postgres-secret"
                   :key "postgres-password"}}
                 :name "POSTGRES_PASSWORD"}
                {:valueFrom
                 {:configMapKeyRef
                  {:name "postgres-config", :key "postgres-db"}}
                 :name "POSTGRES_DB"}]
               :ports [{:containerPort 5432, :name "postgresql"}]
               :volumeMounts
               [{:name "postgres-config-volume"
                 :mountPath "/etc/postgresql/postgresql.conf"
                 :subPath "postgresql.conf"
                 :readOnly true}]}]
             :volumes [{:name "postgres-config-volume", :configMap {:name "postgres-config"}}]}}}}
    (cut/generate-deployment))))
