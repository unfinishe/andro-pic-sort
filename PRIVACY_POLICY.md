# Privacy Policy / Datenschutzerklärung

## English

### Privacy Policy for Android Pic Sort

**Effective Date:** April 14, 2026  
**Last Updated:** April 14, 2026

#### 1. Introduction

Android Pic Sort ("the App") is designed with your privacy as a core principle. This Privacy Policy explains how the App operates and what data it handles.

#### 2. Data Collection & Processing

**The App does NOT collect, transmit, or share any personal data.**

- **No Network Requests**: The App operates entirely offline. It does not connect to the internet, external servers, or cloud services.
- **No Analytics or Tracking**: No analytics libraries, crash reporting, or tracking tools are integrated.
- **No User Account**: The App requires no login, account, or registration.
- **No Third-Party Services**: The App does not use advertising networks, SDKs, or third-party integrations that collect user data.

#### 3. Data Processing on Device

The App processes photos and media files you explicitly select using Android's Storage Access Framework (SAF):

- **File Access**: You grant permission to access a source folder and target folder via SAF. The App reads file metadata (EXIF data, file timestamps) and file content to determine organization strategy.
- **Local Processing**: All processing occurs on your device. Files are never transmitted or copied to external servers.
- **Local Settings Storage**: The App stores selected folder URIs and sorting preferences (for example operation mode, date source mode, dry run, and conflict policy) locally on your device so you can continue where you left off.

#### 4. Permissions

The App requests only the minimum access necessary:

- **Storage Access Framework (SAF) Tree Access**: You explicitly grant folder access for source and target locations. This access is used only to read, copy, or move files for operations you start.

These permissions are used **only** for the operations you explicitly initiate.

#### 5. File Metadata & EXIF

The App reads EXIF metadata from image files to extract the date taken. If EXIF data is unavailable or inconsistent, the App falls back to the file's creation timestamp. This fallback ensures your media is always organized by a reliable date source.

- EXIF data is read **only from files you explicitly process**.
- EXIF data is **never transmitted externally**.

#### 6. Storage & Backup

- **Android Backup**: Automatic Android backup is disabled for this app (`android:allowBackup="false"`).
- **No Cloud Sync**: The App does not sync files, metadata, or settings to cloud services.

#### 7. Data Security

- **On-Device Processing**: All file operations are performed locally on your device.
- **No External Transmission**: No data leaves your device during normal App operation.
- **Your Device, Your Data**: You retain complete control over all data and files at all times.

#### 8. Data Retention

The App does not retain uploaded cloud data and does not transmit data externally. It stores selected folder URIs and user preferences locally on your device using Android Preferences DataStore. You can clear this data by uninstalling the App or clearing App data in Android Settings.

#### 9. Children's Privacy

The App is not designed for children under 13. It does not collect age information and has no features specifically targeting minors. Parents and guardians who discover their child is using the App may contact the developer.

#### 10. Changes to This Policy

We may update this Privacy Policy from time to time. Changes will be reflected in the "Last Updated" date above. Significant changes will be communicated via release notes.

#### 11. Contact

If you have questions or concerns about this Privacy Policy, you may:

- Open an issue on GitHub: [https://github.com/unfinishe/andro-pic-sort/issues](https://github.com/unfinishe/andro-pic-sort/issues)

#### 12. Legal Basis

This Privacy Policy complies with:
- GDPR (General Data Protection Regulation) for EU users
- CCPA (California Consumer Privacy Act) for California residents
- Google Play Store Developer Program Policies

Since the App does not collect personal data, GDPR data subject rights (access, erasure, portability) are not applicable. You control all data on your device.

---

## Deutsch

### Datenschutzerklärung für Android Pic Sort

**Gültig ab:** 14. April 2026  
**Zuletzt aktualisiert:** 14. April 2026

#### 1. Einleitung

Android Pic Sort (die „App") ist mit deinem Datenschutz als Kernprinzip konzipiert. Diese Datenschutzerklärung erklärt, wie die App funktioniert und welche Daten sie verarbeitet.

#### 2. Datenerfassung & Verarbeitung

**Die App erfasst, überträgt oder teilt KEINE persönlichen Daten.**

- **Keine Netzwerkverbindungen**: Die App funktioniert vollständig offline. Sie verbindet sich nicht mit dem Internet, externen Servern oder Cloud-Diensten.
- **Keine Analysen oder Tracking**: Keine Analysebibliotheken, Absturzberichte oder Tracking-Tools sind integriert.
- **Kein Benutzeraccount**: Die App erfordert keine Anmeldung, kein Konto oder keine Registrierung.
- **Keine Drittanbieter-Services**: Die App verwendet keine Werbenetzwerke, SDKs oder Drittanbieter-Integrationen, die Benutzerdaten erfassen.

#### 3. Datenverarbeitung auf dem Gerät

Die App verarbeitet Fotos und Mediendateien, die du mithilfe von Androids Storage Access Framework (SAF) explizit auswählst:

- **Dateizugriff**: Du gewährst Zugriff auf einen Quellordner und einen Zielordner über SAF. Die App liest Datei-Metadaten (EXIF-Daten, Datei-Zeitstempel) und Dateiinhalte, um die Organisationsstrategie zu bestimmen.
- **Lokale Verarbeitung**: Alle Verarbeitung findet auf deinem Gerät statt. Dateien werden niemals auf externe Server übertragen oder kopiert.
- **Lokale Speicherung von Einstellungen**: Die App speichert ausgewählte Ordner-URIs und Sortiereinstellungen (z. B. Betriebsmodus, Datumsquelle, Dry Run und Konfliktregel) lokal auf deinem Gerät, damit du mit den letzten Einstellungen weiterarbeiten kannst.

#### 4. Berechtigungen

Die App fordert nur den notwendigsten Zugriff an:

- **Storage Access Framework (SAF) Ordnerzugriff**: Du gewährst den Zugriff auf Quell- und Zielordner explizit. Dieser Zugriff wird nur verwendet, um von dir gestartete Sortiervorgänge (Lesen, Kopieren, Verschieben) auszuführen.

Diese Berechtigungen werden **nur** für die Vorgänge verwendet, die du explizit initiierst.

#### 5. Datei-Metadaten & EXIF

Die App liest EXIF-Metadaten aus Bilddateien, um das Aufnahmedatum zu extrahieren. Wenn EXIF-Daten nicht verfügbar oder inkonsistent sind, fällt die App auf den Datei-Erstellungszeitstempel zurück. Dieser Fallback stellt sicher, dass deine Medien immer nach einer zuverlässigen Datumsquelle organisiert sind.

- EXIF-Daten werden **nur aus Dateien gelesen, die du explizit verarbeitest**.
- EXIF-Daten werden **niemals extern übertragen**.

#### 6. Speicherung & Sicherung

- **Android-Sicherung**: Die automatische Android-Sicherung ist für diese App deaktiviert (`android:allowBackup="false"`).
- **Keine Cloud-Synchronisierung**: Die App synchronisiert Dateien, Metadaten oder Einstellungen nicht mit Cloud-Diensten.

#### 7. Datensicherheit

- **Lokale Verarbeitung**: Alle Dateivorgänge werden lokal auf deinem Gerät durchgeführt.
- **Keine externe Übertragung**: Keine Daten verlassen dein Gerät während des normalen App-Betriebs.
- **Dein Gerät, deine Daten**: Du behältst jederzeit vollständige Kontrolle über alle Daten und Dateien.

#### 8. Datenverweilung

Die App speichert keine Cloud-Daten und überträgt keine Daten extern. Ausgewählte Ordner-URIs und Benutzereinstellungen werden lokal auf deinem Gerät mit Android Preferences DataStore gespeichert. Du kannst diese Daten löschen, indem du die App deinstallierst oder App-Daten unter Android-Einstellungen löscht.

#### 9. Datenschutz für Kinder

Die App ist nicht für Kinder unter 13 Jahren konzipiert. Sie erfasst keine Altersangaben und hat keine Funktionen, die speziell auf Minderjährige ausgerichtet sind. Eltern und Erziehungsberechtigte, die feststellen, dass ihr Kind die App nutzt, können sich an den Entwickler wenden.

#### 10. Änderungen dieser Richtlinie

Wir können diese Datenschutzerklärung von Zeit zu Zeit aktualisieren. Änderungen werden im Datum „Zuletzt aktualisiert" oben widergespiegelt. Wesentliche Änderungen werden über Release Notes mitgeteilt.

#### 11. Kontakt

Wenn du Fragen oder Bedenken zu dieser Datenschutzerklärung hast, kannst du:

- Ein Problem auf GitHub öffnen: [https://github.com/unfinishe/andro-pic-sort/issues](https://github.com/unfinishe/andro-pic-sort/issues)

#### 12. Rechtsgrundlage

Diese Datenschutzerklärung entspricht:
- DSGVO (Datenschutz-Grundverordnung) für EU-Nutzer
- CCPA (California Consumer Privacy Act) für Bewohner von Kalifornien
- Google Play Store Developer Program Policies

Da die App keine persönlichen Daten erfasst, sind DSGVO-Betroffenenrechte (Zugriff, Löschung, Portabilität) nicht anwendbar. Du kontrollierst alle Daten auf deinem Gerät.

---

**Version:** 1.0  
**Repository:** https://github.com/unfinishe/andro-pic-sort

