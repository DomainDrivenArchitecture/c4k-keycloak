(ns dda.c4k-keycloak.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.browser :as br]
   [dda.c4k-keycloak.core :as core]
   [dda.c4k-keycloak.keycloak :as kc]))

(defn generate-content []
  (cm/concat-vec
   [(assoc 
     (br/generate-needs-validation) :content
     (cm/concat-vec
      (br/generate-group
       "domain"
       (cm/concat-vec
        (br/generate-input-field "fqdn" "Your fqdn:" "keycloak.prod.meissa.de")
        (br/generate-input-field "issuer" "(Optional) Your issuer prod/staging:" "")))
      (br/generate-group
       "credentials"
       (br/generate-text-area "auth" "Your auth.edn:" ":keycloak-admin-user \"keycloak\"
                                                       :keycloak-admin-password \"adminpassword\""
                              "5"))
      [(br/generate-br)]
      (br/generate-button "generate-button" "Generate c4k yaml")))]
   (br/generate-output "c4k-keycloak-output" "Your c4k deployment.yaml:" "25")))

(defn generate-content-div
  []
  {:type :element
   :tag :div
   :content
   (generate-content)})

(defn config-from-document []
  (let [issuer (br/get-content-from-element "issuer" :optional true)]
    (merge
     {:fqdn (br/get-content-from-element "fqdn")}
     (when (some? issuer)
       {:issuer issuer}))))

(defn validate-all! []
  (br/validate! "fqdn" ::kc/fqdn)
  (br/validate! "issuer" ::kc/issuer :optional true)
  (br/validate! "auth" core/auth? :deserializer edn/read-string)
  (br/set-validated!))

(defn add-validate-listener [name]
  (-> (br/get-element-by-id name)
      (.addEventListener "blur" #(do (validate-all!)))))

(defn init []
  (br/append-hickory (generate-content-div))
  (-> js/document
      (.getElementById "generate-button")
      (.addEventListener "click"
                         #(do (validate-all!)
                              (-> (cm/generate-common
                                   (config-from-document) 
                                   (br/get-content-from-element "auth" :deserializer edn/read-string)
                                   {}
                                   core/k8s-objects)
                                  (br/set-output!)))))
  (add-validate-listener "fqdn")
  (add-validate-listener "issuer")
  (add-validate-listener "auth"))