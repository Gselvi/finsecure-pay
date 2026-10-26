# Builds a token-based kubeconfig for the jenkins-deployer service account. Output goes outside the repo; the token is never printed.
param(
    [string]$ClusterName = 'finsecure-eks',
    [string]$Region = 'us-east-1',
    [string]$OutFile = 'C:\CapstoneProject\secrets\jenkins-kubeconfig.yaml'
)
$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force (Split-Path $OutFile) | Out-Null

$cluster = aws eks describe-cluster --name $ClusterName --region $Region --query 'cluster.[endpoint,certificateAuthority.data]' --output text
$endpoint, $ca = $cluster -split "\s+"
$tokenB64 = kubectl -n finsecure get secret jenkins-deployer-token -o jsonpath='{.data.token}'
$token = [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($tokenB64))

@"
apiVersion: v1
kind: Config
clusters:
  - name: $ClusterName
    cluster:
      server: $endpoint
      certificate-authority-data: $ca
users:
  - name: jenkins-deployer
    user:
      token: $token
contexts:
  - name: jenkins
    context:
      cluster: $ClusterName
      user: jenkins-deployer
      namespace: finsecure
current-context: jenkins
"@ | Set-Content -Path $OutFile -Encoding ascii

Write-Host "Wrote $OutFile (contains a secret token; keep it out of Git)"
