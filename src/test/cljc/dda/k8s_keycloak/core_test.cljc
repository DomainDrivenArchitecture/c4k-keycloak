(ns dda.k8s-keycloak.core-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.k8s-keycloak.core :as cut]
   [dda.k8s-keycloak.yaml :as yaml]))

(deftest should-generate-yaml
  (is (=  {:apiVersion "v1", :kind "ConfigMap"
           :metadata {:name "keycloak", 
                      :labels {:app.kubernetes.io/name "k8s-keycloak"}}, 
           :data {:config.edn "some-config-value\n", 
                  :credentials.edn "some-credentials-value\n"}}
          (cut/generate-config "some-config-value\n" "some-credentials-value\n"))))

(deftest should-generate-certificate
  (is (= {:apiVersion "cert-manager.io/v1alpha2"
          :kind "Certificate"
          :metadata {:name "keycloak-cert", :namespace "default"}
          :spec
          {:secretName "keycloak-secret"
           :commonName "test.de"
           :dnsNames ["test.de"]
           :issuerRef {:name "letsencrypt-prod-issuer", :kind "ClusterIssuer"}}}
         (cut/generate-certificate {:fqdn "test.de" :issuer :prod} ))))

(deftest should-generate-ingress-yaml-with-default-issuer
  (is (= {:apiVersion "networking.k8s.io/v1beta1"
           :kind "Ingress"
           :metadata
           {:name "ingress-cloud"
            :annotations
            {:cert-manager.io/cluster-issuer "letsencrypt-staging-issuer"
             :nginx.ingress.kubernetes.io/proxy-body-size "256m"
             :nginx.ingress.kubernetes.io/ssl-redirect "true"
             :nginx.ingress.kubernetes.io/rewrite-target "/"
             :nginx.ingress.kubernetes.io/proxy-connect-timeout "300"
             :nginx.ingress.kubernetes.io/proxy-send-timeout "300"
             :nginx.ingress.kubernetes.io/proxy-read-timeout "300"}
            :namespace "default"}
           :spec
           {:tls [{:hosts ["test.de"] :secretName "keycloak-secret"}]
            :rules [{:host "test.de", :http {:paths [{:backend {:serviceName "keycloak", :servicePort 8080}}]}}]}}
          (cut/generate-ingress {:fqdn "test.de"}))))

(deftest should-generate-ingress-yaml-with-prod-issuer
  (is (= {:apiVersion "networking.k8s.io/v1beta1"
          :kind "Ingress"
          :metadata
          {:name "ingress-cloud"
           :annotations
           {:cert-manager.io/cluster-issuer "letsencrypt-prod-issuer"
            :nginx.ingress.kubernetes.io/proxy-body-size "256m"
            :nginx.ingress.kubernetes.io/ssl-redirect "true"
            :nginx.ingress.kubernetes.io/rewrite-target "/"
            :nginx.ingress.kubernetes.io/proxy-connect-timeout "300"
            :nginx.ingress.kubernetes.io/proxy-send-timeout "300"
            :nginx.ingress.kubernetes.io/proxy-read-timeout "300"}
           :namespace "default"}
          :spec
          {:tls [{:hosts ["test.de"], :secretName "keycloak-secret"}]
           :rules '({:host "test.de", :http {:paths [{:backend {:serviceName "keycloak", :servicePort 8080}}]}})}}
         (cut/generate-ingress {:fqdn "test.de"
                                :issuer :prod}))))

(deftest should-generate-deployment
  (is (= {:apiVersion "apps/v1"
          :kind "Deployment"
          :metadata {:name "keycloak", :namespace "default", :labels {:app "keycloak"}}
          :spec
          {:replicas 1
           :selector {:matchLabels {:app "keycloak"}}
           :template
           {:metadata {:labels {:app "keycloak"}}
            :spec
            {:containers
             [{:name "keycloak"
               :image "quay.io/keycloak/keycloak:13.0.0"
               :env
               [{:name "KEYCLOAK_USER", :value "testuser"}
                {:name "KEYCLOAK_PASSWORD", :value "test1234"}
                {:name "PROXY_ADDRESS_FORWARDING", :value "true"}]
               :ports [{:name "http", :containerPort 8080}]
               :readinessProbe {:httpGet {:path "/auth/realms/master", :port 8080}}}]}}}}
         (cut/generate-deployment {:user-name "testuser" :user-password "test1234"}))))
<<<<<<< HEAD
=======

(deftest test-vector-replace-fqdn-function
  (let [ingress-yaml (yaml/from-string (yaml/load-resource "ingress.yaml"))
        fqdn "some_host"
        desired-result (-> ingress-yaml
                           (assoc-in [:spec :rules] [{:host fqdn
                                                      :http {:paths [{:backend {:serviceName "keycloak"
                                                                                :servicePort 8080}}]}}
                                                     {:host fqdn
                                                      :http {:paths [{:backend {:serviceName "another_keycloak"
                                                                                :servicePort 8081}}]}}
                                                     ]))]
    (is (= desired-result 
           (cut/assoc-in-nested (cut/cast-lazy-seq-to-vec ingress-yaml) [:spec :rules :host] fqdn))))
    )


>>>>>>> 0379ad882851993440561c5c09c9f813e4357ab1
