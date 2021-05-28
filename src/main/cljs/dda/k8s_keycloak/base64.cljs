(ns dda.k8s-keycloak.base64)

(defn encode
  [string]
  (.btoa js/window string))

(defn decode
  [string]
  (.atob js/window string))
