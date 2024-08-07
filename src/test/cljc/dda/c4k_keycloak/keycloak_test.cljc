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
          {:keycloak-user "dXNlcg=="
           :keycloak-password "cGFzc3dvcmQ="}}
         (cut/generate-secret {:namespace "keycloak" :fqdn "test.de"} {:keycloak-admin-user "user" :keycloak-admin-password "password"}))))

(deftest should-generate-deployment
  (is (= {:apiVersion "apps/v1",
          :kind "Deployment",
          :metadata
          {:name "keycloak", :namespace "keycloak", :labels {:app "keycloak"}},
          :spec
          {:replicas 1,
           :selector {:matchLabels {:app "keycloak"}},
           :template
           {:metadata {:labels {:app "keycloak"}},
            :spec
            {:containers
             [{:name "keycloak",
               :image "quay.io/keycloak/keycloak:20.0.3",
               :imagePullPolicy "IfNotPresent",
               :args ["start"],
               :volumeMounts
               [{:name "keycloak-cert",
                 :mountPath "/etc/certs",
                 :readOnly true}],
               :env
               [{:name "KC_HTTPS_CERTIFICATE_FILE",
                 :value "/etc/certs/tls.crt"}
                {:name "KC_HTTPS_CERTIFICATE_KEY_FILE",
                 :value "/etc/certs/tls.key"}
                {:name "KC_HOSTNAME", :value "test.de"}
                {:name "KC_PROXY", :value "edge"}
                {:name "DB_VENDOR", :value "POSTGRES"}
                {:name "DB_ADDR", :value "postgresql-service"}
                {:name "DB_SCHEMA", :value "public"}
                {:name "DB_DATABASE",
                 :valueFrom
                 {:configMapKeyRef
                  {:name "postgres-config", :key "postgres-db"}}}
                {:name "DB_USER",
                 :valueFrom
                 {:secretKeyRef
                  {:name "postgres-secret", :key "postgres-user"}}}
                {:name "DB_PASSWORD",
                 :valueFrom
                 {:secretKeyRef
                  {:name "postgres-secret", :key "postgres-password"}}}
                {:name "KEYCLOAK_ADMIN",
                 :valueFrom
                 {:secretKeyRef
                  {:name "keycloak-secret", :key "keycloak-user"}}}
                {:name "KEYCLOAK_ADMIN_PASSWORD",
                 :valueFrom
                 {:secretKeyRef
                  {:name "keycloak-secret", :key "keycloak-password"}}}],
               :ports [{:name "http", :containerPort 8080}]}],
             :volumes
             [{:name "keycloak-cert",
               :secret
               {:secretName "keycloak",
                :items
                [{:key "tls.crt", :path "tls.crt"}
                 {:key "tls.key", :path "tls.key"}]}}]}}}}
         (cut/generate-deployment {:fqdn "test.de" :namespace "keycloak"}))))
