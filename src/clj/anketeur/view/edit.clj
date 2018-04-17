(ns anketeur.view.edit
  (:require
    [anketeur.view.parts :as parts]
    [hiccup.page :as page]))

(defn opener [data]
  (let [init-state (merge
                     (select-keys data [:glossary :flash-errors :doclist :add-link :open-link-base])
                     {:headline "Survey Editor"
                      :add-subhead "Create a Survey"
                      :open-subhead "Edit a Survey"})]
    (parts/spa-appbase data init-state "anketeur.client.opener.init();")))

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



