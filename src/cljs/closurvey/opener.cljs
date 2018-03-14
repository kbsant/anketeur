(ns closurvey.opener
  (:require
    [clojure.string :as string]
    [closurvey.client.event :as event]
    [closurvey.client.ui :as ui]
    [reagent.core :as r]))

(defonce state
  (r/atom
    {:doclist []}))

(defn doc-opener [state]
  (fn []
    (let [{:keys [headline add-subhead add-link open-subhead open-link-base]} @state]
      [:div.row
        [:h1 headline]
        (when add-link
          [:div
            [:ul
              [:li add-subhead]]
            [:form.inline
              {:method :post
               :action add-link}
              [ui/anti-forgery-field js/csrfToken]
              [:input
               {:type :hidden
                :name ""}]
              [:input
                {:type :submit
                 :value "Create new"}]]])
        [:ul
          [:li open-subhead]]
        [(partial ui/doc-selector #(str open-link-base (:surveyno %))) state]])))

(defn home-page []
  [:div.container
    [:li [:a {:href "/"} "Home"]]
    [doc-opener state]])

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-transit-state js/transitState)]
    (swap! state merge init-state)))

(defn init! []
  (mount-components)
  (load-transit!))

