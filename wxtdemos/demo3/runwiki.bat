@ECHO OFF
set ANT_HOME=C:\fixed\apache-ant
set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_06
set PATH=%PATH%;%ANT_HOME%\bin
@ECHO. Doing wiki download
ant -buildfile c:\obamatestbs\testsite\wikijob.xml
