#!/bin/bash

PREFIX=$1
BLOG_HOME=/Users/dparoulek/code/upgradingdave

cp resources/public/js/compiled/${PREFIX}.js \
    ${BLOG_HOME}/resources/templates/js
cp resources/public/js/compiled/${PREFIX}-dev.js \
    ${BLOG_HOME}/resources/templates/js

