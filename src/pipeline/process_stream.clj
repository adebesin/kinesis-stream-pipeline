(ns pipeline.process-stream
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [data-science-poc.lib :as lib])
  (:import (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)
           (com.amazonaws.services.sns.model PublishRequest)))

(def ^:const sns-arn (System/getenv "SNS_ARN"))

(defn -handleRequest [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let [w (io/writer output-stream)
        parameters (lib/parse-stream input-stream)
        process (transduce lib/blob-xform conj (lib/extract-records parameters))]
    (doseq [p process]
      (lib/publish-sns "" ""))
    (println ">>> Events: " process)
    (println ">>> Parameters " parameters)))
3