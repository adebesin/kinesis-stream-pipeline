(ns pipeline.process-raw-stream
  (:gen-class
    :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [pipeline.lib :as lib])
  (:import (java.io InputStream OutputStream OutputStreamWriter)
           (com.amazonaws.services.lambda.runtime Context)
           (com.amazonaws.services.sns.model PublishRequest)))

(def ^:const sns-arn (System/getenv "SNS_ARN"))

(defn -handleRequest [this ^InputStream input-stream ^OutputStream output-stream ^Context context]
  (let [w (io/writer output-stream)
        parameters (lib/parse-stream input-stream)
        process (transduce lib/blob-xform conj {:Records parameters})]
    (doseq [p process]
      (if (:error p)                                        ;TODO implement business logic
        (lib/publish-sns "" ""))
      (lib/write-dynamo ""))

    (println ">>> Events: " process)
    (println ">>> Parameters " parameters)))
