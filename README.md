# ceopathfinder
Finds a path of circumstantial relations between events on the basis of the CircumstantialEventOntology

Assume Java and Maven 3.3.* installed

Go to the directoty of the installation using the command line.

install with maven 3.3.*

After installing go to the script folder and adapt the rights to run the ceopath.sh

This script prints the hiearchy, prints the pre, post and during state maps with the event classes and it shows how to call it to find the path between two event classes.

Functions:

1. print the class hiearchy

java -Xmx812m -cp "$LIB/ceopathfinder-v0.1-jar-with-dependencies.jar" CeoPathFinder --ont-file "$RESOURCES/CEO_ESO.owl" --printTree > tree.txt

2. print the pre, during and post condition states with the classes that have these conditions

java -Xmx812m -cp "$LIB/ceopathfinder-v0.1-jar-with-dependencies.jar" CeoPathFinder --ont-file "$RESOURCES/CEO_ESO.owl" --printMap > map.txt

3. find the circumstantial relation between two event classes

java -Xmx812m -cp "$LIB/ceopathfinder-v0.1-jar-with-dependencies.jar" CeoPathFinder --ont-file "$RESOURCES/CEO_ESO.owl" --match 0 --e1 Fire --e2 ExtinguishingFire

4. generate all possible inference chains starting from post-situations and map these to during and pre-situations

 java -Xmx812m -cp "$LIB/ceopathfinder-v0.1-jar-with-dependencies.jar" CeoPathFinder --ont-file "$RESOURCES/CEO_ESO.owl" --printChain > chain.txt


Some parameters:

--deep inherits constraints through the type hierarchy to all children
--during also during conditions are considered as post conditions of the firs event and pre conditions of the second event
--match [0,1] value 0 means direct match, value 1 looks for 1 connecting event for two events that are compared