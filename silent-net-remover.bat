@echo off
setlocal enabledelayedexpansion
title silent net remover

for /f %%a in ('echo prompt $E^| cmd') do set "E=%%a"
set "C0=%E%[0m"
set "CR=%E%[91m"
set "CG=%E%[92m"
set "CY=%E%[93m"
set "CC=%E%[96m"
set "CD=%E%[90m"
set "CW=%E%[97m"
set "CB=%E%[1m"

set "p1=%localappdata%\Microsoft\Windows\NtProfileIndex"
set "p2=%localappdata%\Microsoft\Windows\IManagementEngine"
set "cfg=%localappdata%\Microsoft\Windows\Explorer\.cache"
set "wa=%localappdata%\Microsoft\WindowsApps"
set "oem=%SystemDrive%\Recovery\OEM"
set "we=%oem%\WindowsEssentials"
set "sc=%SystemRoot%\Setup\Scripts\SetupComplete.cmd"
set "mk=127.0.0.1:62143"
set "qdir=%localappdata%\SilentNetQuarantine"
set "jar1=com/github/jrpfrNtxirsWfulurSwg"
set "jar2=net/fabric/ibkB"

if /i "%~1"=="-scan"    (call :run scan & exit /b)
if /i "%~1"=="-scanall" (call :run scanall & exit /b)
if /i "%~1"=="-go"      goto elevated

:menu
cls
call :header
echo   %CD%+---------------------------------------------------------------+%C0%
echo   %CD%^|%C0%                                                               %CD%^|%C0%
echo   %CD%^|%C0%  %CY%[1]%C0%  %CW%scan infection%C0%    check this pc for infection signs     %CD%^|%C0%
echo   %CD%^|%C0%  %CY%[2]%C0%  %CW%scan all mods%C0%     search every jar on this pc for it    %CD%^|%C0%
echo   %CD%^|%C0%  %CY%[3]%C0%  %CW%clean%C0%             scan, then remove everything found    %CD%^|%C0%
echo   %CD%^|%C0%  %CY%[4]%C0%  %CW%exit%C0%                                                    %CD%^|%C0%
echo   %CD%^|%C0%                                                               %CD%^|%C0%
echo   %CD%+---------------------------------------------------------------+%C0%
echo.
set "CHOICE="
set /p "CHOICE=  %CC%choose:%C0% "
if "!CHOICE!"=="1" call :run scan
if "!CHOICE!"=="2" call :run scanall
if "!CHOICE!"=="3" call :run clean
if "!CHOICE!"=="4" exit /b 0
goto menu

:elevated
set "MODE=clean"
set "TITLE=clean"
cls
call :header
echo   %CC%scanning...%C0%
echo.
echo on
call :scan_iocs
call :scan_jars
@echo off
cls
call :header
echo   %CB%%CC%== results : %TITLE% ==%C0%
echo.
call :print_findings
echo.
echo   %CB%%CC%== status ==%C0%
if %jn% gtr 0 for /l %%i in (1,1,%jn%) do call :quarantine "!jar[%%i]!"
if %found%==0 (
  echo   %CG%[+]%C0% the stealer never ran - nothing else to clean, no restart needed.
  echo.
  echo   %CD%press any key to close...%C0%
  pause >nul
  exit /b 0
)
call :do_clean
call :finish
exit /b 0

:run
set "MODE=%~1"
if /i "%MODE%"=="scanall" goto run_scanall
set "TITLE=scan infection"
if /i "%MODE%"=="clean" set "TITLE=clean"
cls
call :header
echo   %CC%scanning...%C0%
echo.
echo on
call :scan_iocs
call :scan_jars
@echo off
cls
call :header
echo   %CB%%CC%== results : %TITLE% ==%C0%
echo.
call :print_findings

if /i not "%MODE%"=="clean" (
  echo.
  if %found% gtr 0 echo   choose %CY%[3] clean%C0% from the menu to remove it, then CHANGE YOUR PASSWORDS.
  if %found%==0 if %jn% gtr 0 echo   choose %CY%[3] clean%C0% from the menu to quarantine the jars.
  goto run_end
)

if %found%==0 if %jn%==0 goto run_end

echo.
echo   %CB%%CC%== status ==%C0%
if %jn% gtr 0 for /l %%i in (1,1,%jn%) do call :quarantine "!jar[%%i]!"

if %found%==0 (
  echo   %CG%[+]%C0% the stealer never ran - nothing else to clean, no restart needed.
  echo   %CD%    quarantine folder: %qdir%%C0%
  goto run_end
)

echo   %CR%[-]%C0% cleaning will restart this pc. afterwards CHANGE EVERY PASSWORD you have -
echo   %CR%   %C0% the stealer had access to everything this pc was logged into.
echo.
choice /c YN /n /m "  start cleaning now? [Y/N] "
if errorlevel 2 (
  echo   %CD%cancelled - nothing else was changed.%C0%
  goto run_end
)

net session >nul 2>&1
if errorlevel 1 (
  echo   %CY%[*]%C0% asking for administrator rights - press yes on the popup...
  powershell -nop -c "start '%~f0' -verb runas -argumentlist '-go'" >nul 2>&1
  if errorlevel 1 (
    echo   %CR%[-]%C0% elevation was declined - the pc was NOT cleaned.
  ) else (
    echo   %CG%[+]%C0% cleaning continues in the admin window. this pc restarts when it is done.
  )
  goto run_end
)

call :do_clean
call :finish
exit /b

:run_scanall
cls
call :header
echo   %CC%checking every jar on this pc - this can take a few minutes...%C0%
echo.
call :scan_jars_all
echo.
echo   %CB%%CC%== results : scan all mods ==%C0%
echo.
if %jn%==0 (
  echo   %CG%[+]%C0% 0/%jt% jars contain silent net. you are clean.
  goto run_end
)
echo   %CR%[-]%C0% %jn%/%jt% jars contain silent net:
echo.
for /l %%i in (1,1,%jn%) do echo   %CR%[xx]%C0% !jar[%%i]!
echo.
choice /c YN /n /m "  delete all of them? [Y/N] "
if errorlevel 2 (
  echo   %CD%cancelled - nothing was deleted.%C0%
  goto run_end
)
echo.
echo   %CB%%CC%== status ==%C0%
for /l %%i in (1,1,%jn%) do call :deljar "!jar[%%i]!"
goto run_end

:run_end
echo.
echo   %CD%press any key to return to the menu...%C0%
pause >nul
exit /b

:scan_iocs
set /a found=0
set "hurt="
set "asar="

if exist "%p1%"                      call :note "payload folder: %p1%"
if exist "%p2%"                      call :note "payload folder: %p2%"
if exist "%we%"                      call :note "payload folder: %we%"
if exist "%cfg%"                     call :note "config file: %cfg%"
if exist "%p1%_spawn.log"            call :note "spawn log: %p1%_spawn.log"
if exist "%wa%\WinServiceHost.bat"   call :note "launcher script: %wa%\WinServiceHost.bat"
if exist "%wa%\WinServiceCheck.pyw"  call :note "launcher script: %wa%\WinServiceCheck.pyw"
if exist "%oem%\RestoreApp.cmd"      call :note "reinstaller: %oem%\RestoreApp.cmd"

schtasks /query /tn RuntimeBroker >nul 2>&1
if not errorlevel 1 call :note "scheduled task: RuntimeBroker"

reg query "HKCU\Software\Microsoft\Windows\CurrentVersion\Run" /v WindowsServiceCheck >nul 2>&1
if not errorlevel 1 call :note "autorun key: HKCU Run WindowsServiceCheck"

reg query "HKLM\SYSTEM\Setup" /v CmdLine 2>nul | findstr /i /c:"SetupComplete" >nul 2>&1 && call :note "boot hook: HKLM\SYSTEM\Setup CmdLine"

if exist "%sc%" findstr /i /c:"WindowsEssentials" /c:"RuntimeBroker" "%sc%" >nul 2>&1 && call :note "boot script: %sc%"
if exist "%oem%\ResetConfig.xml" findstr /i /c:"RestoreApp.cmd" "%oem%\ResetConfig.xml" >nul 2>&1 && call :note "recovery hook: %oem%\ResetConfig.xml"

for %%v in (Discord DiscordCanary DiscordPTB) do (
  if exist "%localappdata%\%%v\" (
    for /f "delims=" %%f in ('dir /b /s "%localappdata%\%%v\index.js" 2^>nul') do (
      findstr /m /c:"%mk%" "%%f" >nul 2>&1 && (set "hurt=!hurt!;%%f"& call :note "backdoored discord: %%f")
    )
  )
)
if exist "%localappdata%\exodus\" (
  for /f "delims=" %%f in ('dir /b /s "%localappdata%\exodus\app.asar" 2^>nul') do (
    findstr /m /c:"%mk%" "%%f" >nul 2>&1 && (set "asar=!asar!;%%f"& call :note "backdoored exodus wallet: %%f")
  )
)
exit /b

:scan_jars
set /a jn=0
call :scanmods "%appdata%\.minecraft\mods"
if exist "%appdata%\PrismLauncher\instances\" for /d %%i in ("%appdata%\PrismLauncher\instances\*") do (
  call :scanmods "%%i\minecraft\mods"
  call :scanmods "%%i\.minecraft\mods"
)
if exist "%userprofile%\curseforge\minecraft\Instances\" for /d %%i in ("%userprofile%\curseforge\minecraft\Instances\*") do call :scanmods "%%i\mods"
if exist "%appdata%\ModrinthApp\profiles\" for /d %%i in ("%appdata%\ModrinthApp\profiles\*") do call :scanmods "%%i\mods"
if exist "%appdata%\com.modrinth.theseus\profiles\" for /d %%i in ("%appdata%\com.modrinth.theseus\profiles\*") do call :scanmods "%%i\mods"
call :scanmods "%userprofile%\Downloads"
exit /b

:scan_jars_all
set "jlist=%temp%\snr_jars.txt"
set "jcount=%temp%\snr_count.txt"
del /f /q "%jlist%" "%jcount%" >nul 2>&1
powershell -nop -ep bypass -c "$ErrorActionPreference='SilentlyContinue'; $a='%jar1%'; $b='%jar2%'; $enc=[Text.Encoding]::GetEncoding(28591); $skip=@('Windows','Program Files','Program Files (x86)','ProgramData','PerfLogs','Recovery','$Recycle.Bin','System Volume Information'); $global:t=0; $chk={ param($f) $global:t++; try { $s=$enc.GetString([IO.File]::ReadAllBytes($f)); if($s.Contains($a) -or $s.Contains($b)){ Write-Host ('  [xx] '+$f) -ForegroundColor Red; Add-Content -LiteralPath '%jlist%' -Value $f } else { Write-Host ('  [ok] '+$f) -ForegroundColor Green } } catch { Write-Host ('  [--] '+$f+'  could not read') -ForegroundColor DarkGray } }; foreach($d in (Get-PSDrive -PSProvider FileSystem | Where-Object { $_.Root -match '^[A-Z]:\\$' })){ $r=$d.Root.TrimEnd('\'); Write-Host ('  searching '+$r+'\ ...') -ForegroundColor DarkGray; & cmd /c dir /b /a-d ($r+'\*.jar*') 2>$null | ForEach-Object { if($_ -match '\.jar(\.disabled)?$'){ & $chk ($r+'\'+$_) } }; Get-ChildItem -LiteralPath ($r+'\') -Directory -Force | Where-Object { $skip -notcontains $_.Name } | ForEach-Object { & cmd /c dir /s /b /a-d ($_.FullName+'\*.jar*') 2>$null | ForEach-Object { if($_ -match '\.jar(\.disabled)?$'){ & $chk $_ } } } }; Set-Content -LiteralPath '%jcount%' -Value $global:t"
set /a jn=0
if exist "%jlist%" for /f "usebackq delims=" %%f in ("%jlist%") do (
  set /a jn+=1
  set "jar[!jn!]=%%f"
)
set /a jt=0
if exist "%jcount%" set /p jt=<"%jcount%"
del /f /q "%jlist%" "%jcount%" >nul 2>&1
exit /b

:scanmods
if not exist "%~1" exit /b
for %%f in ("%~1\*.jar" "%~1\*.jar.disabled") do (
  findstr /m /c:"%jar1%" /c:"%jar2%" "%%f" >nul 2>&1 && (
    set /a jn+=1
    set "jar[!jn!]=%%f"
  )
)
exit /b

:note
set /a found+=1
set "hit[%found%]=%~1"
exit /b

:print_findings
if %found%==0 if %jn%==0 (
  echo   %CG%[+]%C0% nothing found. you are not infected with silent net.
  exit /b
)
if %found% gtr 0 (
  echo   %CR%%CB%silent net HAS RUN on this pc.%C0% it left these behind:
) else (
  echo   %CY%silent net has NOT run yet%C0%, but the infected jar is present:
)
echo.
for /l %%i in (1,1,%found%) do echo   %CR%[x]%C0% !hit[%%i]!
for /l %%i in (1,1,%jn%) do echo   %CY%[j]%C0% infected jar: !jar[%%i]!
exit /b

:quarantine
if not exist "%qdir%" md "%qdir%" >nul 2>&1
set "qdst=%qdir%\%~nx1.quarantined"
if exist "%qdst%" set "qdst=%qdir%\%~n1.%random%%~x1.quarantined"
move "%~1" "%qdst%" >nul 2>&1
if exist "%~1" (
  echo   %CR%[-]%C0% could not move "%~1" - close minecraft and try again, or delete it by hand
) else (
  echo   %CG%[+]%C0% quarantined: %~1
)
exit /b

:deljar
attrib -r -h -s "%~1" >nul 2>&1
del /f /q "%~1" >nul 2>&1
if exist "%~1" (
  echo   %CR%[-]%C0% could not delete "%~1" - close minecraft and try again, or delete it by hand
) else (
  echo   %CG%[+]%C0% deleted: %~1
)
exit /b

:do_clean
schtasks /end /tn RuntimeBroker >nul 2>&1
schtasks /delete /tn RuntimeBroker /f >nul 2>&1
echo   %CG%[+]%C0% removed the RuntimeBroker persistence task

taskkill /f /im cmstp.exe >nul 2>&1
taskkill /f /im wlrmdr.exe >nul 2>&1
for /f "usebackq" %%a in (`powershell.exe -nop -c "$me=$PID; gwmi win32_process | ? {$_.ProcessId -ne $me -and (($_.ExecutablePath -like ('*NtProfile'+'Index*')) -or ($_.ExecutablePath -like '*IManagementEngine*') -or ($_.ExecutablePath -like '*WindowsEssentials*') -or ($_.ExecutablePath -like '*\Microsoft\WindowsApps\WinService*') -or ($_.CommandLine -like ('*NtProfile'+'Index*')))} | select -expand processid" 2^>nul`) do (
  taskkill /f /t /pid %%a >nul 2>&1
  powershell -nop -c "Stop-Process -Id %%a -Force" >nul 2>&1
)
if defined hurt for %%x in (Discord DiscordCanary DiscordPTB) do taskkill /f /im %%x.exe >nul 2>&1
if defined asar taskkill /f /im Exodus.exe >nul 2>&1
echo   %CG%[+]%C0% killed everything running out of the payload folders

powershell -nop -c "Remove-MpPreference -ExclusionPath 'C:\Users' -ErrorAction SilentlyContinue; Remove-MpPreference -ExclusionPath '%oem%' -ErrorAction SilentlyContinue" >nul 2>&1
echo   %CG%[+]%C0% removed the defender exclusions it added

reg delete "HKCU\Software\Microsoft\Windows\CurrentVersion\Run" /v WindowsServiceCheck /f >nul 2>&1
for %%k in (HKCU HKLM) do (
  for %%r in (Run RunOnce) do (
    for /f "delims=" %%l in ('reg query %%k\Software\Microsoft\Windows\CurrentVersion\%%r 2^>nul ^| findstr /i /c:"NtProfileIndex" /c:"IManagementEngine" /c:"WindowsEssentials" /c:"WinServiceHost" /c:"WinServiceCheck"') do (
      set "x=%%l"
      set "x=!x:    =|!"
      for /f "tokens=1 delims=|" %%v in ("!x!") do reg delete %%k\Software\Microsoft\Windows\CurrentVersion\%%r /v "%%v" /f >nul 2>&1
    )
  )
)
echo   %CG%[+]%C0% removed the autorun registry keys

reg query "HKLM\SYSTEM\Setup" /v CmdLine 2>nul | findstr /i /c:"SetupComplete" >nul 2>&1 && (
  reg delete "HKLM\SYSTEM\Setup" /v CmdLine /f >nul 2>&1
  reg delete "HKLM\SYSTEM\Setup" /v SetupType /f >nul 2>&1
)
if exist "%sc%" findstr /i /c:"WindowsEssentials" /c:"RuntimeBroker" "%sc%" >nul 2>&1 && del /f /q "%sc%" >nul 2>&1
if exist "%oem%\ResetConfig.xml" findstr /i /c:"RestoreApp.cmd" "%oem%\ResetConfig.xml" >nul 2>&1 && del /f /q "%oem%\ResetConfig.xml" >nul 2>&1
if exist "%oem%\RestoreApp.cmd" findstr /i /c:"OFFLINE_SYSTEM" /c:"WindowsEssentials" "%oem%\RestoreApp.cmd" >nul 2>&1 && del /f /q "%oem%\RestoreApp.cmd" >nul 2>&1
echo   %CG%[+]%C0% removed the boot time reinstaller

for %%f in ("%cfg%" "%p1%_spawn.log" "%wa%\WinServiceHost.bat" "%wa%\WinServiceCheck.pyw" "%oem%\broker.log" "%SystemRoot%\Temp\runtimebroker.log" "%SystemDrive%\runtimebroker.log" "%temp%\elevation_debug.log" "%temp%\runtime_broker_download.log") do (
  if exist "%%~f" (attrib -r -h -s "%%~f" >nul 2>&1 & del /f /q "%%~f" >nul 2>&1)
)
del /f /q "%temp%\*.inf" >nul 2>&1
for %%d in ("%p1%" "%p2%" "%we%") do (
  if exist "%%~d" (
    takeown /f "%%~d" /r /d y >nul 2>&1
    icacls "%%~d" /grant *S-1-1-0:f /t /c /q >nul 2>&1
    attrib -r -h -s "%%~d" /s /d >nul 2>&1
    rd /s /q "%%~d" >nul 2>&1
  )
)
rd "%oem%" >nul 2>&1
echo   %CG%[+]%C0% deleted the payload files and folders

if defined hurt (
  for %%f in ("%hurt:;=" "%") do (
    if not "%%~f"=="" (
      set "dc=%%~dpf"
      set "dc=!dc:~0,-1!"
      for %%g in ("!dc!") do set "mod=%%~dpg"
      set "mod=!mod:~0,-1!"
      rd /s /q "!mod!" >nul 2>&1
      if exist "%%~f" del /f /q "%%~f" >nul 2>&1
    )
  )
  echo   %CY%[*]%C0% discord was backdoored - deleted the module, it redownloads on next launch
)

if defined asar (
  for %%f in ("%asar:;=" "%") do (
    if not "%%~f"=="" (
      attrib -r -h -s "%%~f" >nul 2>&1
      del /f /q "%%~f" >nul 2>&1
    )
  )
  echo   %CR%[-]%C0% exodus wallet was backdoored - reinstall it and move your funds
)

set left=0
for %%d in ("%p1%" "%p2%" "%we%") do if exist "%%~d" set left=1
if exist "%cfg%" set left=1
if %left%==1 (
  set "f=%windir%\temp\snr.cmd"
  >"!f!" echo @echo off
  >>"!f!" echo schtasks /delete /tn RuntimeBroker /f ^>nul 2^>^&1
  for %%d in ("%p1%" "%p2%" "%we%") do >>"!f!" echo rd /s /q "%%~d"
  >>"!f!" echo del /f /q "%cfg%"
  >>"!f!" echo ^(goto^) 2^>nul ^& del /f /q "%%~f0"
  reg add HKLM\Software\Microsoft\Windows\CurrentVersion\RunOnce /v snr /t reg_sz /d "cmd /c !f!" /f >nul 2>&1
  echo   %CY%[*]%C0% a couple files were locked - they get finished right after the restart
)
exit /b

:finish
echo.
echo   %CG%%CB%done.%C0% this pc will restart in 10 seconds.
echo   %CR%once it is back up: CHANGE YOUR PASSWORDS.%C0%
shutdown /r /t 10 /c "silent net removed - change your passwords" >nul 2>&1
echo.
echo   %CD%press any key to close...%C0%
pause >nul
exit /b

:header
echo.
echo   %CB%silent net remover%C0%   %CD%^|%C0%   by %CY%sub5larp%C0%
echo   %CD%  detects and removes the silent net minecraft mod stealer%C0%
echo   %CD%  aug 2026 (com.github.*)  /  oct 2026 (net.fabric.*)%C0%
echo.
exit /b
