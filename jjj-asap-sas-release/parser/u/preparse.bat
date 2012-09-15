rem
rem Prepare training files for parsing
rem

type ..\..\work\text\u\1-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\u\2-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\u\3-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\u\4-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\u\5-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\u\6-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\u\7-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\u\8-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\u\9-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"
type ..\..\work\text\u\10-spell-checked.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -F"\t" "{print tolower($6) > $1 \".txt\"}"

dir /b *.txt | ..\..\gawk-3.1.6-1\bin\gawk.exe -f parse.gawk >> parse.bat

