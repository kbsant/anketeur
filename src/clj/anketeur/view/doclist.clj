(ns anketeur.view.doclist
  (:require
    [anketeur.form :as form]
    [anketeur.view.parts :as parts]))

(defn render-selector-row
  [file-link link-fn i {:keys [surveyname surveyno description updated] :as survey-info}]
  ^{:key i}
  [:tr
    (when file-link [:td [:input {:type :radio :name "surveyno" :value surveyno}]])
    [:td
     [:a
      {:href (link-fn survey-info)}
      (or surveyname [:span.label.label-default "(no name)"])]]
    [:td.wide-cell description]
    [:td.narrow-cell updated]])

(defn open-doclist
  [{:keys [open-link-base doclist file-link] :as view-info}]
  (when-not (empty? doclist)
    (let [link-fn #(str open-link-base (:surveyno %))
          row-renderer (partial render-selector-row file-link link-fn)]
      [:table.table.doclist
        [:thead
         [:tr
          (when file-link [:th {:width "3em"}])
          [:th "Title"]
          [:th.wide-cell "Description"]
          [:th.narrow-cell "Updated"]]]
        [:tbody
          (map-indexed row-renderer doclist)]])))

(defn file-action-toolbar
  [{:keys [csrf-token doclist] :as view-info}]
  (let [no-selection? (empty? doclist)]
    [:div.row
      (form/anti-forgery-field csrf-token)
      [:div.col-xs-2.toolbar-tile
        [:button
           {:type :submit :name "fileaction" :value "new"}
           [:img {:src "/img/file.png"}]
           [:br]
           [:p "New"]]]
      [:div.col-xs-2.toolbar-tile
         [:button
           {:disabled no-selection? :type :submit :name "fileaction" :value "copy"}
           [:img {:src "img/copy.png"}]
           [:br]
           [:p "Copy"]]]
      [:div.col-xs-2.toolbar-tile
         [:button
           {:disabled no-selection? :type :submit :name "fileaction" :value "delete"}
           [:img {:src "img/batsu-small.png"}]
           [:br]
           [:p "Delete"]]]]))

(defn content
  [{:keys [flash-errors headline file-link] :as view-info}]
  [:div.container
    (form/navbar [:a {:href "/"} "Home"])
    (when flash-errors (form/errors-div flash-errors))
    [:div.row
      [:h1 headline]
      [:br]]
    [:form.inline
      {:method :POST
       :action file-link}
      (when file-link (file-action-toolbar view-info))
      (open-doclist view-info)]])

(defn render [{:keys [glossary] :as view-info}]
  (parts/main glossary 
    nil
    (content view-info)))

