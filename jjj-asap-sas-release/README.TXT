
README.TXT
==========

Contest: The Hewlett Foundation: Short Answer Scoring
Team: JJJ
Contact: james.jesensky@gmail.com

DISCLAIMER:
===========

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

PREREQUSITES:
=============

1. Intel Based PC w/ Windows 7 64-bit, at least 8GB RAM.  (Multi-core processor recommended).

2. Oracle 64-bit JDK version 6 or version 7.

3. Apache Ant 1.8.x. (Optional - needed to build software from source).

INSTRUCTIONS:
=============

1. Create an empty directory c:\asap\sas

2. Unpack the zip file into c:\asap\sas

3. If you completed steps 1 and 2 correctly, then c:\asap\sas\release\model.bat should exist.

4. COPY THE FINAL TEST DATASET TO C:\ASAP\SAS\RELEASE\DATA\TEST.TSV

5. Install the Stanford Parser library into C:\stanford-parser-2012-05-22\. You can find it inside the zip file at
   c:\asap\sas\release\stanford-parser-2012-05-22.tgz. Or you can download from 
   http://nlp.stanford.edu/software/lex-parser.shtml.

   To be clear, c:\stanford-parser-2012-05-22\stanford-parser.jar should exist.

   If you must install it elsewhere, edit c:\asap\sas\release\parser\t\parse.gawk and
   c:\asap\sas\release\parser\u\parse.gawk to account for this.

6. Install the JGAP library into C:\lib\jgap-3.6.1. You can find it inside the zip file at C:\asap\sas\release\jgap-3.6.1.zip.
   Or download from http://jgap.sourceforge.net/.
   
   To be clear, c:\lib\jgap-3.6.1\jgap.jar should exist.
   
   If you must install it elsewhere, edit c:\asap\sas\release\main.bat and c:\asap\sas\release\build.xml
   to account for this.
   
7. Open the file c:\asap\sas\release\build.bat in a text editor. Change the values of the environment variables
   as needed. (This step can be skipped if not building from source).

8. Open the file c:\asap\sas\release\model.bat in a text editor. Change the values of the environment variables
   as needed. This was delveoped on a PC with 8GB RAM and a quad-core CPU with hyper-threading. If you have different
   specs., then you probably need to change MY_HEAP_SIZE and MY_THREADS to appropiate values.

9. [OPTIONAL] Optionally, you can rebuild the software from source as follows:

    a. Open Windows Command Prompt.
	b. Change the working directory to c:\asap\sas\release
	c. Enter command: .\build.bat
	d. Inspect c:\asap\sas\release\logs\ant.log for errors.
	
	Note that the zip file contains compiled class files so this step is optional.
	
10. You can now build the model as follows:

    a. Open Windows Command Prompt.
	b. Change the working directory to c:\asap\sas\release
	c. Enter command: .\model.bat
	d. Wait for command to finish. This can take hours or days depending on your setup.
    e. If everything worked, the output CSV file is c:\asap\sas\release\publish\unicorn-naive-bayes.csv.
	
TROUBLESHOOTING:
================

a. Look in c:\asap\sas\release\logs. Many log files are written in this directory during processing.

b. The software can generally be re-started after a crash--and it will pick up where it left off. This doesn't always
   work if it was in the middle of writting a file. So restart the batch job at your own risk if it crashes.

OHTER:
======

JaSpell was written by Bruno Martins and is covered by the BSD license (http://opensource.org/licenses/bsd-license.php).
