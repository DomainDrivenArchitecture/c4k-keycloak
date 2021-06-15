(ns dda.c4k-keycloak.browser
  (:require
   [clojure.string :as st]
   [clojure.spec.alpha :as s]
   [clojure.tools.reader.edn :as edn]
   [expound.alpha :as expound]
   [dda.c4k-keycloak.core :as core]
   [dda.c4k-keycloak.keycloak :as kc]))

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
    (when-not (st/blank? issuer-str)
      (keyword issuer-str))))

(defn fqdn-from-document []
  (-> (fqdn)
      (.-value)))

(defn auth []
  (-> js/document
      (.getElementById "auth")))

(defn form []
  (-> js/document
      (.getElementById "form")))

(defn config-from-document []
  (merge
   {:fqdn (fqdn-from-document)}
   (when-not (st/blank? (issuer-from-document))
     {:issuer (issuer-from-document)})))

  (defn auth-from-document []
    (edn/read-string (-> (auth)
                         (.-value))))

(defn set-output!
  [input]
  (-> js/document
      (.getElementById "output")
      (.-value)
      (set! input)))

(defn set-fqdn-validation-result!
  [validation-result]
  (-> js/document
      (.getElementById "fqdn-validation")
      (.-innerHTML)
      (set! validation-result))
  (-> (fqdn)
      (.setCustomValidity validation-result))
  validation-result)

(defn set-issuer-validation-result!
  [validation-result]
  (-> js/document
      (.getElementById "issuer-validation")
      (.-innerHTML)
      (set! validation-result))
  (-> (issuer)
      (.setCustomValidity validation-result))
  validation-result)

(defn validate-fqdn! []
  (let [fqdn (fqdn-from-document)]
    (if (s/valid? ::kc/fqdn fqdn)
      (set-fqdn-validation-result! "")
      (set-fqdn-validation-result!
       (expound/expound-str ::kc/fqdn fqdn {:print-specs? false})))))

(defn validate-issuer! []
  (let [issuer (issuer-from-document)]
    (print-debug (js->clj issuer))
    (print-debug (st/blank? issuer))
    (if (or (st/blank? issuer) (s/valid? ::kc/issuer issuer))
      (set-issuer-validation-result! "")
      (set-issuer-validation-result!
       (expound/expound-str ::kc/issuer issuer {:print-specs? false})))))

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
  (let [auth-map (auth-from-document)]
    (if (s/valid? core/auth? auth-map)
      (set-auth-validation-result! "")
      (set-auth-validation-result!
       (expound/expound-str core/auth? auth-map {:print-specs? false})))))

(defn validate-all! []
  (validate-fqdn!)
  (validate-issuer!)
  (validate-auth!)
  (set-validated!))


(defn init []
  (-> js/document
      (.getElementById "generate-button")
      (.addEventListener "click"
                         #(do (validate-all!)
                              (-> (core/generate (config-from-document) (auth-from-document))
                                  (set-output!)))))
  (-> (fqdn)
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (issuer)
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (auth)
      (.addEventListener "blur"
                         #(do (validate-all!))))
  )