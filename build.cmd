setlocal
if exist env.cmd call env
set pjname=
if not "%1" == "" set pjname=%~n1
if "" == "%task%" set task=jar
if "" == "%pjname%" (
call gradlew %task%
) else (
call gradlew :%pjname%:%task%
)
