(ns dda.k8s-keycloak.core
  (:require
   [clojure.string :as cs]
   [clojure.spec.alpha :as s]
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.k8s-keycloak.yaml :as yaml]))

(defn bash-env-string?
  [input]
  (and (string? input)
       (not (re-matches #".*['\"\$]+.*" input))))

(defn fqdn-string?
  [input]
  (and (string? input)
       (not (nil? (re-matches #"(?=^.{4,253}\.?$)(^((?!-)[a-zA-Z0-9-]{1,63}(?<!-)\.)+[a-zA-Z]{2,63}\.?$)" input)))))

(s/def ::user-name bash-env-string?)
(s/def ::user-password string?)
(s/def ::fqdn fqdn-string?)
(s/def ::issuer #{:prod :staging})

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer]))

(def auth? (s/keys :req-un [::user-name ::user-password]))

(defn generate-config [my-config my-auth]
  (->
   (yaml/from-string (yaml/load-resource "config.yaml"))
   (assoc-in [:data :config.edn] (str my-config))
   (assoc-in [ :data :credentials.edn] (str my-auth))))

(defn generate-deployment [config]
  (let [user (:user config)
        password (:password config)]
    (->
     (yaml/from-string (yaml/load-resource "deployment.yaml"))
     (assoc-in [:spec :template :spec :containers]
               [{:name "keycloak"
                 :image "quay.io/keycloak/keycloak:13.0.0"
                 :env
                 [{:name "KEYCLOAK_USER", :value user}
                  {:name "KEYCLOAK_PASSWORD", :value password}
                  {:name "PROXY_ADDRESS_FORWARDING", :value "true"}]
                 :ports [{:name "http", :containerPort 8080}]
                 :readinessProbe {:httpGet {:path "/auth/realms/master", :port 8080}}}]))))

(defn generate-certificate [config]
  (let [{:keys [fqdn issuer]
         :or {issuer :staging}} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
  (->
   (yaml/from-string (yaml/load-resource "certificate.yaml"))
   (assoc-in [:spec :commonName] fqdn)
   (assoc-in [:spec :dnsNames] [fqdn])
   (assoc-in [:spec :issuerRef :name] letsencrypt-issuer))))

(defn generate-ingress [config]
  (let [{:keys [fqdn issuer]
         :or {issuer :staging}} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "ingress.yaml"))
     (assoc-in [:metadata :annotations :cert-manager.io/cluster-issuer] letsencrypt-issuer)
     (assoc-in [:spec :tls] [{:hosts [fqdn], :secretName "keycloak-secret"}])
     (assoc-in [:spec :rules] [{:host fqdn
                                :http {:paths [{:backend {:serviceName "keycloak"
                                                          :servicePort 8080}}]}}]))))

(defn generate-service []
  (yaml/from-string (yaml/load-resource "service.yaml")))

(defn-spec generate any?
  [my-config config?
   my-auth auth?] 
  (cs/join "\n" 
           [(yaml/to-string (generate-config my-config my-auth))
            "---"
            (yaml/to-string (generate-certificate))
            "---"
            (yaml/to-string (generate-ingress))
            "---"
            (yaml/to-string (generate-service))
            "---"
            (yaml/to-string (generate-deployment))]))
