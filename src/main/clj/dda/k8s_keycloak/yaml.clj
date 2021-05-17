(ns dda.k8s-keycloak.yaml
  (:require
   [clojure.java.io :as io]
   [clj-yaml.core :as yaml]))

(defn load-resource [resource-name]
  (slurp (io/resource  resource-name)))

(defn from-string [input]
  (yaml/parse-string input))

(defn to-string [edn]
  (yaml/generate-string edn :dumper-options {:flow-style :block}))