(ns anketeur.controller.result
  (:require
    [clojure.tools.logging :as log]
    [ashikasoft.webstack.layout :as layout]
    [anketeur.model :as model]
    [anketeur.survey :as survey]
    [anketeur.util.core :as util]
    [anketeur.view.result :as view.result]
    [ring.util.http-response :as response]))

(defn render-opener [ds]
  (let [doclist (survey/query-docs ds (complement :deleted?))
        errors (when (empty? doclist)
                  ["No documents found. Please create a new document."])]
    (layout/render-hiccup
      view.result/opener
      {:glossary {:title "Survey Results"}
       :flash-errors errors
       :open-link-base "/result/id/"
       :doclist doclist})))

(defn read-aggregate-result [ds surveyno]
  (let [survey-info (survey/read-doc ds surveyno)
        answers (survey/read-answers ds surveyno)]
     (model/survey-result-agg survey-info answers)))

(defn render-result [ds surveyno]
  (let [result-agg (read-aggregate-result ds surveyno)]
    (log/info "surveyno: " surveyno "result-agg" result-agg)
    (layout/render-hiccup
      view.result/result-page
      (merge
        result-agg
        {:export-link-base "/result/export/"
         :glossary {:title "Survey Results"}}))))

(defn export [ds format surveyno]
  (let [result-agg (read-aggregate-result ds surveyno)
        text (util/export-format format result-agg)]
    (layout/render-text text)))
