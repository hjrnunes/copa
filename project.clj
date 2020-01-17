(defproject copa "0.2-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [
                 [buddy "2.0.0"]

                 [clojure.java-time "0.3.2"]
                 [clj-time "0.15.0"]

                 [cprop "0.1.14"]
                 [funcool/struct "1.4.0"]

                 [ch.qos.logback/logback-classic "1.2.3"]

                 [org.xerial/sqlite-jdbc "3.25.2"]
                 [conman "0.8.3"]

                 [luminus-immutant "0.2.5"]
                 [luminus-migrations "0.6.5"]
                 [luminus-transit "0.1.1"]
                 [luminus/ring-ttl-session "0.3.3"]

                 [cheshire "5.8.1"]
                 [metosin/muuntaja "0.6.4"]
                 [com.cognitect/transit-clj "0.8.313"]

                 [markdown-clj "1.10.0"]
                 [selmer "1.12.12"]
                 [nrepl "0.6.0"]

                 [mount "0.1.16"]

                 [metosin/reitit "0.3.9"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [metosin/ring-http-response "0.9.1"]

                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"]
                 [org.clojure/core.async "0.7.559"]
                 [org.clojure/google-closure-library "0.0-20190213-2033d5d9" :scope "provided"]
                 [org.clojure/tools.cli "0.4.2"]
                 [org.clojure/tools.logging "0.4.1"]

                 [com.google.javascript/closure-compiler-unshaded "v20190618" :scope "provided"]
                 [thheller/shadow-cljs "2.8.39" :scope "provided"]

                 [com.taoensso/sente "1.15.0"]
                 [com.taoensso/timbre "4.10.0"]

                 [juxt/crux-core "20.01-1.6.2-alpha"]
                 [juxt/crux-lmdb "20.01-1.6.2-alpha"]


                 ;[org.webjars.npm/bulma "0.7.5"]
                 ;[org.webjars.npm/material-icons "0.3.0"]
                 ;[org.webjars/webjars-locator "0.36"]

                 ; CLJS only
                 [kee-frame "0.3.3"]
                 [re-frame "0.10.7"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [reagent "0.9.0-rc4"]
                 [cljs-ajax "0.8.0"]
                 [ring-webjars "0.2.0"]
                 [reagent-forms "0.5.40"]
                 [breaking-point "0.1.2"]
                 [fork "1.2.3"]
                 [datascript "0.18.7"]
                 [re-posh "0.3.1"]
                 ]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot copa.core

  :plugins [[lein-immutant "2.1.0"]]

  :clean-targets ^{:protect false} [:target-path "target/cljsbuild"]
  :shadow-cljs {:nrepl {:port 7002}
                :builds
                       {:app
                        {:target     :browser
                         :output-dir "target/cljsbuild/public/js"
                         :asset-path "/js"
                         :modules    {:app {:entries [copa.app]}}
                         :devtools   {:watch-dir "resources/public"}}
                        :test
                        {:target    :node-test
                         :output-to "target/test/test.js"
                         :autorun   true}}}

  :npm-deps [[shadow-cljs "2.8.39"]
             [create-react-class "15.6.3"]
             [react "16.8.6"]
             [react-dom "16.8.6"]]

  :profiles {:uberjar       {:omit-source    true
                             :prep-tasks     ["compile" ["run" "-m" "shadow.cljs.devtools.cli" "release" "app"]]
                             :aot            :all
                             :uberjar-name   "copa.jar"
                             :source-paths   ["env/prod/clj"]
                             :resource-paths ["env/prod/resources"]}

             :dev           [:project/dev :profiles/dev]
             :test          [:project/dev :project/test :profiles/test]

             :project/dev   {:jvm-opts       ["-Dconf=dev-config.edn"]
                             :dependencies   [[binaryage/devtools "0.9.10"]
                                              [cider/piggieback "0.4.1"]
                                              [expound "0.7.2"]
                                              [pjstadig/humane-test-output "0.9.0"]
                                              [prone "1.6.4"]
                                              [re-frisk "0.5.4.1"]
                                              [ring/ring-devel "1.7.1"]
                                              [ring/ring-mock "0.4.0"]
                                              [fsmviz "0.1.3"]]
                             :plugins        [[com.jakemccrary/lein-test-refresh "0.24.1"]]


                             :source-paths   ["env/dev/clj"]
                             :resource-paths ["env/dev/resources"]
                             :repl-options   {:init-ns user}
                             :injections     [(require 'pjstadig.humane-test-output)
                                              (pjstadig.humane-test-output/activate!)]}
             :project/test  {:jvm-opts       ["-Dconf=test-config.edn"]
                             :resource-paths ["env/test/resources"]

                             }
             :profiles/dev  {}
             :profiles/test {}})
