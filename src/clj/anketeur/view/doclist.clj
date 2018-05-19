(ns anketeur.view.doclist
  (:require
    [anketeur.form :as form]
    [anketeur.view.parts :as parts]))

(defn render-selector-row
  [link-fn i {:keys [surveyname] :as survey-info}]
  ^{:key i}
  [:tr
    [:td
     [:a
      {:href (link-fn survey-info)}
      (or surveyname [:span.label.label-default "(no name)"])]]])

(defn open-doclist
  [{:keys [open-link-base doclist] :as view-info}]
  (when-not (empty? doclist)
    (let [link-fn #(str open-link-base (:surveyno %))
          row-renderer (partial render-selector-row link-fn)]
      [:table.table
        [:thead [:tr [:th "Survey name"]]]
        [:tbody
          (map-indexed row-renderer doclist)]])))

(defn content
  [{:keys [csrf-token flash-errors headline add-link] :as view-info}]
  [:div.container
    (form/navbar [:a {:href "/"} "Home"])
    (when flash-errors (form/errors-div flash-errors))
    [:div.row
      [:h1 headline]
      [:br]]
    (when add-link
      [:div.row
        [:form.inline
          {:method :post
           :action add-link}
          (form/anti-forgery-field csrf-token)
          [:div.col-xs-2.toolbar-tile
            [:button
             {:type "submit"}
             [:img {:src "/img/file.png"}]
             [:br]
             [:p "New"]]]
          [:div.col-xs-2.toolbar-tile
            [:button
             {:disabled true}
             [:img {:src "img/copy.png"}]
             [:br]
             [:p "Copy"]]]
          [:div.col-xs-2.toolbar-tile
             [:button
              {:disabled true}
              [:img {:src "img/batsu-small.png"}]
              [:br]
              [:p "Delete"]]]]])
    (open-doclist view-info)])

(defn render [{:keys [glossary] :as view-info}]
  (parts/main glossary 
    nil
    (content view-info)))

