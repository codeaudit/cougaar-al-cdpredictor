
                          CPESociety Version 1.0alpha Release Notes

1. NEW FEATURES AND BUGS

-This version of the CPESociety includes:
   + Zone planning
   + Manuever planning
   + Supply planning

-The new CPE society configuration with a total of 14 agents (1 BDE, 3 BN, 9 CPY, 1 Supply) and a
WorldState agent.

This is an alpha release for functionality testing.

2. INSTALLATION

CPE has been tested with Cougaar 11.

First, unzip the CPESociety.zip file into the <CPE> directory, where <CPE> is an installation directory.
(In the sections that follow, we assume that Cougaar has been installed in the <CIP> directory.)

 1) Copy the cpe.jar file from <CPE>classes into the <CIP>/lib directory.  Note that this file also contains all the
Castellan class files.  CPE uses some of the library files from that project.

 2) It is preferable that the "-server" flag be set on the Java VM's command line arguments. Add this to the
setarguments.bat file in <CIP>\bin so that the server VM will be used. Also, add the -mx768m parameter to the
MYMEMORY environment variable.  For example:

set MYPROPERTIES=-server -Xbootclasspath/p:"%COUGAAR_INSTALL_PATH%\lib\javaiopatch.jar" -Dorg.cougaar.system.path="%COUGAAR3RDPARTY%" -Dorg.cougaar.install.path="%COUGAAR_INSTALL_PATH%" -Duser.timezone=GMT -Dorg.cougaar.core.agent.startTime="08/10/2005 00:05:00" -Dorg.cougaar.class.path=%COUGAAR_DEV_PATH% -Dorg.cougaar.workspace=%COUGAAR_WORKSPACE% -Dorg.cougaar.config.path="%COUGAAR_INSTALL_PATH%\configs\common\;%COUGAAR_INSTALL_PATH%\configs\glmtrans\;"

set MYMEMORY=-Xms256m -Xmx768m

 3) If you wish to run the nodes distributed across multiple nodes using the provided *.ini files, 
insure that the alpreg.ini file has the correct name server, i.e.
address=<ipaddress or host name>

 4) If you are installing and running this on multiple nodes, it is desirable that all of the system clocks
for each node are synchronized.


3. RUNNING THE SINGLE NODE SIMULATOR 

The simulator depicts centralized planning and execution for both manuever and resupply using the world 
model and is used for testing purposes.  It is executed by running the runtest.bat file in the /classes 
subdirectory.  Planning is currently CPU and memory intensive; a minimum of 1GB of physical 
memory is preferable.

4. RUNNING THE AGENTS

A society comprised of nine "companies" (CPY1-9), one "brigade'" (BDE1) and three
"battalion" (BN1,BN2,BN3) and one "Supply1" agent. are included here.  

The WorldState agent is special in thatit contains the reference world model against which 
execution is actually performed.  In addition, it should be privileged (e.g. low latency, infinite bandwidth
messaging should be simulated) since communication between all agents and their perception of the
world is instantaneous (within the capabilities of the agent).  In other words, the WorldState agent should
not constrain the behavior of other agents.  All execution is currently performed on the WorldState agent.


There are two configuration files of interesting, WorldStateConfig.xml and CPAgentConfig.xml.  WorldStateConfig.xml
sets up the overal parameters of the run.

4.1 Configuration Files

The new configuration files are found in the "<CPE>/test/newconfigs" directory.

By default, the following node setups are included for testing:
NodeWorld.ini
NodeBDE.ini  Contains both BDE and Supply1 agent.
NodeBN1.ini
NodeBN2.ini
NodeBN3.ini
NodeCPY19.ini  Contains all CPY units.  May be split up as needed.


The WorldState agent is configured from the WorldStateConfig.xml file.

<WorldStateConfig>
<settings>
  <BoardWidth value="36" />                 // Size of the board
  <BoardHeight value="25" />                // Height of the board
  <PenaltyHeight value="4" />               // Penalty marker
  <NumberOfUnits value="4" />               // Number of CP units.  Should be equal to the number of agents
  <ZoneGridSize value="2" />                // Size of the zone for planning
  <RecoveryHeight value="-1.5" />           // Distance for Supply units to travel.  Should be negative. 
  <NumberOfSupplyVehicles value="8" />      // Number of resupply vehicles
  <NumberOfSupplyUnits value="1" />         // Number of supply units.  Should be 1.
  <InitialNumberOfTargets value="3" />      // Starting configuration in terms of number of targets.  This is obsolete.
  <DeltaT value="5" />                      // Seconds per DeltaT.  Should be 5.
  <SimulationDuration value="36000000" />   // Total amount of time to run in ms
  </settings>
</WorldStateConfig>

Each agent type (BDE,BN,CPY) now uses a different XML configuration file.

An example CPYAgentConfig.xml value is

 <CPConfig>
  <actions>
  <CPYUpdateStatusNIU value="2000" />             // Number of instruction units it takes for the CPY agent to perform the UpdateStatus action
  <CPYUpdateManueverPlanNIU value="1500" />       // Number of instruction units it takes for the CPY agent to perform the UpdateManeuverPlan action
  <CPYUpdateTimerPeriod value="30000" />          // UpdateTimer period (in ms.)
  <SupplyProcessManuverPlanNIU value="2500" />    // Not used.
  </actions>
  </CPConfig>

An example BN configuration agent is show 

 <CPConfig>
  <actions>
  <BNReplanTimerPeriod value="60000" />           // ReplanTimer period (in ms)
  <BNUpdateStatusNIU value="500" />               // Number of instruction units 
  <BNPlanningDepth value="3" />                   // Planning depth of search
  <BNPlanningBreadth value="20" />                // Number of search nodes expanded per search level
  </actions>
  </CPConfig>

An example BDE file is shown

<CPConfig>
   <actions>
       <BDEReplanPeriod value="2" />   // How many Zone planning phases per replan (a zone planning phase is currently 12 deltaT units or 60 seconds)
       <BDEPlanningDepth value="7" />  // Planning depth for zones
       <BDEPlanningBreadth value="50" />  // Planning breadth for zones.
       <BDEPlanningDelay value="1" />  // How many zone planning phases to delay planning
       <BDEUpdateStatusNIU value="0" />
   </actions>
</CPConfig>

6. Appendix

Note: Instruction units (IUs) are abstract units.  The actual amount of time taken to execute is scaled by CPU.  Currently, 
1000 IU ~ 1000 ms on a 2.5 GHz P4.
