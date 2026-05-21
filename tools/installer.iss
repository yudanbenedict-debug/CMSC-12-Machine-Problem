[Setup]
AppName=Island Escaper
AppVersion=1.0
DefaultDirName={autopf}\IslandEscaper
DefaultGroupName=Island Escaper
OutputDir=C:\Users\acer\OneDrive\Desktop\Persona3\Installer\Windows
OutputBaseFilename=IslandEscaper-Setup
Compression=lzma
SolidCompression=yes
SetupIconFile=C:\Users\acer\OneDrive\Desktop\Persona3\Island Escaper\tools\icon.ico
UninstallDisplayIcon={app}\icon.ico

[Files]
Source: "C:\Users\acer\OneDrive\Desktop\Persona3\Installer\Jar\IslandEscaper.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\Users\acer\OneDrive\Desktop\Persona3\Installer\Jar\assets\*"; DestDir: "{app}\assets"; Flags: ignoreversion recursesubdirs
Source: "C:\Users\acer\OneDrive\Desktop\Persona3\Island Escaper\tools\jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs
Source: "C:\Users\acer\OneDrive\Desktop\Persona3\Island Escaper\tools\icon.ico"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\Island Escaper"; Filename: "{app}\IslandEscaper.bat"; IconFilename: "{app}\icon.ico"
Name: "{commondesktop}\Island Escaper"; Filename: "{app}\IslandEscaper.bat"; IconFilename: "{app}\icon.ico"

[Run]
Filename: "{app}\IslandEscaper.bat"; Description: "Launch Island Escaper"; Flags: postinstall nowait

[Code]
procedure CreateLaunchScript();
var
  BatchFile: string;
  Lines: TArrayOfString;
begin
  BatchFile := ExpandConstant('{app}\IslandEscaper.bat');
  SetArrayLength(Lines, 2);
  Lines[0] := '@echo off';
  Lines[1] := 'start "" "%~dp0jre\bin\javaw.exe" -jar "%~dp0IslandEscaper.jar"';
  SaveStringsToFile(BatchFile, Lines, False);
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then
    CreateLaunchScript();
end;