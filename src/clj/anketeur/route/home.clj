(ns anketeur.route.home
  (:require
    [anketeur.controller.edit :as edit]
    [anketeur.controller.answer :as answer]
    [anketeur.controller.result :as result]
    [anketeur.controller.main :as main]
    [ashikasoft.webstack.middleware :as middleware]))

(defn- applyfn
  "A short form of (fn [_]( ... )).
  This returns a function that calls the given function with its arguments.
  The returned function is arity-1, but ignores its argument.
  We use this when we want to supply a handler (which must have arity-1)
  to the route definition, but we don't actually need the argument."
  ([f x] (fn [_] (f x)))
  ([f x & args] (fn [_] (apply f x args))))

(defn home-routes [env ds]
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/"
    (applyfn main/render env)]
   ["/fileaction"
    {:post (partial edit/file-action ds)}]
   ["/save"
    {:post (partial edit/save-action ds)}]
   ["/answer"
    {:get (applyfn answer/render-opener ds)
     :post (partial answer/answer-action ds)}]
   ["/answernojs"
    {:post (partial answer/answernojs-action ds)}]
   ["/answer/completed/:surveyno"
    (fn [{{:keys [surveyno]} :path-params}]
      (answer/completed ds surveyno))]
   ["/answer/id/:surveyno"
    (fn [{{:keys [surveyno]} :path-params}]
      (answer/render-add ds surveyno))]
   ["/answer/add"
    {:post (partial answer/add-action ds)}]
   ["/answer/id/:surveyno/formno/:formno"
    (fn [{{:keys [surveyno formno]} :path-params}]
      (answer/render-responder ds surveyno formno))]
   ["/answernojs/id/:surveyno/formno/:formno"
    (fn [{{:keys [surveyno formno]} :path-params}]
      (answer/render-responder-nojs ds surveyno formno))]
   ["/open"
    (applyfn edit/render-opener ds)]
   ["/edit/id/:surveyno"
    (fn [{{:keys [surveyno]} :path-params}]
      (edit/render-editor ds surveyno))]
   ["/edit/export/EDN/id/:surveyno"
    (fn [{{:keys [surveyno]} :path-params}]
      (edit/export ds surveyno))]
   ["/result"
    (applyfn result/render-opener ds)]
   ["/result/id/:surveyno"
    (fn [{{:keys [surveyno]} :path-params}]
      (result/render-result ds surveyno))]
   ["/result/export/:format/id/:surveyno"
    (fn [{{:keys [format surveyno]} :path-params}]
      (result/export ds format surveyno))]])

