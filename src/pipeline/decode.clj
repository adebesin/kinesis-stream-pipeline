(ns pipeline.decode
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [cheshire.core :as json])
  (:import (java.util Base64)
           (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)))

(defn -handleRequest
  [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let [writer (io/writer output-stream)
        reader (io/reader input-stream)
        blob (json/generate-string
               (keep #(String.
                       (.. Base64 getDecoder (decode %)))
                    (json/parse-stream reader true)))]
    (println ">>> Decoding JSON blobs")
    (println ">>> Blob" blob)
    (doto writer
      (.write blob)
      (.flush))))


