(ns dda.k8s-keycloak.base64
  (:import (java.util Base64)))

(defn encode
  [string]
  (.encodeToString 
   (Base64/getEncoder) 
   (.getBytes string))) 

(defn decode
  [string]
  (String. 
   (.decode (Base64/getDecoder) string) 
   "UTF-8"))