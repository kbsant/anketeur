(ns anketeur.client.opener
  (:require
    [clojure.string :as string]
    [anketeur.client.ui :as ui]
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
            [:h4 add-subhead]
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
        [:h4 open-subhead]
        (when (nil? add-link)
          [ui/errors-div :flash-errors state])
        (ui/doc-selector #(str open-link-base (:surveyno %)) @state)])))

(defn home-page []
  [:div.container
    [doc-opener state]])

(defn mount-components []
  (r/render [home-page] (.getElementById js/document "app")))

(defn load-transit! []
  (let [init-state (ui/read-json js/transitState)]
    (swap! state merge init-state)))

(defn ^:export init []
  (mount-components)
  (load-transit!))

