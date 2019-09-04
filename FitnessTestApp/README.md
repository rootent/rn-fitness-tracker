## EXAMPLE APP - FitenssTestApp

###### Android setup

1. Go to [google API console](https://console.developers.google.com) and create project.
2. Go to credentials and create new Android OAuth Client Id with keystore SHA1 fingerprint. (App will be signed with debug.keystore which is located in `android/app` directory.
).
3. Go to library section in [google API console](https://console.developers.google.com), find Fitness API and enable it for your project.
4. Go to [Firebase console](https://console.firebase.google.com), select or add your project. Go to project setting and download google-service.json file.
5. Place  google-service.json file in `<YourProjectName>/android/app` directory.
