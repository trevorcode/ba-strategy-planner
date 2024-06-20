(ns main
  (:require [util :as u]
            [colorpicker :as c]))

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

               (set! g.style.transform matrix))))

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
         [:nav
          [:ul
           [:li [:button {:tool "hand"} [:i {:data-feather "move"}]]]
           [:li [:button {:tool "pen"} [:i {:data-feather "edit-2"}]]]
           [:li [:button {:tool "eraser"} [:i {:data-feather "x-square"}]]]]
          [:span "Color Picker"]
          [:ul
           [:li [:button {:color-selector :primary}]]
           [:li [:button {:color-selector :secondary}]]
           c/color-picker]]
         [:main
          [:div {:id "svg-container"}
           [:svg {:id "map" :class "map-viewer" :xmlns "http://www.w3.org/2000/svg"}
            [:g {:id "map-content"}
             [:image {:href "assets/battlemap.png"}]]]]]])

(defn start-drag [state container e]
  (let [g (.getCTM (:g svg-refs))
        gx g.e
        gy g.f]
    (aset state :isDragging true)
    (aset state :startX (- e.clientX gx))
    (aset state :startY (- e.clientY gy))
    (set! container.style.cursor "grabbing")))

(defn get-internal-position [mouseX mouseY]
  (let [svg (:svg svg-refs)
        g (:g svg-refs)
        svgRect (.getBoundingClientRect (:svg svg-refs))
        svgX (- mouseX svgRect.left)
        svgY (- mouseY svgRect.top)
        pt (.createSVGPoint svg)
        _ (set! pt.x svgX)
        _ (set! pt.y svgY)
        internalPt (pt.matrixTransform (.inverse (g.getCTM)))]
    [internalPt.x internalPt.y]))

(defn start-draw [state e primary?]
  (let [[x y] (get-internal-position e.clientX e.clientY)
        newPath (js/document.createElementNS "http://www.w3.org/2000/svg" "path")
        color (if primary?
                (:primary-color @c/color-state)
                (:secondary-color @c/color-state))]
    (aset state :isDrawing true)
    (aset state :currentPath newPath)
    (aset state :startX x)
    (aset state :startY y)
    (conj! state.pathPoints [x y])

    (newPath.setAttribute "d" (str "M " x " " y))
    (newPath.setAttribute "stroke" color)
    (newPath.setAttribute "stroke-width" "6")
    (newPath.setAttribute "fill" "none")

    (.appendChild (:g svg-refs) newPath)))

(defn start-erase [state e]
  (let [[x y] (get-internal-position e.clientX e.clientY)
        new-eraser (js/document.createElementNS "http://www.w3.org/2000/svg"
                                                #html [:rect {:id "eraser"
                                                              :x x
                                                              :y y
                                                              :width 20
                                                              :height 20
                                                              :fill "gray"}])]

    (.appendChild (:g svg-refs) new-eraser)

    (aset state :isErasing true)))

(defn mouse-up [container state]
  (aset state :isDragging false)
  (aset state :isDrawing false)
  (aset state :isErasing false)
  (set! container.style.cursor "default"))

(defn register-map []
  (let [container (:container svg-refs)
        state {:isDragging false
               :isDrawing false
               :isErasing false
               :pathPoints []
               :currentPath nil
               :startX nil
               :startY nil}]

    (container.addEventListener
     "contextmenu"
     (fn [e]
       (e.preventDefault)))

    (container.addEventListener
     "mousedown"
     (fn [e]
       (e.preventDefault)

       (let [tool @selected-tool]
         (case tool
           :hand (start-drag state container e)
           :pen
           (case e.button
             1 (start-drag state container e)
             0 (start-draw state e true)
             2 (start-draw state e false))

           :eraser (start-erase state e)))))

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
         (let [[x y] (get-internal-position e.clientX e.clientY)
               d (state.currentPath.getAttribute "d")]
           (when (> (u/distance [x y] (last state.pathPoints)) 5)
             (conj! state.pathPoints [x y])
             (state.currentPath.setAttribute "d" (str d " L " x " " y)))))

       (when state.isErasing
         (let [[x y] (get-internal-position e.clientX e.clientY)
               paths (js/document.querySelectorAll "paths")]))))

    (container.addEventListener
     "wheel"
     (fn [e]
       (e.preventDefault)
       (let [[x y] (get-internal-position e.clientX e.clientY)
             matrix (.getCTM (:g svg-refs))
             new-matrix (-> matrix
                            (.translate x y)
                            (.scale (- 1 (* 0.001 e.deltaY)))
                            (.translate (- 1 x) (- 1 y)))]
         (swap! map-translation-matrix
                (fn [m]
                  (-> m
                      (assoc :zoom new-matrix.a)
                      (assoc :x new-matrix.e)
                      (assoc :y new-matrix.f)))))))

    (container.addEventListener
     "mouseup"
     (fn [e]
       (mouse-up container state)))

    (container.addEventListener
     "mouseleave"
     (fn [e]
       (mouse-up container state)))))

(defn register-buttons []
  (doseq [btn (js/document.querySelectorAll "nav button[tool]")]
    (btn.addEventListener
     "click"
     (fn [] (select-tool (btn.getAttribute :tool)))))



  (reset! selected-tool @selected-tool))

(defn init []
  (-> (js/document.querySelector "body")
      .-innerHTML
      (set! initial-template))

  (set! svg-refs {:container (js/document.querySelector "#svg-container")
                  :svg (js/document.querySelector "#map")
                  :g (js/document.querySelector "#map-content")})

  (register-buttons)
  (c/initialize-color-picker)
  (register-map)

  (js/feather.replace))

(init)


