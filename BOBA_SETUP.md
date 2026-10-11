# Boba shopping assistant setup

Boba is the Aishwarya Mart shopping assistant. Product search, budget suggestions, and cart actions run through the app's existing DAOs. Natural-language shopping questions use Gemini when a server-side API key is configured.

## Configure Gemini locally (Windows PowerShell)

Set the key in the same terminal environment that will launch Tomcat:

```powershell
$env:GEMINI_API_KEY = "PASTE_YOUR_GEMINI_API_KEY_HERE"
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& "C:\apache-tomcat-9.0.121\bin\startup.bat"
```

If Tomcat is already running, stop it and start it again from the terminal where the environment variable is set. Adjust the Java and Tomcat paths if yours differ.

Do not paste the key into JavaScript, HTML, GitHub, or chat messages. The browser calls the app's `/api/v1/chat` endpoint; only the Java server reads `GEMINI_API_KEY`.

## Configure deployment

In the hosting provider's environment-variable settings, add:

- Name: `GEMINI_API_KEY`
- Value: your Gemini API key

Redeploy or restart the app after saving the variable. Never commit the real key.

## If no API key is configured

The product catalogue search, budget recommendations, and sign-in-required cart actions still work. Open-ended AI replies use a friendly fallback until the environment variable is configured.
