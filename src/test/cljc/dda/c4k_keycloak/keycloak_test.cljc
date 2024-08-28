(ns dda.c4k-keycloak.keycloak-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.test.alpha :as st]
   [dda.c4k-keycloak.keycloak :as cut]))

(st/instrument)

(deftest should-generate-secret
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "keycloak-secret", :namespace "keycloak"}
          :type "Opaque"
          :data
          {:KEYCLOAK_ADMIN "dXNlcg=="
           :KEYCLOAK_ADMIN_PASSWORD "cGFzc3dvcmQ="
           :DB_USER "a2V5Y2xvYWs="
           :DB_PASSWORD "ZGItcGFzc3dvcmQ="}}
         (cut/generate-secret {:namespace "keycloak" :fqdn "test.de"} 
                              {:keycloak-admin-user "user" :keycloak-admin-password "password"
                               :postgres-db-user "keycloak"
                               :postgres-db-password "db-password"}))))

(deftest should-generate-deployment
  (is (= {:name "keycloak", :namespace "keycloak", :labels {:app "keycloak"}}
         (:metadata (cut/generate-deployment {:fqdn "example.com" :namespace "keycloak"})))))
