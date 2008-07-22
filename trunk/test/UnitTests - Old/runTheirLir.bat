
setLocal EnableDelayedExpansion 

cd F:\workspace\IC_COMPILER\test\UnitTests\LIR1

for /f "tokens=* delims= " %%a in ('dir/b') do ( 
cd F:\workspace\microLIR
microlir.bat "F:\workspace\IC_COMPILER\test\UnitTests\LIR1\%%a" >> F:\workspace\IC_COMPILER\test\UnitTests\OUT1\%%a.out
cd F:\workspace\IC_COMPILER\test\UnitTests\LIR1
)

cd F:\workspace\IC_COMPILER\test\UnitTests\