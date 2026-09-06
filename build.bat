@echo off
set JAVA_HOME=C:\PROGRA~1\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d C:\Users\Lucia Florentin\Documents\workspace-spring-tools-for-eclipse-5.3.0.RELEASE\SistemParcan
mvnw.cmd compile -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true
