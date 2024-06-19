(ns main
  (:require))

(def selected-tool (atom :hand))
(def map-translation-matrix (atom {:x 0
                                   :y 0
                                   :zoom 1}))

(add-watch map-translation-matrix :watch-map-translation
           (fn [_ _ _ {:keys [x y zoom]}]
             (let [svg (js/document.querySelector "#map")]
               (println x y zoom svg)
               (println (str "matrix(" zoom
                             ", 0, 0, " zoom
                             ", " x "px, "
                             y "px)"))
               (set! svg.style.transform (str "matrix(" zoom
                                              ", 0, 0, " zoom
                                              ", " x "px, "
                                              y "px)")))))

(add-watch selected-tool :watch-tool
           (fn [key atom old new]
             (let [btns (js/document.querySelectorAll "nav button")]
               (doseq [btn btns]
                 (if (= new (btn.getAttribute :tool))
                   (btn.classList.add "selected")
                   (btn.classList.remove "selected"))))))

(defn select-tool [tool]
  (reset! selected-tool tool))

(def initial-template
  #html [:div {:class "container"}
         [:nav [:ul
                [:li [:button {:tool "hand"} "Hand"]]
                [:li [:button {:tool "pen"} "Pen"]]
                [:li [:button {:tool "eraser"} "Eraser"]]]]
         [:main
          [:div {:id "svg-container"}
           [:svg {:id "map" :class "map-viewer" :xmlns "http://www.w3.org/2000/svg"}
            [:circle {:cx "500" :cy "500" :r "50" :fill "red"}]]]]])


(defn register-map []
  (let [svg (js/document.querySelector "#map")
        container (js/document.querySelector "#svg-container")
        state {:isDragging false
               :startX nil
               :startY nil}]

    (container.addEventListener
     "mousedown"
     (fn [e]
       (aset state :isDragging true)
       (aset state :startX (- e.clientX (.-left (svg.getBoundingClientRect))))
       (aset state :startY (- e.clientY (.-top (svg.getBoundingClientRect))))
       (set! container.style.cursor "grabbing")))

    (container.addEventListener
     "mousemove"
     (fn [e]
       (when state.isDragging
         (let [x (- e.clientX state.startX)
               y (- e.clientY state.startY)]
           (swap! map-translation-matrix (fn [m]
                                           (-> m
                                               (assoc :x x)
                                               (assoc :y y))))
           #_(set! svg.style.transform (str "translate(" x "px," y "px)"))))))


    (container.addEventListener
     "mouseup"
     (fn [e]
       (aset state :isDragging false)
       (set! container.style.cursor "default")))

    (container.addEventListener
     "mouseleave"
     (fn [e]
       (aset state :isDragging false)
       (set! container.style.cursor "default")))))

(defn register-buttons []
  (doseq [btn (js/document.querySelectorAll "nav button")]
    (btn.addEventListener
     "click"
     (fn [] (select-tool (btn.getAttribute :tool))))))

(defn init []
  (-> (js/document.querySelector "body")
      .-innerHTML
      (set! initial-template))

  (register-buttons)
  (register-map))

(init)


