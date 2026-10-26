# Collects the secrets Jenkins needs into C:\CapstoneProject\secrets (outside the repo). Values are typed at hidden prompts and never printed.
$ErrorActionPreference = 'Stop'
$dir = 'C:\CapstoneProject\secrets'
New-Item -ItemType Directory -Force $dir | Out-Null

function Read-Secret([string]$prompt) {
    $s = Read-Host -Prompt $prompt -AsSecureString
    $b = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($s)
    try { [Runtime.InteropServices.Marshal]::PtrToStringBSTR($b) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($b) }
}
function Save([string]$name, [string]$value) {
    [IO.File]::WriteAllText((Join-Path $dir $name), $value)
}

if (-not (Test-Path "$dir\jenkins_admin_password")) {
    $chars = (48..57) + (65..90) + (97..122)
    Save 'jenkins_admin_password' (-join ($chars | Get-Random -Count 20 | ForEach-Object { [char]$_ }))
    Write-Host "Generated a Jenkins admin password in $dir\jenkins_admin_password"
}
Save 'dockerhub_token' (Read-Secret 'Docker Hub access token (Read & Write)')
Save 'notify_email'    (Read-Host  'Email address for notifications (Gmail address used to send and receive)')
Save 'smtp_password'   (Read-Secret 'Gmail app password (16 characters, no spaces)')
if (-not (Test-Path "$dir\sonar_token")) { Save 'sonar_token' 'placeholder' }
Write-Host 'Done. Files are in' $dir '(never commit them).'
