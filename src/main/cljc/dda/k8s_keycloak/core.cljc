(ns dda.k8s-keycloak.core
  (:require
   [clojure.string :as cs]
   [clojure.spec.alpha :as s]
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.k8s-keycloak.yaml :as yaml]
   [clojure.walk]))

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

(defn replace-all-matching-values-by-new-value
  [coll value-to-match value-to-replace]
  (clojure.walk/postwalk #(if (and (= (type value-to-match) (type %)) 
                                   (= value-to-match %))
                            value-to-replace
                            %) coll))

(defn generate-config [my-config my-auth]
  (->
   (yaml/from-string (yaml/load-resource "config.yaml"))
   (assoc-in [:data :config.edn] (str my-config))
   (assoc-in [:data :credentials.edn] (str my-auth))))

(defn generate-postgres-config []
   (yaml/from-string (yaml/load-resource "postgres/postgres-config.yaml")))

(defn generate-deployment [my-auth]
  (let [{:keys [user-name user-password]} my-auth]
    (->
     (yaml/from-string (yaml/load-resource "deployment.yaml"))
     (assoc-in [:spec :template :spec :containers 0 :env 0 :value] user-name)
     (assoc-in [:spec :template :spec :containers 0 :env 1 :value] user-password))))

(defn generate-postgres-deployment [my-auth]
  (let [{:keys [postgres-user postgres-password postgres-db]} my-auth]
    (->
     (yaml/from-string (yaml/load-resource "postgres/postgres-deployment.yaml"))
     (assoc-in [:spec :template :spec :containers 0 :env 0 :value] postgres-user)
     (assoc-in [:spec :template :spec :containers 0 :env 1 :value] postgres-db)
     (assoc-in [:spec :template :spec :containers 0 :env 2 :value] postgres-password))))

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
     (replace-all-matching-values-by-new-value "fqdn" fqdn))))

(defn generate-service []
  (yaml/from-string (yaml/load-resource "service.yaml")))

(defn generate-postgres-service []
  (yaml/from-string (yaml/load-resource "postgres/postgres-service.yaml")))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (cs/join "\n"
           [(yaml/to-string (generate-postgres-config))
            "---"
            (yaml/to-string (generate-postgres-service))
            "---"
            (yaml/to-string (generate-postgres-deployment my-auth))
            "---"
            (yaml/to-string (generate-config my-config my-auth))
            "---"
            (yaml/to-string (generate-certificate my-config))
            "---"
            (yaml/to-string (generate-ingress my-config))
            "---"
            (yaml/to-string (generate-service))
            "---"
            (yaml/to-string (generate-deployment my-auth))]))