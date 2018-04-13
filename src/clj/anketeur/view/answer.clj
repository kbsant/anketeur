(ns anketeur.view.answer
  (:require
    [anketeur.view.parts :as parts]
    [anketeur.model :as model]
    [anketeur.form :as form]
    [ring.util.anti-forgery :refer [anti-forgery-field]]
    [hiccup.page :as page]))

(defn add-content [{:keys [surveyname surveyno description] :as survey-info}]
  [:div.container
    (form/navbar [:a {:href "/"} "Home"])
    [:h1 "Respond to a survey"]
    [:h2 surveyname]
    [:p description]
    [:form {:method :POST :action "/answer/add"}
      (anti-forgery-field)
      [:input {:type :hidden :name "surveyno" :value surveyno}]
      [:input {:type :submit :value "Start"}]]])

(defn add [{:keys [survey-info glossary message]}]
  (parts/main
    glossary
    nil
    (add-content survey-info)))

(defn opener [data]
  (let [init-state (merge
                     (select-keys data [:glossary :flash-errors :doclist :open-link-base])
                     {:headline "Respond to a Survey"
                      :open-subhead "Open"})]
    (parts/spa-appbase data init-state "anketeur.client.opener.init();")))

(defn responder [{:keys [survey-info surveyno formno flash-errors] :as data}]
  (parts/appbase
    data
    (parts/js-transit-state
      "transitState"
      {:survey-info survey-info :flash-errors flash-errors})
    (list
      (page/include-js "/js/app.js")
      [:script
        {:type "text/javascript"}
        "anketeur.client.responder.init();"]
      [:div.container
        [:noscript
          [:p
           [:a {:href (str "/answernojs/id/" surveyno "/formno/" formno)}
            "A form without javascript is available."]]]])))

(defn content-nojs [{:keys [glossary survey-info surveyno formno flash-errors]}]
  (let [questions (model/question-list-view survey-info)
        render-question (partial form/preview-question survey-info)]
    [:div.container
      (form/navbar [:a {:href "/"} "Home"])
      [:h1 (:surveyname survey-info)]
      [:p (:description survey-info)]
      [:p (-> survey-info :answers str)]
      [:ul
        [:li "Add submit/mark as complete"]
        [:li "See whether the survey can be resumed after session expiry/survives after back/refresh"]]
      [:div.row
        [:span.font-weight-bold (str "Question List (" (count questions) ")")]
        (when-not (empty? questions)
          [:form#response {:action "/answernojs/submit" :method :POST}
            [:input {:type :hidden :name "formno" :value formno}]
            [:input {:type :hidden :name "surveyno" :value surveyno}]
            (map render-question questions)
            [:input {:type :submit :value "Submit"}]])]]))


(defn responder-nojs [{:keys [glossary flash-errors] :as data}]
  (parts/main glossary
    nil
    (content-nojs data)))
