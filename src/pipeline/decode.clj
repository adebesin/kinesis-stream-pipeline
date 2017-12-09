(ns pipeline.decode
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.string :as str]
            [pipeline.lib :as lib])
  (:import (java.util Base64)
           (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)
           (com.amazonaws.services.sns.model PublishRequest)))

(defn -handleRequest
  [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let [w (io/writer output-stream)
        stream (io/reader input-stream)
        blob (json/generate-string
               (keep #(String.
                       (.. Base64 getDecoder (decode %)))
                    (json/parse-stream stream true)))]
    (println ">>> Decoding JSON blobs")
    (println ">>> Blob" blob)
    (doto w
      (.write blob)
      (.flush))))
