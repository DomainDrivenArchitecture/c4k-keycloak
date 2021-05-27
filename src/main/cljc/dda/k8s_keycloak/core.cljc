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

(defn cast-lazy-seq-to-vec
  [lazy-seq]
  (clojure.walk/postwalk #(if (instance? clojure.lang.LazySeq %)
                            (do (println %) (into [] %))
                            %) lazy-seq))

(defn replace-all-matching-values-by-new-value
  [value-to-match value-to-replace coll]
  (clojure.walk/postwalk #(if (and (= (type value-to-match) (type %)) 
                                   (= value-to-match %))
                            value-to-replace
                            %) coll))

(declare assoc-in-nested)
(declare assoc-in-nested-seq)
(declare assoc-in-nested-map)

(defn assoc-in-nested-seq [s path n]
  (map #(if (sequential? %)
          (assoc-in-nested-seq % path n)
          (assoc-in-nested-map % path n)) s))

(defn assoc-in-nested-map [m path n]
  (into (empty m)
        (let [p1 (first path)]
          (for [[k v] m]
            (if (= k p1)
              [k (assoc-in-nested v (rest path) n)]
              [k (assoc-in-nested v path n)])))))

(defn assoc-in-nested [data path n]
  (if (empty? path)
    n
    (if (sequential? data)
      (assoc-in-nested-seq data path n)
      (if (map? data)
        (assoc-in-nested-map data path n)
        data))))

(defn generate-config [my-config my-auth]
  (->
   (yaml/from-string (yaml/load-resource "config.yaml"))
   (assoc-in [:data :config.edn] (str my-config))
   (assoc-in [:data :credentials.edn] (str my-auth))))

(defn generate-deployment [my-auth]
  (let [{:keys [user-name user-password]} my-auth]
    (->
     (yaml/from-string (yaml/load-resource "deployment.yaml"))
     (assoc-in [:spec :template :spec :containers]
               [{:name "keycloak"
                 :image "quay.io/keycloak/keycloak:13.0.0"
                 :env
                 [{:name "KEYCLOAK_USER", :value user-name}
                  {:name "KEYCLOAK_PASSWORD", :value user-password}
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
            (yaml/to-string (generate-certificate my-config))
            "---"
            (yaml/to-string (generate-ingress my-config))
            "---"
            (yaml/to-string (generate-service))
            "---"
            (yaml/to-string (generate-deployment my-auth))]))


