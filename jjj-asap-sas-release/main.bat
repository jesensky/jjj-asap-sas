rem 
rem IMPORTANT
rem
rem Do not run this script directly - run model.bat instead.
rem

set MY_JARS=lib\jjj.jar
set MY_JARS=%MY_JARS%;weka\weka.jar
set MY_JARS=%MY_JARS%;weka\naiveBayesTree\naiveBayesTree.jar
set MY_JARS=%MY_JARS%;weka\RBFNetwork\RBFNetwork.jar
set MY_JARS=%MY_JARS%;\lib\jgap-3.6.1\jgap.jar
set MY_JARS=%MY_JARS%;weka\partialLeastSquares\partialLeastSquares.jar

set PATH=%JAVA_HOME%\bin;%PATH%
set > .\logs\run.log

rem
rem Create work directories
rem

if not exist work\text md work\text
if not exist work\text\t md work\text\t
if not exist work\text\u md work\text\u

if not exist work\datasets md work\datasets
if not exist work\datasets\t md work\datasets\t
if not exist work\datasets\u md work\datasets\u

if not exist work\models1 md work\models1
if not exist work\models1\t md work\models1\t
if not exist work\models1\u md work\models1\u
if not exist work\models2 md work\models2
if not exist work\models2\t md work\models2\t
if not exist work\models2\u md work\models2\u
if not exist work\models3 md work\models3

rem
rem Preprocessing
rem

java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.text.job.Unmarshall
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.text.job.Jargon
IF ERRORLEVEL 1 GOTO FAIL

IF EXIST work\text\t\10-spell-checked.txt GOTO SKIPTRAINSPELLCHECK

java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\t\1-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\t\1-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\t\2-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\t\2-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\t\3-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\t\3-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\t\4-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\t\4-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\t\5-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\t\5-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\t\6-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\t\6-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\t\7-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\t\7-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\t\8-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\t\8-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\t\9-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\t\9-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\t\10-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\t\10-spell-checked.txt

:SKIPTRAINSPELLCHECK
IF EXIST work\text\u\10-spell-checked.txt GOTO SKIPTESTSPELLCHECK

java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\u\1-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\u\1-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\u\2-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\u\2-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\u\3-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\u\3-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\u\4-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\u\4-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\u\5-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\u\5-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\u\6-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\u\6-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\u\7-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\u\7-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\u\8-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\u\8-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\u\9-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\u\9-spell-checked.txt
java -classpath jaspell pt.tumba.spell.JaSpell --data-dir=jaspell\dict --jargon=jargon.txt --misspells=common-misspells.txt --lang=en -c work\text\u\10-raw.txt | gawk-3.1.6-1\bin\gawk.exe "{if(NR>1) print $0;}" > work\text\u\10-spell-checked.txt

:SKIPTESTSPELLCHECK

rem
rem Convert text to ARFF
rem

java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.datasets.job.Import spell-checked
IF ERRORLEVEL 1 GOTO FAIL

rem
rem Run parser
rem 

IF EXIST parser\t\parser.data GOTO SKIPTRAINPARSERDATA

cd parser\t
call preparse.bat
cd ..
cd ..

cd parser\t
call parse.bat
cd ..
cd ..

cd parser\t
call postparse.bat
cd ..
cd ..

:SKIPTRAINPARSERDATA
IF EXIST parser\u\parser.data GOTO SKIPTESTPARSERDATA

cd parser\u
call preparse.bat
cd ..
cd ..

cd parser\u
call parse.bat
cd ..
cd ..

cd parser\u
call postparse.bat
cd ..
cd ..

:SKIPTESTPARSERDATA

rem
rem Import Parser output
rem

java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.parser.job.ImportParserData
IF ERRORLEVEL 1 GOTO FAIL

rem
rem Create datasets
rem

java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.datasets.job.Alphabetic spell-checked bow
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.datasets.job.NGrams spell-checked bow
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.datasets.job.ForPLS spell-checked pls
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.datasets.job.ForRegression spell-checked regression
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.datasets.job.ForCosine spell-checked cosine
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.datasets.job.RGrams spell-checked rgrams
IF ERRORLEVEL 1 GOTO FAIL

rem
rem Build models
rem

java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.models1.job.BuildBasicModels bow bow
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.models1.job.BuildBasicModels2 bow2 bow2
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.models1.job.BuildBasicMetaCostModels bow2 cost
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.models1.job.BuildPLSModels pls pls
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.models1.job.BuildRegressionModels regression regression
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.models1.job.BuildCosineModels cosine cosine
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.models1.job.BuildRBFKernelModels bow smo
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.models1.job.RGramModels rgrams rgrams
IF ERRORLEVEL 1 GOTO FAIL

rem
rem Build ensemble
rem

java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.models2.job.BuildUnicornEnsemble unicorn unicorn bow bow2 smo cost pls regression cosine rgrams
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.models3.job.Assemble unicorn-naive-bayes unicorn-naive-bayes
IF ERRORLEVEL 1 GOTO FAIL
java -Xmx%MY_HEAP_SIZE% -Dmy.threads=%MY_THREADS% -classpath "%MY_JARS%" jjj.asap.sas.misc.job.PublishModel unicorn-naive-bayes.csv
IF ERRORLEVEL 1 GOTO FAIL

rem
rem Exit
rem 

echo ***** JOB SUCCESSFUL *****
GOTO EXIT
:FAIL
echo ***** JOB FAILED *****
:EXIT

