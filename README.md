# NEXUS CONTROL — page de téléchargement

Site statique (`index.html`) + `downloads/NEXUS.apk` (Android) et `downloads/NEXUS.exe` (Windows 10/11, 64 bits).

- Les deux fichiers sont fabriqués automatiquement par GitHub Actions (`.github/workflows/build.yml`) à chaque modification de `android/`, `windows/` ou `shared/`.
- Les applis ouvrent ton NEXUS en ligne ; au premier lancement elles demandent son adresse (écran `shared/setup.html`).
- Déploiement : Render → Static Site → ce dépôt, branche `main`, Build Command vide, Publish Directory `.`
