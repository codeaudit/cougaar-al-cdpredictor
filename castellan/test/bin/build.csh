#!/bin/tcsh -x
setenv CASTELLANHOME ../..
REM This is the root directory where the hydra branch is stored
setenv CASTELLANROOT ../../
setenv CASTELLANSRC $CASTELLANROOT/src
setenv COUGAARPATH %COUGAAR_INSTALL_PATH%
setenv LIBPATHS $COUGAAR_INSTALL_PATH/lib/core.jar
setenv LIBPATHS ${LIBPATHS}:$COUGAAR_INSTALL_PATH/lib/build.jar
setenv LIBPATHS ${LIBPATHS}:$COUGAAR_INSTALL_PATH/sys/xerces.jar
setenv LIBPATHS ${LIBPATHS}:$COUGAAR_INSTALL_PATH/lib/glm.jar
setenv LIBPATHS ${LIBPATHS}:$CASTELLANHOME/data/libs/jgl3.1.0.jar
setenv LIBPATHS ${LIBPATHS}:$COUGAAR_INSTALL_PATH/sys/grappa1_2_bbn.jar
setenv LIBPATHS ${LIBPATHS}:$COUGAAR_INSTALL_PATH/sys/mm-mysql-2.jar

#setenv SOURCEFILES=$CASTELLANSRC/org/hydra/server/*.java $CASTELLANSRC/org/hydra/server/ui/*.java $CASTELLANSRC/org/hydra/metrics/*.java 
#setenv SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/hydra/pdu/*.java %CASTELLANSRC%/org/hydra/libui/*.java %CASTELLANSRC%/org/hydra/util/*.java
#set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/hydra/planlog/*.java %CASTELLANSRC%/org/dbinterface/*.java %CASTELLANSRC%/org/hydra/plugin/*.java %CASTELLANSRC%/org/hydra/pspace/search/*.java

javac -classpath $LIBPATHS -d $CASTELLANHOME `find $CASTELLANSRC -name \*.java`
mkdir $CASTELLANHOME/org/dbinterface/defs
cp $CASTELLANROOT/data/defs/*.xml $CASTELLANHOME/org/dbinterface/defs
(cd $CASTELLANHOME; jar cvf castellan.jar org)
