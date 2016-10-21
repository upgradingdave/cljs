(ns up.webworkers.simple)

(when (undefined? (.-document js/self))
  (set! (.-onmessage js/self) 
        (fn [e]
          (js/self.postMessage "Hi from cljs worker"))))

