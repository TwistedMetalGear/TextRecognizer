# TextRecognizer
TextRecognizer is an expansion of the text recognition portion of Google's [ML Kit sample app](https://github.com/firebase/quickstart-android/tree/master/mlkit/app/src/main/java/com/google/firebase/samples/apps/mlkit).

It adds the ability to enter a search query and detect matches in any text that the camera is pointed at. The matching text will be highlighted in real time. It also adds the ability to detect text matches that may span across multiple lines. This is not always the most reliable due to the variance in text results as the frames are processed. So the highlight may show for some frames, disappear in others, then show again.

To run TextRecognizer, add a new project on [Firebase console](https://console.firebase.google.com). Use the applicationId value specified in the app/build.gradle file as the Android package name. Download the generated google-services.json file and place it in the app/ directory of TextRecognizer.

# Screenshots

![Screenshot 1](https://raw.githubusercontent.com/TwistedMetalGear/TextRecognizer/master/screenshot.png)
