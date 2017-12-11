(ns pipeline.filter
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [cheshire.core :as json])
  (:import (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)))

(defn -handleRequest
  [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let [writer (io/writer output-stream)
        reader (io/reader input-stream)
        blob (json/generate-string
               (keep #(get-in % [:kinesis :data])
                     (:Records (json/parse-stream reader true))))]
    (println ">>> Filtering JSON blobs")
    (println ">>> Blob" blob)
    (doto writer
      (.write blob)
      (.flush))))


