(ns index.viewer
  (:use [ring.middleware.params :only [wrap-params]]
        [hiccup.core :only [defhtml html]])
  (:require [clj-http.client :as http]
            [org.danlarkin.json :as json])
  (:import (java.net URL URLEncoder)))

(def index-url "http://localhost:9200/irc/_search")

(def page-size 100)

(defn query [string page]
  (-> index-url
      (http/get {:query-params
                 {:q string
                  :size page-size
                  :from (* page page-size)
                  :sort "time:desc"}})
      (:body)
      (json/decode)
      (:hits)
      (:hits)
      ((partial map :_source))))

(def fifteen-minutes (* 1000 60 15))

(defhtml log-time [time page query-string]
  [:a {:href (format "?q=%s"
                     (URLEncoder/encode
                      (format "(%s) AND time:[%s TO %s]"
                              query-string
                              (- time fifteen-minutes)
                              (+ time fifteen-minutes))))}
   (java.util.Date. time)])

(defhtml log-line [i page query-string]
  [:li
   (log-time (:time i) page query-string) " "
   "&lt;"
   [:a {:href (format "?q=%s"
                     (URLEncoder/encode
                      (format "(%s) AND sender:%s"
                              query-string
                              (:sender i))))
        :style "text-decoration:none;color:black"}
    (:sender i)] "> "
   (interpose
    \space
    (for [n (.split (:message i) " ")]
      (if (.startsWith n "http://")
        (format "<a href=\"%s\">%s</a>" n n)
        (format "<a style=\"text-decoration:none;color:black\"
 href=\"?page=0&q=%s\">%s</a>"
                (URLEncoder/encode
                 (format "(%s) AND %s"
                         query-string
                         (first (re-seq #"\w+" n))))
                n))))])

(defhtml nav-bar [page query-string]
  [:a {:href (str "?page=" (inc page) "&q=" (URLEncoder/encode query-string))}
   "previous page"]
  " "
  [:a {:href (str "?page=" (dec page) "&q=" (URLEncoder/encode query-string))}
   "next page"])

(defn foo [{{:strs [page q]}  :params}]
  (let [page (Integer/parseInt (or page "0"))
        query-string (or q "channel:safe")]
    {:status 200
     :body (html
            [:html
             [:head [:title query-string " - logs - page " page]]
             [:body
              [:form
               [:input {:type "text" :name "page" :value page}]
               [:input {:type "text" :name "q" :value query-string}]
               [:input {:type "submit" :name "asubmit" :value "submit"}]]
              (nav-bar page query-string)
              [:ul
               (for [i (query query-string page)]
                 (log-line i page query-string))]
              (nav-bar page query-string)]])}))

(alter-var-root #'foo wrap-params)
