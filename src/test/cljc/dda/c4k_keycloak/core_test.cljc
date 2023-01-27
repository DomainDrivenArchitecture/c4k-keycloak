(ns dda.c4k-keycloak.core-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   #?(:cljs [shadow.resource :as rc])
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-keycloak.core :as cut]))

#?(:cljs
   (defmethod yaml/load-resource :keycloak-test [resource-name]
     (case resource-name
       "keycloak-test/valid-auth.yaml"   (rc/inline "keycloak-test/valid-auth.yaml")
       "keycloak-test/valid-config.yaml" (rc/inline "keycloak-test/valid-config.yaml")
       (throw (js/Error. "Undefined Resource!")))))

;; TODO: 2023.01.27 - jem: may not validate k3s-cluster-name entries ... find out what's wrong.
(deftest validate-valid-resources
  (is (s/valid? ::cut/config (yaml/load-as-edn "keycloak-test/valid-config.yaml")))
  (is (s/valid? cut/auth? (yaml/load-as-edn "keycloak-test/valid-auth.yaml"))))