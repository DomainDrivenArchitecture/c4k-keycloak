(defproject org.domaindrivenarchitecture/k8s-keycloak "0.1.0-SNAPSHOT"
  :description "keycloak k8s-installation package"
  :url "https://domaindrivenarchitecture.org"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.reader "1.3.4"]
                 [aero "1.1.6"]
                 [orchestra "2021.01.01-1"]
                 [expound "0.8.9"]
                 [clj-commons/clj-yaml "0.7.106"]]
  :target-path "target/%s/"
  :source-paths ["src/main/cljc"
                 "src/main/clj"]
  :resource-paths ["src/main/resources"]
  :repositories [["snapshots" :clojars]
                 ["releases" :clojars]]
  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]]
  :profiles {:test {:test-paths ["src/test/cljc"]
                    :resource-paths ["src/test/resources"]
                    :dependencies [[dda/data-test "0.1.1"]]}
             :dev {:plugins [[lein-shell "0.5.0"]]}
             :uberjar {:aot :all
                       :main dda.k8s-keycloak.uberjar
                       :uberjar-name "k8s-keycloak-standalone.jar"
                       :dependencies [[org.clojure/tools.cli "1.0.206"]
                                      [ch.qos.logback/logback-classic "1.3.0-alpha4"
                                       :exclusions [com.sun.mail/javax.mail]]
                                      [org.slf4j/jcl-over-slf4j "2.0.0-alpha1"]]}}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]]
  :aliases {"native" ["shell"
                      "native-image"
                      "--report-unsupported-elements-at-runtime"
                      "--initialize-at-build-time"
                      "-jar" "target/uberjar/k8s-keycloak-standalone.jar"
                      "-H:ResourceConfigurationFiles=graalvm-resource-config.json"
                      "-H:Log=registerResource"
                      "-H:Name=target/graalvm/${:name}"]})
