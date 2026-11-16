#define AppName "TRAP CLIENT"
#define AppVersion "1.0.0"
#define AppPublisher "TRAP CLIENT"
#define AppExeName "TrapClientLauncher.exe"

[Setup]
AppId={{A4D9AEF6-0AE8-4D14-9756-2F5EF9A9D312}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#AppPublisher}
DefaultDirName={localappdata}\Programs\TRAP CLIENT
DefaultGroupName={#AppName}
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
OutputDir=..\build\distributions
OutputBaseFilename=TrapClient-Setup-26.1.2
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
UninstallDisplayName={#AppName}

[Files]
Source: "TrapClient.Launcher\bin\Release\net10.0-windows\win-x64\publish\{#AppExeName}"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{autoprograms}\{#AppName}"; Filename: "{app}\{#AppExeName}"
Name: "{autodesktop}\{#AppName}"; Filename: "{app}\{#AppExeName}"; Tasks: desktopicon

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional shortcuts:"

[Run]
Filename: "{app}\{#AppExeName}"; Description: "Launch TRAP CLIENT"; Flags: postinstall nowait skipifsilent
