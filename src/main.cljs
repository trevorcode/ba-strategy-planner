(ns main
  (:require
   ["alpinejs" :as a :refer [Alpine]]
   [util :as u]
   [colorpicker :as c]
   [unitpicker :as unit]
   [toolpicker :as tool]
   [hotkeys :as h]
   [components :as components]
   [toolpicker :as t]))

(set! js/window.Alpine Alpine)
(Alpine.start)

(def map-translation-matrix (atom {:x 0
                                   :y 0
                                   :zoom 1}))

(def svg-refs {:svg nil
               :container nil
               :g nil
               :images nil
               :paths nil})

(add-watch map-translation-matrix :watch-map-translation
           (fn [_ _ _ {:keys [x y zoom]}]
             (let [g (:g svg-refs)
                   matrix (str "matrix(" zoom
                               ", 0, 0, " zoom
                               ", " x ", "
                               y ")")]

               (set! g.style.transform matrix))))

(def initial-template
  #html [:div [:div {:class "container"}
               [:nav
                [:ul
                 [:li (tool/tool-btn {:tool "hand"
                                      :tooltip "Move Tool [Q]"
                                      :body (components/icon "move")})]
                 [:li (tool/tool-btn {:tool "pen"
                                      :tooltip "Pen [W]"
                                      :body (components/icon "edit-2")})]
                 [:li (tool/tool-btn {:tool "eraser"
                                      :tooltip "Eraser [E]"
                                      :body (components/icon "x-square")})]
                 unit/unit-picker-btn]
                [:div c/colorpicker]]
               [:main
                [:div {:id "svg-container"}
                 [:svg {:id "map" :class "map-viewer" :xmlns "http://www.w3.org/2000/svg"}
                  [:g {:id "map-content"}
                   [:g {:id "background"}
                    [:image {:href "assets/battlemap.png"}]]
                   [:g {:id "map-images"}]
                   [:g {:id "map-paths"}]]]]]]
         unit/unit-picker-el])


(defn start-drag [state container e]
  (let [g (.getCTM (:g svg-refs))
        gx g.e
        gy g.f]
    (aset state :isDragging true)
    (aset state :startX (- e.clientX gx))
    (aset state :startY (- e.clientY gy))
    (set! container.style.cursor "grabbing")))

(defn start-draw [state e primary?]
  (let [[x y] (u/get-internal-position e.clientX e.clientY svg-refs)
        newPath (js/document.createElementNS "http://www.w3.org/2000/svg" "path")
        color (if primary?
                c/color-store.primaryColor
                c/color-store.secondaryColor)]
    (aset state :isDrawing true)
    (aset state :currentPath newPath)
    (aset state :startX x)
    (aset state :startY y)
    (conj! state.pathPoints [x y])

    (newPath.setAttribute "d" (str "M " x " " y))
    (newPath.setAttribute "stroke" color)
    (newPath.setAttribute "stroke-width" "6")
    (when (= t/toolstore.penStyle :dotted)
      (newPath.setAttribute "stroke-dasharray" "30,15"))
    (newPath.setAttribute "fill" "none")

    (.appendChild (:paths svg-refs) newPath)))

(defn start-erase [state e]
  (let [[x y] (u/get-internal-position e.clientX e.clientY svg-refs)
        eraser-elem (js/document.createElementNS "http://www.w3.org/2000/svg" "rect")]
    (eraser-elem.setAttribute :x (- x 10))
    (eraser-elem.setAttribute :width 20)
    (eraser-elem.setAttribute :height 20)
    (eraser-elem.setAttribute :fill "gray")
    (eraser-elem.setAttribute :y (- y 10))
    (.appendChild (:g svg-refs) eraser-elem)
    (set! state.eraser eraser-elem)

    (aset state :isErasing true)))

(defn mouse-up [container state]
  (aset state :isDragging false)
  (aset state :isDrawing false)
  (aset state :isErasing false)
  (when (:eraser state)
    (.remove (:eraser state)))
  (set! container.style.cursor "default"))

(defn register-map []
  (let [container (:container svg-refs)
        state {:isDragging false
               :isDrawing false
               :isErasing false
               :eraser nil
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
       (let [tool tool/toolstore.selectedTool]
         (case tool
           :hand (start-drag state container e)
           :pen
           (case e.button
             1 (start-drag state container e)
             0 (start-draw state e true)
             2 (start-draw state e false))

           :eraser (case e.button
                     0 (start-erase state e)
                     1 (start-drag state container e)
                     2 (start-drag state container e))

           :unitplacer (case e.button
                         0 (unit/place-unit state svg-refs e true)
                         1 (start-drag state container e)
                         2 (unit/place-unit state svg-refs e false))))))

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
         (let [[x y] (u/get-internal-position e.clientX e.clientY svg-refs)
               d (state.currentPath.getAttribute "d")]
           (when (> (u/distance [x y] (last state.pathPoints)) 5)
             (conj! state.pathPoints [x y])
             (state.currentPath.setAttribute "d" (str d " L " x " " y)))))

       (when state.isErasing
         (let [[x y] (u/get-internal-position e.clientX e.clientY svg-refs)
               eraser state.eraser
               eraserBox (eraser.getBBox)
               paths (-> (:paths svg-refs) .-children)
               images (-> (:images svg-refs) .-children)
               elems (concat paths images)]
           (doseq [elem elems]
             (let [pathBox (elem.getBBox)]
               (when (u/intersects? eraserBox pathBox)
                 (elem.parentNode.removeChild elem))))

           (eraser.setAttribute :x (- x 10))
           (eraser.setAttribute :y (- y 10))))))

    (container.addEventListener
     "wheel"
     (fn [e]
       (e.preventDefault)
       (let [[x y] (u/get-internal-position e.clientX e.clientY svg-refs)
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

(defn init []
  (-> (js/document.querySelector "body")
      .-innerHTML
      (set! initial-template))

  (set! svg-refs {:container (js/document.querySelector "#svg-container")
                  :svg (js/document.querySelector "#map")
                  :g (js/document.querySelector "#map-content")
                  :images (js/document.querySelector "#map-images")
                  :paths (js/document.querySelector "#map-paths")})
  (register-map)
  (js/feather.replace))

(init)