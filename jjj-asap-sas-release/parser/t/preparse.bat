rem
rem Prepare training files for parsing
rem

type ..\..\work\text\t\1-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\t\2-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\t\3-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\t\4-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\t\5-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\t\6-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\t\7-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\t\8-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\t\9-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\t\10-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"

dir /b *.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -f parse.gawk >> parse.bat

