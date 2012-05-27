#!/bin/sh
lein marg -d ./resources/public/docs -f index.html \
	-c /docs/marginalia.css
