
setLocal EnableDelayedExpansion 

cd F:\workspace\IC_COMPILER\test\UnitTests\IC

for /f "tokens=* delims= " %%a in ('dir/b') do ( 
move %%a ..\LIR2\%%a
cd F:\workspace\IC_COMPILER\classes
java -cp .;java-cup-v11a.jar;java-cup-v11a-runtime.jar IC.Compiler F:\workspace\IC_COMPILER\test\UnitTests\LIR2\%%a -LF:\workspace\IC_COMPILER\test\libic.sig -print-lir
cd F:\workspace\IC_COMPILER\test\UnitTests\IC
move ..\LIR2\%%a %%a
)

cd F:\workspace\IC_COMPILER\test\UnitTests\LIR2

for /f "tokens=* delims= " %%a in ('dir/b') do ( 
cd F:\workspace\microLIR
del F:\workspace\IC_COMPILER\test\UnitTests\OUT2\%%a.out
microlir.bat "F:\workspace\IC_COMPILER\test\UnitTests\LIR2\%%a" >> F:\workspace\IC_COMPILER\test\UnitTests\OUT2\%%a.out
cd F:\workspace\IC_COMPILER\test\UnitTests\LIR2
)

cd F:\workspace\IC_COMPILER\test\UnitTests\