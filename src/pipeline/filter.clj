(ns pipeline.filter
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.string :as str]
            [pipeline.lib :as lib])
  (:import (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)
           (com.amazonaws.services.sns.model PublishRequest)))

(defn -handleRequest
  [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let [w (io/writer output-stream)
        stream (io/reader input-stream)
        blob ^String (json/generate-string
                       (keep #(get-in % [:kinesis :data])
                             (:Records (json/parse-stream stream true))))]
    (println ">>> Filtering JSON blobs")
    (println ">>> Blob" blob)
    (doto w
      (.write blob)
      (.flush))))