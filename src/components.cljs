(ns components)

(defn option-expander [{:keys [id x-on:click]}]
  #html [:button {:class "expand-btn ba-button"
                  :id id
                  :x-on:click x-on:click} ">"])

(defn icon [icon]
  #html [:i {:data-feather icon}])