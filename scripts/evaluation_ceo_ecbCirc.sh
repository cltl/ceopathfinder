#!/usr/bin/env bash


#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
RESOURCES="$ROOT"/resources
LIB="$ROOT"/target

java -Xmx812m -cp "$LIB/ceopathfinder-v0.1-jar-with-dependencies.jar" MentionReader

python eval_script.py ../Tommaso-v5/connected_events ../Tommaso-v5/connected_events

