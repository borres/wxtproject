set ANT_HOME=c:\fixed\ant18
REM set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_06
set PATH=%PATH%;%ANT_HOME%\bin
ant -buildfile c:\web\wxtsite\wxt\makezippeddists.xml
pause
