# push-to-github.ps1
# Initializes git (if necessary), creates branch 'main', adds remote and pushes.
# You will be prompted for GitHub credentials when git push runs.

Param(
    [string]$RemoteUrl = 'https://github.com/Subramanian03/Java-file-Copilot.git'
)

Write-Host "Running push-to-github.ps1..." -ForegroundColor Cyan

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "git is not installed or not on PATH. Install Git and re-run this script."
    exit 1
}

$cwd = Get-Location
Write-Host "Working directory: $cwd"

# Initialize repo if needed
if (-not (Test-Path .git)) {
    Write-Host "Initializing git repository..."
    git init
} else {
    Write-Host "Git repository already initialized."
}

# Ensure branch 'main' exists and is checked out
$branch = git rev-parse --abbrev-ref HEAD 2>$null
if ($branch -ne 'main') {
    Write-Host "Creating/checking out branch 'main'..."
    git checkout -B main
} else {
    Write-Host "Already on branch 'main'."
}

# Stage and commit changes if any
$changes = git status --porcelain
if ($changes) {
    Write-Host "Staging changes and committing..."
    git add .
    git commit -m "Initial commit: Java-user project"
} else {
    Write-Host "No changes to commit."
}

# Configure remote
$existing = git remote get-url origin 2>$null
if ($existing) {
    Write-Host "Remote 'origin' already set to: $existing"
    if ($existing -ne $RemoteUrl) {
        Write-Host "Updating remote 'origin' to $RemoteUrl"
        git remote set-url origin $RemoteUrl
    }
} else {
    Write-Host "Adding remote 'origin' -> $RemoteUrl"
    git remote add origin $RemoteUrl
}

# Configure credential helper to securely store PAT on Windows (manager-core)
try {
    git config --global credential.helper manager-core
    Write-Host "Configured credential helper: manager-core"
} catch {
    Write-Warning "Could not configure credential helper automatically. You may be prompted for credentials."
}

Write-Host "About to push to remote 'origin' branch 'main'. When prompted, enter your GitHub username and use a Personal Access Token (PAT) as the password." -ForegroundColor Yellow

# Push
$push = git push -u origin main
if ($LASTEXITCODE -ne 0) {
    Write-Error "Push failed. Inspect output above and try again."
    exit $LASTEXITCODE
}

Write-Host "Push completed." -ForegroundColor Green
