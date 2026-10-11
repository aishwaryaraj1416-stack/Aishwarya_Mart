#!/bin/sh
set -eu

# Railway provides PORT dynamically; local/container runs default to 8080.
PORT="${PORT:-8080}"
sed -i "s/port=\"8080\"/port=\"$PORT\"/" /usr/local/tomcat/conf/server.xml

# Keep the H2 database on Railway's mounted persistent volume (/data).
# These defaults can be overridden with Railway service variables.
DB_URL="${DB_URL:-jdbc:h2:file:/data/aishwarya_mart_db;MODE=MySQL;AUTO_SERVER=TRUE}"
DB_USERNAME="${DB_USERNAME:-sa}"
DB_PASSWORD="${DB_PASSWORD:-}"
export DB_URL DB_USERNAME DB_PASSWORD

exec catalina.sh run
