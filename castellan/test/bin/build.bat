REM This is the root directory where the hydra branch is stored
set CASTELLANROOT=C:\bin\castellan
set CASTELLANSRC=%CASTELLANROOT%\src
set TMPDIR=%CASTELLANROOT%\tmpdir
set COUGAARPATH=%COUGAAR_INSTALL_PATH%

set LIBPATHS=%COUGAARPATH%\lib\core.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/lib/bootstrap.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/lib/build.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/lib/util.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/xerces.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/lib/glm.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/jgl3.1.0.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/grappa1_2.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/matlib.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/mm-mysql-2.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/lib/aggagent.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/lib/planning.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/lib/albbn.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/servlet.jar

echo off
set SOURCEFILES=%CASTELLANSRC%/org/cougaar/tools/castellan/server/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/ldm/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/server/ui/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/server/ui/events/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/server/plugin/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/analysis/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/pdu/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/util/libui/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/util/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/planlog/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/plugin/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/pspace/search/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/tests/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/batching/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/alf/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/alf/sensor/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/alf/sensor/plugin/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/alf/sensor/rbfnn/*.java

echo on
javac -classpath %LIBPATHS% -d %TMPDIR% %SOURCEFILES%
REM copy %CASTELLANROOT%\data\defs\*.xml %TMPDIR%\org\dbinterface\defs
jar cf %COUGAARPATH%\lib\castellan.jar -C %TMPDIR% org