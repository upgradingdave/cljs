#!/bin/bash

PREFIX=$1

rm -f resources/public/js/compiled/${PREFIX}.js
echo "compiling ${PREFIX}.js ... "; \
    lein cljsbuild once prod-${PREFIX}
rm -f resources/public/js/compiled/${PREVIX}-dev.js
echo "compiling ${PREFIX}-dev.js ... "; \
    lein cljsbuild once prod-${PREFIX}-devcards

