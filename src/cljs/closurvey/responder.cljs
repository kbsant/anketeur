(ns closurvey.responder
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event]
    [closurvey.model :as model]
    [closurvey.client.surveyform :as form]
    [closurvey.client.ui :as ui]
    [reagent.core :as r]
    [closurvey.ajax :as appajax]
    [ajax.core :refer [GET POST]]))

(defonce state
  (r/atom {}))

;; TODO queuing and auto-save and status display and async stuff
(defn save-answers! []
  (let [params (select-keys @state [:surveyno :answers])]
    (POST
      "/answer"
      {:params params
       ;; TODO fade out after saving
       :handler #(swap! state assoc-in [:client-state :save-status] %)
       :error-handler #(swap! state assoc-in [:client-state :save-status] (str %))})))

(defn question-list [state]
  (fn []
    (let [state-info @state
          questions (model/question-list-view state-info)
          render-question (partial form/render-form-question state state-info)]
      [:div.container
        [:h1 (:surveyname state-info)]
        [:ul
          [:li "Add survey description"]
          [:li "Add auto-save and display auto-save function."]
          [:li "Add dedup number when a new survey is loaded"]
          [:li "Add submit/mark as complete"]
          [:li "See whether the survey can be resumed after session expiry/survives after back/refresh"]]
        [:div.row
          [:span.font-weight-bold (str "Question List (" (count questions) ")")]]
        (when-not (empty? questions)
          (map-indexed render-question questions))])))

(defn home-page []
  [:div.container
    [:ul [:li [:a {:href "/"} "Home"]]]
    [:form.inline
      [:input.mr-1
        {:type :button
         :value "Save"
         :on-click #(save-answers!)}]
      [:input.mr-1
        {:type :button
         :value "Save and publish"}]]
    [:div.row
      ;; TODO fade out after setting
      [:p (str (get-in @state [:client-state :save-status]))]]
    [question-list state]])

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-transit-state js/transitState)
        survey-info (:survey-info init-state)]
    (when survey-info
      (swap! state merge survey-info))))

(defn ^:export init []
  (appajax/load-interceptors! js/context js/csrfToken)
  (mount-components)
  (load-transit!))

