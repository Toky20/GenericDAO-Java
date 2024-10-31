@ECHO OFF

REM Définir les variables (modifier les valeurs entre guillemets)

SET APP_NAME=DAOMDG
SET APP_DIR=%~dp0

SET SRC_MAIN_DIR=%APP_DIR%src
SET SRC_TEST_DIR=%APP_DIR%src

SET LIB_DIR=%APP_DIR%lib
SET TEMP_DIR=%APP_DIR%temp

REM Créer le répertoire d'application dans Tomcat (vérifier si existant)
IF EXIST "%APP_DIR%\temp" (
	ECHO Le répertoire d'application existe déjà. On le supprime.
	RD /S /Q "%APP_DIR%\temp"
)
MKDIR "%APP_DIR%\temp"
MKDIR "%APP_DIR%temp\classes"
copy /y "%APP_DIR%manifest.mf" "%APP_DIR%temp\classes"
MKDIR "%APP_DIR%temp\java_files"

rem Itérer sur chaque fichier .java dans le répertoire source et ses sous-dossiers
for /R %SRC_MAIN_DIR% %%a in (*.java) do (
  rem echo 
  COPY /Y "%%a" "%APP_DIR%temp\java_files"
)

REM Compiler les classes Java
REM -cp %LIB_DIR%\*
javac -cp %LIB_DIR%\* -d %APP_DIR%temp\classes %APP_DIR%temp\java_files\*.java 

cd "%APP_DIR%temp\classes"

jar cmf manifest.mf DAOMDG.jar .

copy /y DAOMDG.jar "%APP_DIR%"

cd "%APP_DIR%"

RD /S /Q "%APP_DIR%\temp"



