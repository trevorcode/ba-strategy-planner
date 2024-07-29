(ns util
  (:require ["alpinejs" :as a :refer [Alpine]]))

(defn distance [[x1 y1] [x2 y2]]
  (js/Math.sqrt (+ (js/Math.pow (- x2 x1) 2)
                   (js/Math.pow (- y2 y1) 2))))

(defn string->html [html-string]
  (let [template (js/document.createElement "template")]
    (set! template.innerHTML html-string)
    (if (= 1 (count template.content.children))
      (first template.content.children)
      template.content.children)))


(defn intersects? [rect1 rect2]
  (not (or (> rect1.x (+ rect2.x rect2.width))
           (< (+ rect1.x rect1.width) rect2.x)
           (> rect1.y (+ rect2.y rect2.height))
           (< (+ rect1.y rect1.height) rect2.y))))

(defn get-internal-position [mouseX mouseY svg-refs]
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

(defn place-image-on-map [{:keys [x y imageUrl svg-refs color base-number ally] :as opts}]
  (let [group (js/document.createElementNS "http://www.w3.org/2000/svg" "g")
        unit-container (js/document.createElementNS "http://www.w3.org/2000/svg" "rect")
        unit-elem (js/document.createElementNS "http://www.w3.org/2000/svg" "image")]
    (unit-container.setAttribute :stroke color)
    (unit-container.setAttribute :stroke-width 4)
    (unit-container.setAttribute :fill "none")
    (unit-container.setAttribute :x (- x 25))
    (unit-container.setAttribute :width 50)
    (unit-container.setAttribute :height 50)
    (unit-container.setAttribute :y (- y 25))

    (when base-number
      (unit-elem.setAttribute :ally ally)
      (unit-elem.setAttribute :base base-number))

    (unit-elem.setAttribute :href imageUrl)
    (unit-elem.setAttribute :x (- x 25))
    (unit-elem.setAttribute :width 50)
    (unit-elem.setAttribute :height 50)
    (unit-elem.setAttribute :y (- y 25))

    (.appendChild group unit-container)
    (.appendChild group unit-elem)
    (.appendChild (:images svg-refs) group)))

(defn init-store [store-name init-map]
  (Alpine.store store-name init-map)
  (Alpine.store store-name))