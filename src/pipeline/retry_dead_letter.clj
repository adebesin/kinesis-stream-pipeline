(ns pipeline.retry-dead-letter
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as str])
  (:import (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)))


(defn -handleRequest [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let [w (io/writer output-stream)
        parameters (json/read (io/reader input-stream) :key-fn keyword)]
    (println ">>> Processing SNS event")
    (println ">>> Parameters " parameters)))