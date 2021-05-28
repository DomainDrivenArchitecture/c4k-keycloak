(ns dda.k8s-keycloak.postgres-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.k8s-keycloak.postgres :as cut]))

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
               [{:name "POSTGRES_USER", :value "psqluser"}
                {:name "POSTGRES_DB", :valueFrom
                  {:configMapKeyRef
                   {:name "postgres-config", :key "postgres-db"}}}
                {:name "POSTGRES_PASSWORD", :value "test1234"}]
               :ports [{:containerPort 5432, :name "postgresql"}]
               :cmd nil
               :volumeMounts
               [{:name "postgres-config-volume"
                 :mountPath "/etc/postgresql/postgresql.conf"
                 :subPath "postgresql.conf"
                 :readOnly true}]}]
             :volumes [{:name "postgres-config-volume", :configMap {:name "postgres-config"}}]}}}}
    (cut/generate-deployment {:postgres-db-user "psqluser" :postgres-db-password "test1234"}))))
