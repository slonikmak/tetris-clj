# UI Development with Clojure and ClojureScript

This is an educational project exploring different approaches to UI development in Clojure and ClojureScript, with and without global state.

Inspired by a lecture from a Clojure course, I decided to experiment with various UI design strategies—both stateful and stateless—while also exploring different UI frameworks.

## Clojure Implementation (cljfx - JavaFX)

- Uses the `cljfx` library (based on JavaFX).
- Located in `src/cljfx_state` (with global state) and `src/cljfx_stateless` (without global state).
- **How to run:**
    - Open the respective `core.clj` file in each folder and run it in a REPL.

## ClojureScript Implementation

- Located in `src/cljs_simple` (pure JavaScript, no global state) and `src/cljs_rum` (with global state using the `RUM` library).
- **How to run:**
    - Use Leiningen to build and watch for changes:
      ```sh
      lein cljsbuild auto cljs_rum  # or cljs_simple
      ```
    - Open the corresponding HTML file from `resources/public` in a browser.

This project serves as a practical guide for understanding different state management paradigms in UI development with Clojure and ClojureScript.

## The `rum_canvas` is rum implementation of a simple canvas drawing app from article:
- [Canvas with React.js](https://medium.com/@pdx.lucasm/canvas-with-react-js-32e133c05258)

## The `scittle` is a simple game implementation using [scittle](https://medium.com/@pdx.lucasm/canvas-with-react-js-32e133c05258) and reagent

- Located in `docs`
- **How to run:**
    - Use babshka to run server in the `docs` folder:
      ```sh
      bb dev
      ```
    - Open the browser and go to the link provided by the server


# How to play

- arrows to move
- shift to rotate