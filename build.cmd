@echo off
rem Ajusta JAVA_HOME al JDK 21 requerido por el proyecto (Spring Boot 4 / Java 21)
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d C:\Users\Lucia Florentin\Documents\workspace-spring-tools-for-eclipse-5.3.0.RELEASE\SistemParcan
mvnw.cmd compile -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true
