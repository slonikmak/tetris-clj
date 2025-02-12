(ns rum-canvas.core
  (:require [rum.core :as rum]))

(defonce pos (atom 0))

(rum/defc canvas-component []
  (let [canvas-ref (rum/use-ref nil)]
    (rum/use-effect!
      (fn []
        (let [canvas (rum/deref canvas-ref)]
          (when canvas
            (let [ctx (.getContext canvas "2d")
                  width (.-width canvas)
                  height (.-height canvas)
                  pos (atom 0)
                  anim-id (atom nil)
                  render (fn render []
                           ;; Обновляем позицию, циклически
                           (swap! pos (fn [p] (mod (+ p 2) width)))
                           (let [p @pos]
                             ;; Очищаем canvas, заполняем белым
                             (set! (.-fillStyle ctx) "#ffffff")
                             (.fillRect ctx 0 0 width height)
                             ;; Рисуем красный прямоугольник
                             (set! (.-fillStyle ctx) "#ff0000")
                             (.fillRect ctx p 50 50 50))
                           ;; Планируем следующий кадр анимации
                           (reset! anim-id (js/requestAnimationFrame render)))]
              (render)
              ;; Функция очистки эффекта при размонтировании компонента
              (fn []
                (js/cancelAnimationFrame @anim-id))))))
      ;; Зависимостей нет — эффект выполнится только при монтировании
      [])
    [:canvas {:ref canvas-ref :width 800 :height 600}]))

(defn mount []
  ;; Предполагается, что в HTML есть элемент с id "app"
  (rum/mount (canvas-component) (.getElementById js/document "app")))

(defn init []
  (mount))

(set! (.-onload js/window) init)