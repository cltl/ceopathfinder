#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT="$( cd $DIR && cd .. && pwd)"
RESOURCES="$ROOT"/resources
LIB="$ROOT"/target

java -Xmx812m -cp "$LIB/ceopathfinder-v0.1-jar-with-dependencies.jar" CeoPathFinder --ont-file "$RESOURCES/CEO_ESO.owl" --printTree > tree.txt

java -Xmx812m -cp "$LIB/ceopathfinder-v0.1-jar-with-dependencies.jar" CeoPathFinder --ont-file "$RESOURCES/CEO_ESO.owl" --printMap > map.txt

java -Xmx812m -cp "$LIB/ceopathfinder-v0.1-jar-with-dependencies.jar" CeoPathFinder --ont-file "$RESOURCES/CEO_ESO.owl" --printChain > chain.txt

java -Xmx812m -cp "$LIB/ceopathfinder-v0.1-jar-with-dependencies.jar" CeoPathFinder --ont-file "$RESOURCES/CEO_ESO.owl" --deep --match 0 --e1 Fire --e2 Surgery >  two.events.txt

# special parameters can be used in combination with the function for comparing two events:
# --deep pre, during and post conditions are inherited along the type hierarchy from top to bottom
# --match [0,1] 0 means only direct matches, 1 means only when there is one intermediate class
# --during also during conditions are considered as pre and post