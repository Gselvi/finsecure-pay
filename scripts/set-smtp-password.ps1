# Replaces only the Gmail app password (hidden prompt; spaces are removed). Used for Jenkins and Alertmanager email.
$ErrorActionPreference = 'Stop'
$dir = 'C:\CapstoneProject\secrets'
$s = Read-Host -Prompt 'Gmail app password (16 characters; spaces are ignored)' -AsSecureString
$b = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($s)
try { $plain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($b) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($b) }
$plain = $plain -replace '\s', ''
if ($plain.Length -ne 16) { Write-Host "That is $($plain.Length) characters; a Gmail app password has exactly 16. Nothing was saved."; exit 1 }
[IO.File]::WriteAllText((Join-Path $dir 'smtp_password'), $plain)
Write-Host 'Saved the app password (not shown).'
