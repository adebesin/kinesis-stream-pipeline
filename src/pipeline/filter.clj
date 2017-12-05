(ns pipeline.filter
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [pipeline.lib :as lib])
  (:import (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)
           (com.amazonaws.services.sns.model PublishRequest)))

(defn -handleRequest
  [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let [w (io/writer output-stream)
        parameters (json/read (io/reader input-stream) :key-fn keyword)]
    (println ">>> Filtering JSON blobs")
    (println ">>> Parameters" parameters)
    (.write w ^String (json/write-str
                        (keep #(get-in % [:kinesis :data])
                              (:Records parameters))))
    (.flush w)))