(ns up.validate.core)

(defn parse-number
  "Returns :invalid if invalid, :empty if empty or nil. Otherwise
  returns anything that resembles a number. Notice that '10.' gets
  parsed valid number 10."
  [raw]
  (let [n (re-matches #"^(\d*)\.$" (str raw))]
    (cond 
      (number? raw)
      raw
      
      (or (nil? raw) (empty? raw))
      :empty

      (not (js/isNaN raw))
      (js/parseFloat raw)
      
      (and n (not (js/isNaN n)))
      (js/parseFloat n)
      
      :else :invalid)))

(defn error-messages 
  "Return a list of error messages"
  [field]
  (filter #(not (nil? %)) (vals (get field :errors))))

(defn valid?
  [field]
  (empty? (filter #(not (nil? %)) (vals (get field :errors)))))

(defn has-errors?
  [field]
  (not (valid? field)))

(defn error
  [field k msg]
  (update-in field [:errors] (fn [o] (assoc o k msg))))

(defn- update-it 
  "This is a convenience function to update the value, display and
  error fields inside a field map"
  [field new-value new-display error-key error-msg]
  (-> field
      (assoc-in [:value]   new-value)
      (assoc-in [:display] new-display)
      (error error-key error-msg)))

;; Validations are functions that must return a function. The function
;; returned must accept a single 'field' argument which is a map
;; representing the current state of a form field. The function must
;; return a map representing the resulting state of the field.

(defn- type-fn [& [opts]] (get opts :type))

(defn valid []
  (fn [{:keys [raw] :as field}]
    (js/console.log "valid")
    (js/console.log (str field))
    (update-it field raw raw :valid nil)))

(defn percent [& [{:keys [msg] :as opts}]]
  (fn [{:keys [raw] :as field}]
    (js/console.log "percent")
    (js/console.log (str field))
    (let [n (parse-number raw)] 
      (case n 
        :invalid
        (update-it field 0 raw :percent 
                   (or msg "Please enter a valid percent"))
        :empty 
        (update-it field 0 raw :percent nil)
        (update-it field n raw :percent  nil)))))

(defn number [& [{:keys [msg] :as opts}]]
  (fn [{:keys [raw] :as field}]
    (js/console.log "number")
    (js/console.log (str field))
    (let [n (parse-number raw)] 
      (case n 
        :invalid
        (update-it field 0 raw :number 
                   (or msg "Please enter a valid number"))
        :empty 
        (update-it field 0 raw :number nil)
        (update-it field n raw :number nil)))))

(defn required [& [{:keys [msg] :as opts}]]
  (fn [{:keys [raw] :as field}] 
    (js/console.log "required default")
    (js/console.log (str field))
    (if (empty? raw)
      (error field :required (or msg "This field is required"))
      (error field :required nil))))

(defn positive [& [{:keys [msg min]
                    :or {min 1} :as opts}]]
  (fn [{:keys [raw] :as field}] 
    (js/console.log "greater-than")
    (js/console.log (str field))
    (let [r (parse-number raw)]
      (if (and r (< r min))
        (error field :positive (or msg (str "This field must be greater"
                                            " than or equal to " min)))
        (error field :positive nil)))))

(defn validate! [data path & validations]
  (fn [e]
    (let [raw (.-value (.-target e))
          validations (or validations [(valid)])
          start (assoc (get @data path) :raw raw)
          result ((apply comp validations) start)]
      (js/console.log "result")
      (js/console.log (str result))
      (swap! data assoc-in path result))))
