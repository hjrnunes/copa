(defproject copa "0.1.1-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89" :scope "provided"]
                 [org.clojure/tools.cli "0.3.5"]
                 [selmer "1.0.0"]
                 [markdown-clj "0.9.88"]
                 [bouncer "1.0.0"]
                 [org.webjars/font-awesome "4.5.0"]
                 [org.webjars.bower/tether "1.1.1"]
                 [org.webjars/jquery "2.2.0"]
                 [compojure "1.5.1"]
                 [ring-middleware-format "0.7.0"]
                 [metosin/ring-http-response "0.6.5"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring-ttl-session "0.3.0"]
                 [ring "1.4.0" :exclusions [ring/ring-jetty-adapter]]
                 [mount "0.1.10"]
                 [buddy "0.10.0"]
                 [reagent "0.6.0-rc"]
                 [reagent-forms "0.5.24"]
                 [reagent-utils "0.1.7"]
                 [re-frame "0.7.0"]
                 [cljs-ajax "0.5.8"]
                 [metosin/compojure-api "1.1.7"]
                 [metosin/ring-swagger-ui "2.2.1-0"]
                 [luminus/config "0.5"]
                 [luminus-http-kit "0.1.4"]
                 [luminus-log4j "0.1.2"]
                 [luminus-nrepl "0.1.4"]
                 [luminus-migrations "0.2.6"]
                 [io.rkn/conformity "0.4.0"]
                 [prismatic/plumbing "0.5.3"]
                 [prismatic/schema "1.0.5"]
                 [json-html "0.3.8"]
                 [com.novemberain/monger "3.0.2"]
                 [hodgepodge "0.1.3"]
                 [bidi "2.0.9"]
                 [kibu/pushy "0.3.6"]
                 [funcool/cuerdas "0.8.0"]
                 [cljsjs/semantic-ui "2.2.2-0"]
                 [conman "0.6.0"]
                 [com.h2database/h2 "1.3.176"]
                 [cprop "0.1.9"]
                 [com.taoensso/timbre "4.7.4"]
                 [com.fzakaria/slf4j-timbre "0.3.2"]
                 ]

  :min-lein-version "2.0.0"
  :uberjar-name "copa.jar"
  :jvm-opts ["-server"]
  :resource-paths ["resources" "target/cljsbuild"]

  :main copa.core
  :migratus {:store :database :db ~(get (System/getenv) "DATABASE_URL")}

  :plugins [[lein-environ "1.0.1"]
            [lein-cljsbuild "1.1.1"]
            [lein-cprop "1.0.1"]
            [migratus-lein "0.4.1"]]
  :clean-targets ^{:protect false} [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :cljsbuild
  {:builds
   {:app
    {:source-paths ["src-cljs"]
     :compiler
                   {:output-to    "target/cljsbuild/public/js/app.js"
                    :output-dir   "target/cljsbuild/public/js/out"
                    :externs      ["react/externs/react.js"]
                    :pretty-print true}}}}

  :profiles
  {:uberjar       {:omit-source    true
                   :env            {:production true}
                   :prep-tasks     ["compile" ["cljsbuild" "once"]]
                   :cljsbuild
                                   {:builds
                                    {:app
                                     {:source-paths ["env/prod/cljs"]
                                      :compiler
                                                    {:optimizations :advanced
                                                     :pretty-print  false
                                                     :closure-warnings
                                                                    {:externs-validation :off :non-standard-jsdoc :off}}}}}

                   :aot            :all
                   :source-paths   ["env/prod/clj"]
                   :resource-paths ["env/prod/resources"]}
   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]
   :project/dev   {:dependencies   [[prone "1.0.1"]
                                    [ring/ring-mock "0.3.0"]
                                    [ring/ring-devel "1.4.0"]
                                    [pjstadig/humane-test-output "0.7.1"]
                                    [lein-figwheel "0.5.0-6"]
                                    [lein-doo "0.1.6"]
                                    [com.cemerick/piggieback "0.2.2-SNAPSHOT"]]
                   :plugins        [[lein-figwheel "0.5.0-6"]
                                    [lein-doo "0.1.6"]
                                    [org.clojure/clojurescript "1.7.228"]]
                   :cljsbuild
                                   {:builds
                                    {:app
                                     {:source-paths ["env/dev/cljs"]
                                      :compiler
                                                    {:main          "copa.app"
                                                     :asset-path    "/js/out"
                                                     :optimizations :none
                                                     :source-map    true}}
                                     :test
                                     {:source-paths ["src-cljs" "test-cljs"]
                                      :compiler
                                                    {:output-to     "target/test.js"
                                                     :main          "copa.doo-runner"
                                                     ;:optimizations :whitespace
                                                     :optimizations :advanced
                                                     :pretty-print  true}}}}

                   :figwheel
                                   {:http-server-root "public"
                                    :server-port      3449
                                    :nrepl-port       7002
                                    :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
                                    :css-dirs         ["resources/public/css"]
                                    :ring-handler     copa.handler/app}
                   :doo            {:build "test"}
                   :source-paths   ["env/dev/clj"]
                   :resource-paths ["env/dev/resources"]
                   :repl-options   {:init-ns user}
                   :injections     [(require 'pjstadig.humane-test-output)
                                    (pjstadig.humane-test-output/activate!)]
                   ;;when :nrepl-port is set the application starts the nREPL server on load
                   :env            {:dev        true
                                    :port       3000
                                    :nrepl-port 7000}}
   :project/test  {:resource-paths ["env/dev/resources" "env/test/resources"]}
   :profiles/dev  {}
   :profiles/test {}})
