(defproject witan.gwyn "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [net.mikera/core.matrix "0.55.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [witan.workspace-api "0.1.22"]
                 [prismatic/schema "1.1.3"]
                 [kixi/stats "0.2.1"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot witan.gwyn.gwyn
  :target-path "target/%s"
  :profiles {:dev {:dependencies [[witan.workspace-executor "0.2.6"
                                   :exclusions [witan.workspace-api]]]}})
