(ns dda.k8s-keycloak.core-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.k8s-keycloak.core :as cut]))

(deftest should-generate-yaml
  (is (=  {:apiVersion "v1", :kind "ConfigMap"
           :metadata {:name "keycloak", 
                      :labels {:app.kubernetes.io/name "k8s-keycloak"}}, 
           :data {:config.edn "some-config-value\n", 
                  :credentials.edn "some-credentials-value\n"}}
          (cut/generate-config "some-config-value\n" "some-credentials-value\n"))))

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
           {:tls '({:hosts ["test.de"] :secretName "keycloak-secret"})
            :rules '({:host "test.de", :http {:paths '({:backend {:serviceName "keycloak", :servicePort 8080}})}})}}
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
           :rules '({:host "test.de", :http {:paths '({:backend {:serviceName "keycloak", :servicePort 8080}})}})}}
         (cut/generate-ingress {:fqdn "test.de"
                                :issuer :prod}))))