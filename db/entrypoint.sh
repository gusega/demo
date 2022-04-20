#!/bin/bash -eu

trap catch SIGTERM

catch () {
  neo4j status;
  wait "$(<run/neo4j.pid)"
  neo4j status;
}

# shellcheck disable=SC2034 #used by cypher-shell
IFS="/" read -r user pass <<<"$NEO4J_AUTH"

CMD="$1"

if [[ "$CMD" == "neo4j" ]]; then

  /startup/docker-entrypoint.sh "$1" &

  until cypher-shell -u "$user" -p "$pass" <<< ":exit"; do
    >&2 echo "Neo4j is unavailable - sleeping"
    sleep 3
  done

  >&2 echo "Neo4j is up - can import files"

  if [[ ! -f /data/import_finished ]]; then
    cypher-shell -u "$user" -p "$pass" -f "scripts/import.cypher"
    touch /data/import_finished
    echo "import finished"
  fi;

  wait
else
  "$@"
fi;


