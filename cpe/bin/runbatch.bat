set CPE_CLASS_PATH=.;%COUGAAR_INSTALL_PATH%\lib\core.jar;%COUGAAR_INSTALL_PATH%\lib\glm.jar;%COUGAAR_INSTALL_PATH%\lib\planning.jar;%COUGAAR_INSTALL_PATH%\lib\util.jar;
java -server -cp %CPE_CLASS_PATH% -mx768m vgworld.unittests.BatchedVGSimulator C:\Software\DISGroup\Projects\Ultralog\Software\CPESociety\outputs\batchout.txt
pause