# Overview

This project is where I experiment building cljs widgets mostly with
reagent and bootstrap css. I usually write a post about lessons
learned building each of these widgets on my blog at
[http://www.upgradingdave.com/blog](http://www.upgradingdave.com/blog).

To run Devcards, use `lein figwheel`, then browse to
`http://localhost:3449/cards.html`

To build individual widgets, like the tree widget, for example, use
something like `lein cljsbuild once prod-tree`. See `project.clj` for
all the available widgets.
