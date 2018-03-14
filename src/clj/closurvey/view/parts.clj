(ns closurvey.view.parts
  (:require
    [clojure.data.codec.base64 :as base64]
    [closurvey.util.json :as json]
    [hiccup.core :as hc]
    [hiccup.page :as page]))

(defn js-script [& contents]
  [:script {:type "text/javascript"}
    (apply str contents)])

(defn js-var [name value]
  (str "var " name " = " value ";"))

(defn js-quot [value]
  (str "'" value "'"))

(defn js-quot-b64 [raw]
  (-> (.getBytes raw) base64/encode (String. "UTF-8") js-quot))

(defn js-transit-b64 [raw]
  (-> (json/write-utf8 raw) js-quot-b64))

(defn js-transit-state [name state]
  (js-script
    (js-var name (js-transit-b64 state))))

(defn main [glossary headitems content]
  (page/html5
    [:head
      [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:link {:rel "shortcut icon" :type "image/png" :href "/img/favicon.png"}]
      [:title (glossary :title)]
      (page/include-css
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
      [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
      [:link {:rel "shortcut icon" :type "image/png" :href "/img/favicon.png"}]
      [:title (glossary :title)]
      (page/include-css
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
      [:div#navbar]
      [:div#app
        [:div.container
          [:div.jumbotron [:h1 (glossary :title)]]
          [:div.three-quarters-loader "Loading… (requires javascript and html5)"]
          [:noscript
            [:p "This application requires javascript, but it seems to be disabled. Please enable it and reload."]]]]
      content]))

(defn spa-appbase [view-data init-state app-js]
  (appbase
    view-data
    (js-transit-state
      "transitState"
      init-state)
    (list
      (page/include-js "/js/app.js")
      [:script
        {:type "text/javascript"}
        (str app-js ";")])))

