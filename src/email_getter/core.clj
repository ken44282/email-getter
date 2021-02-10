(ns email-getter.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure-mail.core :refer :all]
            [clojure-mail.gmail :as gmail]
            [clojure-mail.message :as message])
  (:import (java.text SimpleDateFormat)
           (java.util Date)))

(defn multipart? [m]
  (:multipart? (message/read-message m)))

(defn output-txt [m]
  (let [sdf (SimpleDateFormat. "yyyyMMddHHmmssSSS")
        now (.format sdf (Date.))
        dir (io/as-file "mail")]
    (.mkdirs dir)
    (with-open [writer (io/writer (str dir "/" now ".txt"))]
      (message/mark-read m)
      (.write writer (:address (first (message/from m))))
      (.newLine writer)
      (.write writer (message/subject m))
      (.newLine writer)
      (if (multipart? m)
        (->> (message/get-content m)
             (message/read-multi)
             (map #(.write writer (:body %)))
             (doall))
        (.write writer (message/get-content m))))))

(defn -main [& args]
  (let [gstore (gmail/store "test@gmail.com" "password")
        unread-msgs (unread-messages gstore "INBOX")]
    (doall (map output-txt unread-msgs))))