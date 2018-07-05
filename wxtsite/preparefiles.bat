@echo off
set FIXED=c:\fixed
set ANT_HOME=%FIXED%\ant18
set PATH=%PATH%;%ANT_HOME%\bin
ant -buildfile %1\files2wxt.xml -logfile %1\antreport.txt -Dsiteroot=%1%