
setLocal EnableDelayedExpansion 

cd C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\LIR1

for /f "tokens=* delims= " %%a in ('dir/b') do ( 
set v=%%a
cd C:\temp\microLir\microLir\
microlir.bat "C:\Users\Amir\workspace\IC_COMPILER\test\UnitTests\LIR1\%%a" >> C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\OUT1\%%a.out
cd C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\LIR1
)

cd C:\Users\Amir\WORKSP~1\ICC563~1\test\UnitTests\