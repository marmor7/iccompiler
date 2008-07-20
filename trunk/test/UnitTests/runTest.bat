
setLocal EnableDelayedExpansion 

cd C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\IC

for /f "tokens=* delims= " %%a in ('dir/b') do ( 
set v=%%a 
move %%a ..\LIR2\%%a
cd C:\Users\Amir\WORKSP~1\ICC563~1\classes
java -cp .;java-cup-v11a.jar;java-cup-v11a-runtime.jar IC.Compiler C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\LIR2\%%a -LC:\Users\Amir\workspace\IC_COMPILER\test\libic.sig
cd C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\IC
move ..\LIR2\%%a %%a
)

cd C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\LIR2

for /f "tokens=* delims= " %%a in ('dir/b') do ( 
set v=%%a
cd C:\temp\microLir\microLir\
del C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\OUT2\%%a.out
microlir.bat "C:\Users\Amir\workspace\IC_COMPILER\test\UnitTests\LIR2\%%a" >> C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\OUT2\%%a.out
cd C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\LIR2
)

cd C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\