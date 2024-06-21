(ns util)

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
