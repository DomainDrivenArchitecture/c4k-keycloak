(ns dda.c4k-keycloak.browser
  (:require
   [clojure.spec.alpha :as s]
   [clojure.tools.reader.edn :as edn]
   [expound.alpha :as expound]
   [dda.c4k-keycloak.core :as core]))

(defn print-debug [sth]
  (print "debug " sth)
  sth)

(defn fqdn []
    (-> js/document
      (.getElementById "fqdn")))

(defn issuer []
  (-> js/document
      (.getElementById "issuer")))

(defn issuer-from-document []
  (let [issuer-str (-> (issuer)
                       (.-value))]
    (if (= issuer-str "")
      :staging
      (keyword issuer-str))))

(defn auth []
  (-> js/document
      (.getElementById "auth")))

(defn form []
  (-> js/document
      (.getElementById "form")))

(defn config-from-document []
   {:fqdn (-> (fqdn)
              (.-value))
    :issuer (issuer-from-document)})

(defn auth-from-document []
  (-> (auth)
      (.-value)))

(defn set-output!
  [input]
  (-> js/document
      (.getElementById "output")
      (.-value)
      (set! input)))

(defn set-config-validation-result!
  [validation-result]
  (-> js/document
      (.getElementById "config-validation")
      (.-innerHTML)
      (set! validation-result))
  (-> (fqdn)
      (.setCustomValidity validation-result))
  (-> (issuer)
      (.setCustomValidity validation-result))
  validation-result)

(defn validate-config! []
  (let [config-map (config-from-document)]
    (if (s/valid? core/config? config-map)
      (set-config-validation-result! "")
      (set-config-validation-result!
       (expound/expound-str core/config? config-map {:print-specs? false})))))

(defn set-validated! []
  (-> (form)
      (.-classList)
      (.add "was-validated")))

(defn set-auth-validation-result!
  [validation-result]
  (-> js/document
       (.getElementById "auth-validation")
       (.-innerHTML)
       (set! validation-result))
  (-> (auth)
      (.setCustomValidity validation-result))
  validation-result)

(defn validate-auth! []
  (let [auth-str (auth-from-document)
        auth-map (edn/read-string auth-str)]
    (if (s/valid? core/auth? auth-map)
      (set-auth-validation-result! "")
      (set-auth-validation-result!
       (expound/expound-str core/auth? auth-map {:print-specs? false})))))

(defn init []
  (-> js/document
      (.getElementById "generate-button")
      (.addEventListener "click"
                         #(do (validate-config!)
                              (validate-auth!)
                              (set-validated!)
                              (-> (core/generate (config-from-document) (auth-from-document))
                                  (set-output!)))))
  (-> (fqdn)
      (.addEventListener "blur"
                         #(do (validate-config!)
                              (validate-auth!)
                              (set-validated!))))
  (-> (auth)
      (.addEventListener "blur"
                         #(do (validate-config!)
                              (validate-auth!)
                              (set-validated!))))
  (-> (issuer)
      (.addEventListener "blur"
                         #(do (validate-config!)
                              (validate-auth!)
                              (set-validated!)))))