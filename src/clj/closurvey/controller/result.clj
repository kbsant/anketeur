(ns closurvey.controller.result
  (:require
    [cheshire.core :as json]
    [clojure.tools.logging :as log]
    [closurvey.layout :as layout]
    [closurvey.model :as model]
    [closurvey.survey :as survey]
    [closurvey.view.result :as view.result]
    [ring.util.http-response :as response]))

(defn render-opener []
  (let [doclist (->> (survey/query-docs nil)
                   vals
                   (into []))
        errors (when (empty? doclist)
                  ["No documents found. Please create a new document."])]
    (layout/render-hiccup
      view.result/opener
      {:glossary {:title "Survey Results"}
       :flash-errors errors
       :open-link-base "/result/id/"
       :doclist doclist})))

(defn read-aggregate-result [surveyno]
  (let [survey-info (survey/read-doc surveyno)
        answers (survey/read-answers surveyno)]
     (model/survey-result-agg survey-info answers)))

(defn render-result [surveyno]
  (let [result-agg (read-aggregate-result surveyno)]
    (log/info "surveyno: " surveyno "result-agg" result-agg)
    (layout/render-hiccup
      view.result/result-page
      (merge
        result-agg
        {:export-link-base "/result/export/"
         :glossary {:title "Survey Results"}}))))

(defmulti export-format (fn [format _] format))

(defmethod export-format "CSV" [_ data]
  (json/generate-string data {:pretty true}))

(defmethod export-format "JSON" [_ data]
  (json/generate-string data {:pretty true}))

(defmethod export-format "EDN" [_ data]
  (clojure.pprint/write data :stream nil))

(defn export [format surveyno]
  (let [result-agg (read-aggregate-result surveyno)]
    ;;    export-fn (get {"CSV" export-csv, "JSON" export-json, "EDN" export-edn})]
    (log/info "export survey: " surveyno "format: " format)
    (export-format format result-agg)))
