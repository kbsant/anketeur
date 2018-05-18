(ns anketeur.view.result
  (:require
    [anketeur.view.parts :as parts]
    [anketeur.view.doclist :as doclist]
    [hiccup.page :as page]))

(defn opener [data]
  (let [view-state (merge
                     (select-keys data [:glossary :flash-errors :doclist :open-link-base])
                     {:headline "Survey Results"
                      :open-subhead "Open"})]
    (doclist/render view-state)))

(defn result-page [{:keys [survey-info flash-errors] :as data}]
  (parts/appbase
    data
    (parts/js-transit-var "transitState" data)
    (list
      (page/include-js "/js/app.js")
      [:script
        {:type "text/javascript"}
        "anketeur.client.result.init();"])))

