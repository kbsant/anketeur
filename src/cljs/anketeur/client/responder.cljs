(ns anketeur.client.responder
  (:require
    [clojure.string :as string]
    [anketeur.client.event :as event]
    [anketeur.model :as model]
    [anketeur.form :as form]
    [anketeur.client.ui :as ui]
    [reagent.core :as r]
    [anketeur.client.ajax :as appajax]
    [ajax.core :refer [GET POST]]))

(defonce docstate
  (atom {}))

(defonce state
  (r/atom {}))

(defn fade-save-status [state]
   (ui/single-timer
     #(swap! state assoc-in [:client-state :save-status] "")
     3000))

(defn submit [form-id]
  (.submit (ui/element-by-id form-id)))

;; TODO queuing and auto-save and status display and async stuff
(defn save-answers!
 ([]
  (save-answers! fade-save-status))
 ([action-after]
  (let [params (select-keys @state [:surveyno :answers])]
    (reset! docstate params)
    (POST
      "/answer"
      {:params params
       :handler #(do
                   (swap! state assoc-in [:client-state :save-status] %)
                   (action-after state))
       ;;TODO dont fade error
       :error-handler #(swap! state assoc-in [:client-state :save-status] (str %))}))))

(defn save-if-changed! []
  (let [params (select-keys @state [:surveyno :answers])]
    (when (not= params @docstate)
      (save-answers!))))

(defn form-change-handler [state form-id index ev]
  (let [form (ui/element-by-id form-id)
        value (ui/form-element-value form index)]
    (swap! state assoc-in [:answers index] value)))

(defn question-list [state]
  (fn []
    (let [state-info @state
          questions (model/question-list-view state-info)
          form-id "response"
          change-handler (partial form-change-handler state form-id)
          render-question #(form/render-question state-info % change-handler)]
      [:div.container
        [:h1 (:surveyname state-info)]
        [:p (:description state-info)]
        [:p (-> state-info :answers str)]
        [:ul]
        [:div.row
          [:span.font-weight-bold (str "Question List (" (count questions) ")")]]
        (when-not (empty? questions)
          [:form {:id form-id}
            (map render-question questions)])])))

(defn save-control-group [form-id state]
  [:form.inline
    {:id form-id :action (str "/answer/completed/" (:surveyno @state)) :method :GET}
    [:input.mr-1
      {:type :button
       :value "Save"
       :on-click #(save-answers!)}]
    [:input.mr-1
      {:type :button
       :value "Save and complete"
       :on-click #(save-answers! (fn [](submit form-id)))}]
    (let [save-status (get-in @state [:client-state :save-status])]
      [:span
       {:style (ui/fade-opacity save-status)}
       save-status])])

(defn home-page []
  [:div.container
    [save-control-group "save-top" state]
    [question-list state]
    [save-control-group "save-bottom" state]])

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-json js/transitState)
        survey-info (:survey-info init-state)]
    (when survey-info
      (swap! state merge survey-info))))

(defn ^:export init []
  (appajax/load-interceptors! js/context js/csrfToken)
  (mount-components)
  (load-transit!)
  (ui/repeat-timer save-if-changed! 60000))

