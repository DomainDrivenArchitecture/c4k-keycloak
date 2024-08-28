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

(deftest should-generate-configmap
  (is (= {:apiVersion "v1",
          :kind "ConfigMap",
          :metadata {:name "keycloak-env", :namespace "keycloak"},
          :data
          {:KC_HTTPS_CERTIFICATE_FILE "/etc/certs/tls.crt",
           :KC_HTTPS_CERTIFICATE_KEY_FILE "/etc/certs/tls.key",
           :KC_HOSTNAME "test.de" ,
           :KC_HOSTNAME_ADMIN "control.test.de",
           :KC_PROXY "edge",
           :KC_DB "postgres",
           :KC_DB_URL_HOST "postgresql-service",
           :KC_DB_URL_PORT 5432}}
         (cut/generate-configmap {:namespace "keycloak" :fqdn "test.de"}))))

(deftest should-generate-deployment
  (is (= {:name "keycloak", :namespace "keycloak", :labels {:app "keycloak"}}
         (:metadata (cut/generate-deployment {:fqdn "example.com" :namespace "keycloak"})))))
