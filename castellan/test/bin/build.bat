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
set LIBPATHS=%LIBPATHS%;%COUGAARPATH%/sys/grappa1_2_bbn.jar
set LIBPATHS=%LIBPATHS%;%COUGAAR_INSTALL_PATH%/sys/mm-mysql-2.jar

set SOURCEFILES=%CASTELLANSRC%/org/hydra/server/*.java %CASTELLANSRC%/org/hydra/server/ui/*.java %CASTELLANSRC%/org/hydra/metrics/*.java 
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/hydra/pdu/*.java %CASTELLANSRC%/org/hydra/libui/*.java %CASTELLANSRC%/org/hydra/util/*.java
set SOURCEFILES=%SOURCEFILES% %CASTELLANSRC%/org/hydra/planlog/*.java %CASTELLANSRC%/org/dbinterface/*.java %CASTELLANSRC%/org/hydra/plugin/*.java %CASTELLANSRC%/org/hydra/pspace/search/*.java

javac -classpath %LIBPATHS% -d %CASTELLANHOME% %SOURCEFILES%
mkdir %CASTELLANHOME%\org\dbinterface\defs
copy %CASTELLANROOT%\data\defs\*.xml %CASTELLANHOME%\org\dbinterface\defs
jar cvf %COUGAARPATH%\lib\castellan.jar %CASTELLANHOME%\org