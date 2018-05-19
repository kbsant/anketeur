(ns anketeur.view.parts
  (:require
    [anketeur.util.json :as json]
    [anketeur.form :as form]
    [hiccup.core :as hc]
    [hiccup.page :as page])
  (:import
    org.apache.commons.text.StringEscapeUtils))

(defn js-script [& contents]
  [:script {:type "text/javascript"}
    (apply str contents)])

(defn js-var [name value]
  (str "var " name " = " value ";"))

(defn js-quot [value]
  (str "'" value "'"))

(defn js-dquot [value]
  (str \" value \"))

(defn escape-json [data]
  (StringEscapeUtils/escapeJson data))

(defn js-transit-var [name value]
  (js-script
    (js-var name (-> value json/write-utf8 escape-json js-dquot))))

(defn main [glossary headitems content]
  (page/html5
    [:head
      [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:link {:rel "shortcut icon" :type "image/png" :href "/img/favicon.png"}]
      [:title (glossary :title)]
      (page/include-css
         "/css/screen.css"
         "/assets/bootstrap/css/bootstrap.min.css"
         "/assets/font-awesome/css/font-awesome.min.css")
      (page/include-js
         "/assets/jquery/jquery.min.js"
         "/assets/bootstrap/js/bootstrap.min.js")
      headitems]
    [:body content]))

(defn appbase [{:keys [glossary servlet-context csrf-token]} headitems content]
  (page/html5
    [:head
      [:meta {:charset "UTF-8"}]
      [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:link {:rel "shortcut icon" :type "image/png" :href "/img/favicon.png"}]
      [:title (glossary :title)]
      (page/include-css
         "/css/screen.css"
         "/assets/bootstrap/css/bootstrap.min.css"
         "/assets/font-awesome/css/font-awesome.min.css"
         "/css/main.css")
      (page/include-js
         "/assets/jquery/jquery.min.js"
         "/assets/bootstrap/js/bootstrap.min.js")
      (js-script
        (js-var "context" (js-quot servlet-context))
        (js-var "csrfToken" (js-quot csrf-token)))
      headitems]
    [:body
      (form/navbar [:a {:href "/"} "Home"])
      [:div#app
        [:div.container
          [:div.jumbotron [:h1 (glossary :title)]]
          [:div.three-quarters-loader "Loading… (uses javascript)"]
          (or
            content
            [:noscript
              [:p "This page uses javascript, but it seems to be disabled. Please enable it and reload."]])]]]))

(defn spa-appbase [view-data init-state app-js]
  (appbase
    view-data
    (js-transit-var
      "transitState"
      init-state)
    (list
      (page/include-js "/js/app.js")
      [:script
        {:type "text/javascript"}
        (str app-js ";")])))

