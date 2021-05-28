(ns dda.k8s-keycloak.common
  (:require
   [clojure.walk]))

(defn bash-env-string?
  [input]
  (and (string? input)
       (not (re-matches #".*['\"\$]+.*" input))))

(defn fqdn-string?
  [input]
  (and (string? input)
       (not (nil? (re-matches #"(?=^.{4,253}\.?$)(^((?!-)[a-zA-Z0-9-]{1,63}(?<!-)\.)+[a-zA-Z]{2,63}\.?$)" input)))))

(defn letsencrypt-issuer? 
  [input]
  (contains? #{:prod :staging} input))

(defn replace-named-value
  [coll name value]
  (clojure.walk/postwalk #(if (and (map? %)
                                   (= name (:name %)))
                            {:name name :value value}
                            %) coll))

(defn replace-all-matching-values-by-new-value
  [coll value-to-match value-to-replace]
  (clojure.walk/postwalk #(if (and (= (type value-to-match) (type %))
                                   (= value-to-match %))
                            value-to-replace
                            %) coll))
