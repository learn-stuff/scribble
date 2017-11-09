(ns scribble.core
  (:require [reagent.core :as r]
            [clojure.string :as string]
            [luggage]))

(enable-console-print!)

(println luggage)

(defn xy [e]
  (let [rect (.getBoundingClientRect (.-target e))]
    [(- (.-clientX e) (.-left rect))
     (- (.-clientY e) (.-top rect))]))

(defn mouse-handlers [drawing]
  (let [pen-down? (r/atom false)
        start-path
        (fn start-path [e]
          (when (not= (.-buttons e) 0)
            (reset! pen-down? true)
            (let [[x y] (xy e)]
              (swap! drawing conj [x y x y]))))
        continue-path
        (fn continue-path [e]
          (when @pen-down?
            (let [[x y] (xy e)]
              (swap! drawing (fn [lines]
                               (let [last-line-idx (dec (count lines))]
                                 (update lines last-line-idx conj x y)))))))
        end-path
        (fn end-path [e]
          (when @pen-down?
            (continue-path e)
            (reset! pen-down? false)))]
    {:on-mouse-down start-path
     :on-mouse-over start-path
     :on-mouse-move continue-path
     :on-mouse-up end-path
     :on-mouse-out end-path}))

(defonce app-state (r/atom []))

(defn paths [drawing]
  (into
   [:g
    {:style {:pointer-events "none"}
     :fill "none"
     :stroke "black"
     :stroke-width 4}]
   (for [[x y & more-points] @drawing]
     [:path {:d (str "M " x " " y "L "(string/join " " more-points))}])))

(defn scribble [attrs drawing]
  [:svg
   (merge-with merge attrs
               {:width "100%"
                :height 400
                :style {:border "1px solid"
                        :box-sizing "border-box"
                        :cursor "crosshair"}})
   [paths drawing]])

(defn app []
  [:div
   [scribble (mouse-handlers app-state) app-state]
   [:h3 "Scribble on me!"]])

(r/render-component [app]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
