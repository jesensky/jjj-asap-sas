rem 
rem Usage: .\build.bat
rem
rem IMPORTANT:
rem 
rem Assumes that the Current Working Directory is the same 
rem as the directory containing build.xml.
rem
rem IMPORTANT:
rem 
rem Edit environment variables below as needed
rem

set ANT_HOME=c:\apache-ant-1.8.1
set JAVA_HOME=c:\Program Files\Java\jdk1.7.0_04

rem
rem IMPORANT:
rem
rem Normally you do not need to edit below this line
rem

set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%PATH%
set > .\logs\build.log
ant -v -l .\logs\ant.log -buildfile .\build.xml
