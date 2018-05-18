(ns anketeur.view.edit
  (:require
    [anketeur.view.parts :as parts]
    [anketeur.view.doclist :as doclist]
    [hiccup.page :as page]))

(defn opener [data]
  (let [view-state (merge
                     (select-keys data [:csrf-token :glossary :flash-errors :doclist :add-link :open-link-base])
                     {:headline "Survey Editor"
                      :add-subhead "Create a Survey"
                      :open-subhead "Edit a Survey"})]
    (doclist/render view-state)))

(defn editor [data]
  (parts/appbase
    data
    (parts/js-transit-var
      "transitState"
      (select-keys data [:survey-info :response-link-base :export-link-base :flash-errors]))
    (list
      (page/include-js "/js/app.js")
      [:script
        {:type "text/javascript"}
        "anketeur.client.edit.init();"])))



