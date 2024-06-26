
Android DownloadableFonts Sample
===================================

This sample demonstrates how to use the Downloadable Fonts feature introduced in Android O.
Downloadable Fonts is a feature that allows apps to request a certain font from a provider
instead of bundling it or downloading it themselves. This means, there is no need to bundle the
font as an asset.

Introduction
------------

There are two ways of requesting a font to download.
To request a font to download from Java code, you need to create a [FontRequest][1] class first like
this:
```
FontRequest request = new FontRequest(
    "com.google.android.gms.fonts", // ProviderAuthority
    "com.google.android.gms",  // ProviderPackage
    query,  // Query
    R.array.com_google_android_gms_fonts_certs); // Certificates
```
The parameters `ProviderAuthority`, `ProviderPackage` are given by a font provider, in the case
above uses Google Play Services as a font provider.
The third parameter is a query string about the requested font. The syntax of the query is defined
by the font provider.

Then pass the request instance to the `requestFont` method in the [FontsContractCompat][2].
```
FontsContractCompat.requestFont(context, request, callback, handler);
```
The downloaded font or an error code if the request failed will be passed to the callback.
The example above assumes you are using the classes from the support library. There are
corresponding classes in the framework, but the feature is available back to API level 14 if you
use the support library.

You can declare a downloaded font in an XML file and let the system download it for you and use it
in layouts.
```xml
<font-family xmlns:app="http://schemas.android.com/apk/res-auto"
        app:fontProviderAuthority="com.google.android.gms.fonts"
        app:fontProviderPackage="com.google.android.gms"
        app:fontProviderQuery="Lobster Two"
        app:fontProviderCerts="@array/com_google_android_gms_fonts_certs">
</font-family>
```
By defining the requested font in an XML file and putting the `preloaded_fonts` array and the
meta-data tag in the AndroidManifest, you can avoid the delay until the font is downloaded by the
first attempt.
```xml
<resources>
    <array name="preloaded_fonts" translatable="false">
        <item>@font/lobster_two</item>
    </array>
</resources>
```

```xml
<application >
    ...
    <meta-data android:name="preloaded_fonts" android:resource="@array/preloaded_fonts" />
    ...
</application>
```

[1]: https://developer.android.com/reference/android/support/v4/provider/FontRequest.html
[2]: https://developer.android.com/reference/android/support/v4/provider/FontsContractCompat.html

Pre-requisites
--------------

- Android SDK 28
- Android Build Tools v28.0.3
- Android Support Repository

Screenshots
-------------

<img src="screenshots/screenshot-1.png" height="400" alt="Screenshot"/> 

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Stack Overflow: http://stackoverflow.com/questions/tagged/android

If you've found an error in this sample, please file an issue:
https://github.com/android/user-interface

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING.md for more details.
