set CASTELLANHOME=C:\castellan\bin
REM This is the root directory where the hydra branch is stored
set CASTELLANROOT=C:\cvslocal\hydra
set CASTELLANSRC=%CASTELLANROOT%\src
set COUGAARPATH=%COUGAAR_INSTALL_PATH%
set LIBPATHS=%COUGAARPATH%\lib\core.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/lib/build.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/xerces.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/lib/glm.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/jgl3.1.0.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/grappa1_2.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/matlib.jar
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/mm-mysql-2.jar

echo off
set SOURCEFILES=%CASTELLANSRC%/org/cougaar/tools/castellan/server/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/ldm/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/server/ui/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/server/plugin/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/analysis/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/pdu/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/util/libui/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/util/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/planlog/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/plugin/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/cougaar/tools/castellan/pspace/search/*.java
echo on
javac -classpath %LIBPATHS% -d %CASTELLANHOME% %SOURCEFILES%
REM mkdir %CASTELLANHOME%\org\dbinterface\defs
REM copy %CASTELLANROOT%\data\defs\*.xml %CASTELLANHOME%\org\dbinterface\defs
jar cvf %COUGAARPATH%\lib\castellan.jar -C %CASTELLANHOME% org