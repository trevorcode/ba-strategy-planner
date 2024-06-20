(ns main
  (:require))

(def selected-tool (atom :hand))
(def map-translation-matrix (atom {:x 0
                                   :y 0
                                   :zoom 1}))

(def svg-refs {:svg nil
               :container nil
               :g nil})

(add-watch map-translation-matrix :watch-map-translation
           (fn [_ _ _ {:keys [x y zoom]}]
             (let [g (:g svg-refs)

                   matrix (str "matrix(" zoom
                               ", 0, 0, " zoom
                               ", " x ", "
                               y ")")]
               (set! g.style.transform matrix)
               #_(set! svg.style.transform (str "translate(" x "px," y "px)")))))

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
            [:g {:id "map-content"}
             [:circle {:cx "500" :cy "500" :r "50" :fill "red"}]]]]]])

(defn start-drag [state container e]
  (let [current-matrix @map-translation-matrix]
    (aset state :isDragging true)
    (aset state :startX (- e.clientX current-matrix.x))
    (aset state :startY (- e.clientY current-matrix.y))
    (set! container.style.cursor "grabbing")))

(defn start-draw [state e]
  (let [current-matrix @map-translation-matrix
        x (- e.clientX current-matrix.x)
        y (- e.clientY current-matrix.y)
        newPath (js/document.createElementNS "http://www.w3.org/2000/svg" "path")]
    (aset state :isDrawing true)
    (aset state :currentPath newPath)

    (newPath.setAttribute "d" (str "M " x " " y))
    (newPath.setAttribute "stroke" "white")
    (newPath.setAttribute "stroke-width" "2")
    (newPath.setAttribute "fill" "none")

    (.appendChild (:g svg-refs) newPath)))

(defn register-map []
  (let [container (:container svg-refs)
        state {:isDragging false
               :isDrawing false
               :currentPath nil
               :startX nil
               :startY nil}]

    (container.addEventListener
     "mousedown"
     (fn [e]
       (let [tool @selected-tool]
         (case tool
           :hand (start-drag state container e)
           :pen (start-draw state e)))))

    (container.addEventListener
     "mousemove"
     (fn [e]
       (when state.isDragging
         (let [x (- e.clientX state.startX)
               y (- e.clientY state.startY)]
           (swap! map-translation-matrix
                  (fn [m]
                    (-> m
                        (assoc :x x)
                        (assoc :y y))))))

       (when state.isDrawing
         (let [x (- e.clientX state.startX)
               y (- e.clientY state.startY)
               d (state.currentPath.getAttribute "d")]
           (state.currentPath.setAttribute "d" (str d " L " x " " y))
           
           ))))

    (container.addEventListener
     "wheel"
     (fn [e]
       (println e)
       (e.preventDefault)
       (swap! map-translation-matrix update :zoom (fn [z] (- z (* 0.001 e.deltaY))))))

    (container.addEventListener
     "mouseup"
     (fn [e]
       (aset state :isDragging false)
       (aset state :isDrawing false)
       (set! container.style.cursor "default")))

    (container.addEventListener
     "mouseleave"
     (fn [e]
       (aset state :isDragging false)
       (aset state :isDrawing false)
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

  (set! svg-refs {:container (js/document.querySelector "#svg-container")
                  :svg (js/document.querySelector "#map")
                  :g (js/document.querySelector "#map-content")})

  (register-buttons)
  (register-map))

(init)


