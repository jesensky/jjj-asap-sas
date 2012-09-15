rem
rem Post parsing
rem

FOR %%I IN (*.txt.sp) DO ..\..\gawk-3.1.6-1\bin\gawk.exe -vo=parser.data "{if(NR==1) print FILENAME >> o; print $0 >> o;}" %%I
