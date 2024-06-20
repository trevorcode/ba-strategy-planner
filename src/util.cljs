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


