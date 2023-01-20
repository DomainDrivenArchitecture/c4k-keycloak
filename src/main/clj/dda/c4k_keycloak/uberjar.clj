(ns dda.c4k-keycloak.uberjar
  (:gen-class)
  (:require
   [dda.c4k-common.uberjar :as uberjar]
   [dda.c4k-keycloak.core :as core]))

(defn -main [& cmd-args]
  (uberjar/main-common
   "c4k-keycloak"
   core/config?
   core/auth?
   core/config-defaults
   core/k8s-objects
   cmd-args))
