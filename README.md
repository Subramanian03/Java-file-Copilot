Java-user project — Setup and run

Prerequisites
- Java JDK 11+ installed (javac on PATH) or set `JAVA_HOME` to JDK root.
- Environment variable `SECRET_KEY` set to a strong server-only secret before running.

Quick checks (PowerShell)

```powershell
# From the project folder
.\check-java.ps1
```

Compile & run

```powershell
cd "f:\Copilot_QE\06_Code_Samples-20251211T035237Z-3-001\06_Code_Samples\python\Java-user"
# compile all classes
javac *.java
# set SECRET_KEY (example, set securely in your CI or system)
$env:SECRET_KEY = "your-very-secret-value"
# run demo
java UserDemo
```

Notes
- If `javac` is not found, install an OpenJDK distribution and ensure `JAVA_HOME\bin` is on your PATH.
- `SECRET_KEY` must be kept secret and rotated per your security policy.
- This project stores encrypted emails and uses an HMAC for lookups; never log the decrypted email in production.
