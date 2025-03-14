(defproject tetris-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.clojure/clojurescript "1.11.132"]
                 [cljfx "1.9.3"]
                 [org.clojure/core.async "1.7.701"]
                 [hiccups "0.3.0"]
                 [rum "0.12.11"]
                 [io.github.clj-kondo/config-rum-rum "1.0.0"]]
  :repl-options {:init-ns cljfx-stateless.core}
  :source-paths ["src"]
  :plugins [[lein-cljsbuild "1.1.8"]]
  :cljsbuild {:builds [{:id "cljs_simple"
                        :source-paths ["src/cljs_simple"]
                        :compiler {:main cljs_simple.core
                                   :output-to "resources/public/simple/js/app.js"
                                   :output-dir "resources/public/simple/js/out"
                                   :asset-path "js/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "cljs_rum"
                        :source-paths ["src/cljs_rum"]
                        :compiler {:main cljs-rum.core
                                   :output-to "resources/public/rum/js/app.js"
                                   :output-dir "resources/public/rum/js/out"
                                   :asset-path "js/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "rum_canvas"
                        :source-paths ["src/rum_canvas"]
                        :compiler {:main rum-canvas.core
                                   :output-to "resources/public/rum_canvas/js/app.js"
                                   :output-dir "resources/public/rum_canvas/js/out"
                                   :asset-path "js/out"
                                   :optimizations :none
                                   :source-map true}}]})
