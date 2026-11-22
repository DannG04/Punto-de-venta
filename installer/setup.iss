; ########################################################################
; # Script de Inno Setup para PuntoVenta - Sistema de Punto de Venta     #
; # Proyecto: Punto-de-venta (Daniel Gonzalez Ruiz)                      #
; ########################################################################

#define MyAppName "Sistema Punto de Venta"
#define MyAppVersion "1.22"
#define MyAppPublisher "Daniel Gonzalez Ruiz"
#define MyAppExeName "PuntoVenta.exe"
; Raiz del proyecto relativa a este .iss (installer\..): compila en cualquier carpeta/equipo
#define ProjectRoot AddBackslash(SourcePath) + ".."

[Setup]
AppId={{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\PuntoVenta
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
; Forzar instalación de 64 bits para que las rutas de PostgreSQL sean correctas
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64
OutputDir={#ProjectRoot}\installer\output
OutputBaseFilename=PuntoVentaSetup
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=admin
UninstallDisplayIcon={app}\{#MyAppExeName}
; Permite cerrar y reiniciar aplicaciones bloqueadas (como VC++ runtime) sin reboot del sistema
CloseApplications=yes
RestartApplications=yes

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"
Name: "installpostgres"; Description: "Instalar PostgreSQL 18"; GroupDescription: "Base de Datos:"; Flags: checkedonce

[Files]
Source: "{#ProjectRoot}\installer\jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#ProjectRoot}\dist\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#ProjectRoot}\installer\scripts\*.bat"; DestDir: "{app}\scripts"; Flags: ignoreversion
Source: "{#ProjectRoot}\installer\scripts\*.sql"; DestDir: "{app}\scripts"; Flags: ignoreversion
Source: "{#ProjectRoot}\installer\update\_bootstrap_schema_version.sql"; DestDir: "{app}\scripts"; Flags: ignoreversion
Source: "{#ProjectRoot}\installer\base_datos\*"; DestDir: "{app}\base_datos"; Flags: ignoreversion
; Migraciones: instalar_bd.bat aplica las que el backup no trae (V1.13 en adelante)
Source: "{#ProjectRoot}\migrations\*.sql"; DestDir: "{app}\migrations"; Flags: ignoreversion
Source: "{#ProjectRoot}\keystore.jks"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#ProjectRoot}\quagga.min.js"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#ProjectRoot}\installer\postgresql-18.1-2-windows-x64.exe"; DestDir: "{tmp}"; Flags: deleteafterinstall; Tasks: installpostgres

[Dirs]
; La app escribe en su carpeta (logs, tickets impresion*.txt, reportes Excel, logo.png).
; En Program Files un usuario sin privilegios no puede escribir: se da permiso de modificar.
Name: "{app}"; Permissions: users-modify

[Messages]
spanish.FinishedLabel=La instalación terminó.%n%nPara entrar la primera vez use:%n    Usuario: admin%n    Contraseña: admin123%n%nCambie la contraseña desde el módulo Empleados.

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"
Name: "{autodesktop}\PuntoVenta"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
; 1. Instalar PostgreSQL
; Sin --locale: usa el locale del SO (siempre disponible, evita error "locale no disponible")
; Sin --serviceaccount: PostgreSQL crea su propia cuenta "postgres" con permisos correctos
Filename: "{tmp}\postgresql-18.1-2-windows-x64.exe"; Parameters: "--mode unattended --unattendedmodeui minimal --superpassword ""root"" --servicename ""postgresql-x64-18"" --serverport 5432"; StatusMsg: "Instalando PostgreSQL 18..."; Flags: waituntilterminated; Tasks: installpostgres; Check: not ShouldSkipPostgreSQLInstall

; 1b. Reinicio silencioso de servicios que puedan haber quedado pendientes por VC++ (sin reiniciar el sistema)
Filename: "{cmd}"; Parameters: "/c net start postgresql-x64-18 2>nul || sc start postgresql-x64-18 2>nul"; StatusMsg: "Iniciando servicio PostgreSQL..."; Flags: waituntilterminated runhidden; Tasks: installpostgres; Check: not ShouldSkipPostgreSQLInstall

; 2. Pausa para arranque de servicio
Filename: "{cmd}"; Parameters: "/c timeout /t 20 /nobreak"; StatusMsg: "Esperando que PostgreSQL esté listo..."; Flags: waituntilterminated runhidden; Tasks: installpostgres; Check: not ShouldSkipPostgreSQLInstall

; 2b. Permitir el servidor del inventario móvil (puerto 8765) en redes privadas,
;     para que el celular conecte sin que Windows bloquee o pregunte.
Filename: "{sys}\netsh.exe"; Parameters: "advfirewall firewall delete rule name=""PuntoVenta Inventario Movil"""; Flags: runhidden waituntilterminated
Filename: "{sys}\netsh.exe"; Parameters: "advfirewall firewall add rule name=""PuntoVenta Inventario Movil"" dir=in action=allow protocol=TCP localport=8765 profile=private,domain"; StatusMsg: "Configurando firewall..."; Flags: runhidden waituntilterminated

; 3. La base de datos (crear si no existe, restaurar, schema_version y migraciones
;    pendientes) se prepara en CurStepChanged(ssPostInstall) con scripts\instalar_bd.bat,
;    que corre despues de estas entradas y avisa si algo falla.

; 5. Ejecutar App (WorkingDir crítico para que Java encuentre db.properties)
Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[UninstallRun]
Filename: "{sys}\netsh.exe"; Parameters: "advfirewall firewall delete rule name=""PuntoVenta Inventario Movil"""; Flags: runhidden; RunOnceId: "QuitarReglaFirewall"

[Code]
var
  PostgreSQLUserPage: TInputQueryWizardPage;
  EmpresaPage: TInputQueryWizardPage;
  LogoPage: TInputFileWizardPage;
  PostgreSQLUser, PostgreSQLPassword: String;
  NombreEmpresa, RutaLogo: String;
  PostgreSQLExists: Boolean;

// --- BUSCADOR DE RUTAS BINARIAS ---
function FindPostgreSQLBinPath(): String;
var
  PF64, PFx86, Candidate: String;
  Versions: array[0..4] of String;
  i: Integer;
begin
  Result := '';
  PF64 := ExpandConstant('{pf64}');
  PFx86 := ExpandConstant('{pf32}');
  Versions[0] := '18'; Versions[1] := '17'; Versions[2] := '16'; Versions[3] := '15'; Versions[4] := '14';
  for i := 0 to 4 do begin
    Candidate := PF64 + '\PostgreSQL\' + Versions[i] + '\bin';
    if FileExists(Candidate + '\psql.exe') then begin Result := Candidate; Exit; end;
    Candidate := PFx86 + '\PostgreSQL\' + Versions[i] + '\bin';
    if FileExists(Candidate + '\psql.exe') then begin Result := Candidate; Exit; end;
  end;
end;

// --- GENERADOR DE db.properties ---
procedure CreateDbPropertiesFile();
var
  FilePath: String;
  Lines: TStringList;
begin
  FilePath := ExpandConstant('{app}\db.properties');
  Lines := TStringList.Create;
  try
    Lines.Add(Trim('db.host=localhost'));
    Lines.Add(Trim('db.port=5432'));
    Lines.Add(Trim('db.name=punto_de_venta'));
    Lines.Add(Trim('db.user=' + Trim(PostgreSQLUser)));
    Lines.Add(Trim('db.password=' + Trim(PostgreSQLPassword)));
    Lines.SaveToFile(FilePath);
  finally
    Lines.Free;
  end;
end;

// --- GENERADOR DE config.properties (personalización de empresa) ---
procedure CreateConfigPropertiesFile();
var
  FilePath, LogoDestPath: String;
  Lines: TStringList;
begin
  FilePath := ExpandConstant('{app}\config.properties');
  Lines := TStringList.Create;
  try
    Lines.Add(Trim('app.nombre=' + Trim(NombreEmpresa)));
    // Si el usuario eligió un .ico válido, copiarlo a la carpeta de instalación
    if (RutaLogo <> '') and FileExists(RutaLogo) then begin
      LogoDestPath := ExpandConstant('{app}\logo.ico');
      FileCopy(RutaLogo, LogoDestPath, False);
      Lines.Add(Trim('app.logo=logo.ico'));
    end else
      Lines.Add(Trim('app.logo='));
    Lines.SaveToFile(FilePath);
  finally
    Lines.Free;
  end;
end;

// --- LÓGICA DE INSTALACIÓN ---
function IsPostgreSQLInstalled(): Boolean;
var ResultCode: Integer;
begin
  if Exec('sc', 'query postgresql-x64-18', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) and (ResultCode = 0) then
    Result := True
  else
    Result := (FindPostgreSQLBinPath() <> '');
end;

// --- VERIFICACIÓN DE CREDENCIALES POSTGRESQL ---
// Intenta una conexión real contra la base "postgres" del servidor existente.
// Devuelve True solo si psql termina con código 0 (autenticación correcta).
function VerifyPostgreSQLPassword(User, Password: String): Boolean;
var
  ResultCode: Integer;
  BinPath, PsqlPath, BatFile: String;
  Lines: TStringList;
begin
  Result := False;
  BinPath := FindPostgreSQLBinPath();
  if BinPath = '' then Exit;
  PsqlPath := BinPath + '\psql.exe';
  if not FileExists(PsqlPath) then Exit;

  // Escribimos un .bat temporal para evitar problemas de escape al pasar
  // la contraseña por línea de comandos. PGPASSWORD se setea en el entorno
  // local del .bat (no queda en el sistema).
  BatFile := ExpandConstant('{tmp}\verify_pg.bat');
  Lines := TStringList.Create;
  try
    Lines.Add('@echo off');
    Lines.Add('setlocal');
    Lines.Add('set "PGPASSWORD=' + Password + '"');
    Lines.Add('"' + PsqlPath + '" -U "' + User + '" -h localhost -p 5432 -d postgres -w -c "SELECT 1;" 1>nul 2>nul');
    Lines.Add('exit /b %ERRORLEVEL%');
    Lines.SaveToFile(BatFile);
  finally
    Lines.Free;
  end;

  if Exec(ExpandConstant('{cmd}'), '/C ""' + BatFile + '""', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
    Result := (ResultCode = 0);

  DeleteFile(BatFile);
end;

function InitializeSetup(): Boolean;
begin
  Result := True;
  PostgreSQLExists := IsPostgreSQLInstalled();
  PostgreSQLUser := 'postgres';
  PostgreSQLPassword := 'root';
  NombreEmpresa := 'Mi Empresa';
  RutaLogo := '';
end;

procedure InitializeWizard();
begin
  // Página de personalización: nombre de empresa
  EmpresaPage := CreateInputQueryPage(wpSelectTasks,
    'Personalización de la aplicación',
    'Configura los datos de tu empresa',
    'Estos datos aparecerán en la barra de título de la aplicación.');
  EmpresaPage.Add('Nombre de la empresa:', False);
  EmpresaPage.Values[0] := 'Mi Empresa';

  // Página de personalización: logo .ico
  LogoPage := CreateInputFilePage(EmpresaPage.ID,
    'Logo de la empresa',
    'Selecciona el logo de tu empresa (opcional)',
    'Elige un archivo .ico para personalizar el ícono de la aplicación. ' +
    'Si no tienes uno, se usará el ícono predeterminado.');
  LogoPage.Add('Archivo de logo (.ico):', 'Archivos de ícono|*.ico|Todos los archivos|*.*', '.ico');

  // Página de credenciales PostgreSQL (solo si ya está instalado)
  if PostgreSQLExists then begin
    PostgreSQLUserPage := CreateInputQueryPage(LogoPage.ID,
      'PostgreSQL Detectado', 'Servidor existente.',
      'Credenciales para "punto_de_venta":');
    PostgreSQLUserPage.Add('Usuario:', False);
    PostgreSQLUserPage.Add('Contraseña:', True);
    PostgreSQLUserPage.Values[0] := 'postgres';
  end;
end;

// --- CREAR / RESTAURAR / MIGRAR LA BASE (scripts\instalar_bd.bat) ---
procedure PrepararBaseDatos();
var
  ResultCode: Integer;
  Params: String;
begin
  WizardForm.StatusLabel.Caption := 'Preparando base de datos...';
  Params := '/C set "PGUSER=' + PostgreSQLUser + '"&& set "PGPASSWORD=' + PostgreSQLPassword +
            '"&& call "' + ExpandConstant('{app}\scripts\instalar_bd.bat') + '" "' + ExpandConstant('{app}') + '"';
  if not Exec(ExpandConstant('{cmd}'), Params, ExpandConstant('{app}'), SW_HIDE, ewWaitUntilTerminated, ResultCode)
     or (ResultCode <> 0) then
    MsgBox('No se pudo preparar la base de datos (codigo ' + IntToStr(ResultCode) + ').' + #13#10 +
           'Revisa el archivo:' + #13#10 + ExpandConstant('{app}\instalacion_bd.log') + #13#10 + #13#10 +
           'Puedes reintentar ejecutando scripts\instalar_bd.bat como administrador.',
           mbError, MB_OK);
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if CurStep = ssPostInstall then begin
    CreateDbPropertiesFile();
    CreateConfigPropertiesFile();
    PrepararBaseDatos();
  end;
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;
  // Capturar nombre de empresa
  if CurPageID = EmpresaPage.ID then begin
    NombreEmpresa := Trim(EmpresaPage.Values[0]);
    if NombreEmpresa = '' then NombreEmpresa := 'Mi Empresa';
  end;
  // Capturar ruta del logo (opcional — no se valida si está vacío)
  if CurPageID = LogoPage.ID then begin
    RutaLogo := Trim(LogoPage.Values[0]);
    if (RutaLogo <> '') and not FileExists(RutaLogo) then begin
      MsgBox('El archivo seleccionado no existe. Se usará el ícono predeterminado.', mbInformation, MB_OK);
      RutaLogo := '';
    end;
  end;
  // Capturar credenciales PostgreSQL (solo si ya existe)
  if PostgreSQLExists and (CurPageID = PostgreSQLUserPage.ID) then begin
    PostgreSQLUser := Trim(PostgreSQLUserPage.Values[0]);
    PostgreSQLPassword := PostgreSQLUserPage.Values[1];
    if PostgreSQLUser = '' then begin
      MsgBox('Usuario requerido.', mbError, MB_OK);
      Result := False;
      Exit;
    end;
    // Verificar contra el servidor real antes de avanzar. Si la contraseña
    // es incorrecta, mantener al usuario en la página para que reintente.
    WizardForm.Update();
    if not VerifyPostgreSQLPassword(PostgreSQLUser, PostgreSQLPassword) then begin
      MsgBox('No se pudo conectar a PostgreSQL con las credenciales proporcionadas.' + #13#10 + #13#10 +
             'Verifica que el usuario y la contraseña sean correctos e intenta nuevamente.' + #13#10 +
             'Si olvidaste la contraseña del superusuario "postgres", deberás restablecerla ' +
             'desde la configuración de PostgreSQL antes de continuar.',
             mbError, MB_OK);
      Result := False;
      Exit;
    end;
  end;
end;

procedure CurPageChanged(CurPageID: Integer);
var i: Integer;
begin
  if (CurPageID = wpSelectTasks) and PostgreSQLExists then
    for i := 0 to WizardForm.TasksList.Items.Count - 1 do
      if Pos('PostgreSQL', WizardForm.TasksList.Items[i]) > 0 then begin
        WizardForm.TasksList.Checked[i] := False;
        WizardForm.TasksList.ItemEnabled[i] := False;
      end;
end;

// Getters para [Run]
function GetPostgreSQLUser(Param: String): String; begin Result := PostgreSQLUser; end;
function GetPostgreSQLPassword(Param: String): String; begin Result := PostgreSQLPassword; end;
function ShouldSkipPostgreSQLInstall(): Boolean; begin Result := PostgreSQLExists; end;
function GetPsqlPath(Param: String): String;
var B: String; begin B := FindPostgreSQLBinPath(); if B <> '' then Result := B + '\psql.exe' else Result := 'psql.exe'; end;
function GetPgRestorePath(Param: String): String;
var B: String; begin B := FindPostgreSQLBinPath(); if B <> '' then Result := B + '\pg_restore.exe' else Result := 'pg_restore.exe'; end;