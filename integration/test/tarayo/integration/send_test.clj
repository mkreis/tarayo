(ns tarayo.integration.send-test
  (:require [camel-snake-kebab.core :as csk]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.test :as t]
            [fudje.sweet :as fj]
            [org.httpkit.client :as http]
            [tarayo.core :as core])
  (:import java.util.Calendar))

(defn- random-address []
  (format "%s@example.com" (java.util.UUID/randomUUID)))

(def ^:private mailhog-server
  {:host "localhost" :port 1025})

(def ^:private mailhog-search-api-endpoint
  (format "http://%s:8025/api/v2/search" (:host mailhog-server)))

(defn- find-mail-by-from [from-address]
  (let [resp @(http/get mailhog-search-api-endpoint
                        {:query-params {:kind "from" :query from-address}})]
    (json/read-str (:body resp) :key-fn (comp keyword csk/->kebab-case))))

(defn- tarayo-message-id? [x]
  (if (sequential? x)
    (tarayo-message-id?  (first x))
    (some?  (re-seq #"^<[0-9A-Za-z]+\.[0-9]+@tarayo\..+>$" x))))

(defn- tarayo-user-agent? [x]
  (if (sequential? x)
    (tarayo-user-agent?  (first x))
    (some?  (re-seq #"^tarayo/.+$" x))))

(t/deftest simple-send-test
  (let [from (random-address)
        now (doto (Calendar/getInstance)
              (.set 2112 (dec 9) 3))]
    (with-open [conn (core/connect mailhog-server)]
      (core/send! conn {:from from
                        :to "alice@example.com"
                        :subject "hello"
                        :body "world"
                        :charset "UTF-8"
                        :date (.getTime now)}))
    (let [resp (find-mail-by-from from)
          item (get-in resp [:items 0])]
      (t/is (= 1 (:total resp)))
      (t/is (= 1 (count (:to item))))
      (t/is
       (compatible
        (first (:to item))
        (fj/contains {:mailbox "alice" :domain "example.com"})))

      (t/is
       (compatible
        (get-in item [:content :headers])
        (fj/contains {:charset ["UTF-8"]
                      :content-type ["text/plain; charset=UTF-8"]
                      :date [(fj/checker #(str/starts-with? % "Sat, 3 Sep 2112 "))]
                      :from [from]
                      :message-id (fj/checker tarayo-message-id?)
                      :subject ["hello"]
                      :to ["alice@example.com"]
                      :user-agent (fj/checker tarayo-user-agent?)})))

      (t/is (= "world" (get-in item [:content :body]))))))
