(ns up.passwd.core
  (:require
   [reagent.core :as r]
   [clojure.test.check.generators :as gen]))

(defn char-upper []
  (gen/fmap char
            (gen/one-of [(gen/choose 65 90)])))

(defn char-lower []
  (gen/fmap char
            (gen/one-of [(gen/choose 97 122)])))

(defn char-special []
  (gen/one-of [(gen/elements [\! \$ \% \^ \&])]))

(defn gen-pwd [& [{:keys [len] :as opts}]]
  (let [len (or len 15)]
    (apply str
           (gen/sample 
            (gen/frequency [[25 (char-upper)]
                            [25 (char-lower)]
                            [25 (char-special)]
                            [25 gen/s-pos-int]])
            len))))

(defn password-generator [data]
  [:form {:class "form-horizontal"}
   [:div {:class "form-group"}
    [:label {:class "col-sm-3 control-label"} "Password Length"]
    [:div {:class "col-sm-9"}
     [:input {:class "form-control" 
              :type "text" :value (:pwd-length @data)
              :on-change #(let [new-val (-> % .-target .-value)]
                            (swap! data assoc-in [:pwd-length]
                                   (if (or (empty? new-val) (js/isNaN new-val))
                                     nil
                                     (let [v (js/parseInt new-val)]
                                       (if (< v 1001)
                                         v
                                         (get-in @data [:pwd-length]))))))}]]]
   [:div {:class "form-group"}
    [:div {:class "col-sm-offset-3 col-sm-9"}
     [:button {:class "btn btn-default"
               :on-click #(do
                            (swap! data assoc-in [:result] 
                                   (gen-pwd {:len (:pwd-length @data)}))
                            (.preventDefault %))}
      "Generate Random Password"]]]
   [:div {:class "form-group"}
    [:div {:class "col-sm-offset-1 alert alert-success" 
          :id "rnd-pwd"} 
     (:result @data)]]])

(def data (r/atom {:pwd-length 15
                   :result (gen-pwd 15)}))

(defn main []
  (if-let [node (.getElementById js/document "pwd-gen")]
    (r/render-component [password-generator data] node)))

(main)



