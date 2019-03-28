(defproject tarayo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [commons-codec "1.12"]
                 [com.sun.mail/jakarta.mail "1.6.3"]
                 [jakarta.mail/jakarta.mail-api "1.6.3"]
                 [nano-id "0.9.3"]
                 [org.apache.tika/tika-core "1.20"]]

  :profiles
  {:dev {:dependencies [[org.clojure/clojure "1.9.0"]
                        [fudje "0.9.7"]
                        ]}

   :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.0"]]}
   :it [:dev {:dependencies [[org.clojure/data.json "0.2.6"]
                             [camel-snake-kebab "0.4.0"]
                             [http-kit "2.3.0"]]
              :test-paths ["integration/test"]}]}
  :aliases
  {"test-all" ["with-profile" "1.9:1.10" "test"]
   "test-whale" ["with-profile" "1.9,it:1.10,it" "test"]})
