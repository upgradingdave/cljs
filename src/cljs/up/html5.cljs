(ns up.html5)

(defn file-api-supported? []
  (and js/File
       js/FileReader 
       js/FileList 
       js/Blob))

;; toBlob polyfill from 
;; https://developer.mozilla.org/en-US/docs/Web/API/HTMLCanvasElement/toBlob
;; I think the cljs compiler doesn't know about toBlob, so as of May
;; 2016, just use the polyfill
(defn to-blob [canvas callback type quality]
  ;;if (not (aget (.-prototype js/HTMLCanvasElement) "toBlob"))

  ;; If browser doesn't support canvas.toBlob, use this polyfill from
  ;; https://developer.mozilla.org/en-US/docs/Web/API/HTMLCanvasElement/toBlob
  (let [url     (.toDataURL canvas type quality)
        bin-str (js/atob (aget (.split url ",") 1))
        len     (.-length bin-str)
        arr     (js/Uint8Array. len)
        type    (or type "image/png")]
    (loop [i 0]
      (when (< i len)
        (aset arr i (.charCodeAt bin-str i))
        (recur (inc i))))
    (callback (js/Blob. #js [arr] #js {"type" type})))

  ;; (do 
  ;;   (js/console.log "toblobbing ...")
  ;;   ;; If browser supports canvas.toBlob, then just call that
  ;;   (.toBlob canvas callback type quality))
  )
