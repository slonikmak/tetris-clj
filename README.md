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

