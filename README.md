# Apto UI SDK for Android 

Welcome to the Apto Android UI SDK. This SDK gives access to the Apto's mobile platform. Using this SDK you just start the SDK and it will onboard new users, issue cards, obtain card activity information and manage the card (set pin, freeze / unfreeze, etc.) without you doing anything else, just:

```kotlin
private fun startSdk() {
    val fontOptions = FontOptions(regularFontTypeface, mediumFontTypeface, semiBoldFontTypeface, boldFontTypeface)
    val cardOptions = CardOptions()
    AptoUiSdk.startCardFlow(activity, cardOptions,
        onSuccess = {
            // SDK successfully initialized
        },
        onError = {
            // SDK initialized with errors
        }
    )
}
```

## Requirements

    * Android Version - API Level 23 (Android 6.0)
    * Kotlin - 1.3.72
    * Gradle - 3.6.3

### Installation (Using [Gradle](https://gradle.org))

1. In your `build.gradle` file, add the following dependency:

    ```
    implementation 'com.aptopayments.sdk:ui:2.7.0'
    ```

2. Run `./gradlew build`.

### Permissions added in our SDK

    * <uses-permission android:name="android.permission.INTERNET" />
    * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    * <uses-permission android:name="android.permission.CALL_PHONE" />
    * <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    * <uses-permission android:name="android.permission.USE_BIOMETRIC" />


## Initializing the SDK

To start the Apto UI SDK you must first register a project in order to get a `API KEY`. Please contact Apto to create a project for you.

### One step initialization

This call must be made from the application class

```kotlin
AptoUiSdk.initializeWithApiKey(application, "API_KEY", AptoSdkEnvironment.SBX)
```

The last parameter is optional and indicates the environment that you are pointing to, in this case is Sandbox, by default is production.


### Two Step initialization

This call has to be done from the Application class

```kotlin
AptoUiSdk.initialize(application)
```

This second step can be deferred and placed anywhere before the first interaction with the SDK

```kotlin
AptoUiSdk.setApiKey("API_KEY", AptoSdkEnvironment.SBX)
```

## Starting the SDK

Once the SDK has been initialized you just have to call the `startCardFlow` method. This method has two required parameters and two optionals:

### from parameter

`Activity` from where the SDK is started. This is a required parameter used to present the SDK to the user.

### cardOptions parameter

A `CardOptions` instance that allows SDK customization. Using this parameter you can decide the features that are available to your users, or the font type to be used in the app.

```kotlin
val options = CardOptions(...)
```
you can set any of these parameters, all of them have a default parameter described also here:

1. `showStatsButton` control if the user can see his monthly consume stats. **Default to false**.
2. `showNotificationPreferences` control if the user can customize the notification channels. **Default to false**.
3. `showDetailedCardActivityOption` control if the user can show other transaction types (like declined transactions). **Default to false**.
4. `hideFundingSourcesReconnectButton` hides the Funding Sources reconnect button. **Default to false**.
5. `showAccountSettingsButton` control if the user can see the account settings screen. **Default to true**.
6. `showMonthlyStatementsOption` control if the user has access to the monthly statements. **Default to true**.
7. `authenticateOnStartup` control if the user has to authenticate on startup (with Passcode or Biometrics) of the app after going to background and returning **Default to false**.
8. `authenticateWithPINOnPCI` control if the user has to authenticate with passcode when tries to show full card data **Default to false**.
9. `darkThemeEnabled` control if dark theme is enabled or not on devices with android 10+. **Default to false**.

If you set it to true, you should also change your main theme to support DayNight Theme 

```xml
<style name="AppTheme" parent="Theme.AppCompat.DayNight.*">
...
</style>
```

10. `inAppProvisioningEnabled` this feature will be available in the future, and will require a further authorization of Google. **Default to false**.
11. `openingMode` **Default to standalone**.

A OpeningMode value that defines how the SDK is opened and closed.

EMBEDDED: if this mode is specified a close option will be added to the manage card screen so the user can close it and return to the host app. This is the recommended mode when you are starting the Apto UI SDK from an existing app.
STANDALONE: if this case the SDK can only be closed via logout. Use this mode when the host app has no other features than starting the Apto UI SDK.

12. `fontOptions : FontOptions`  is an optional parameter that specify a custom font to be used in the app providing to this class four different TypeFace. **Default to Phone fonts**.
You should replace the `.otf` file names for the ones that you include in your `assets` folder

```kotlin
val fontoptions = FontOptions(
        regularFont = Typeface.createFromAsset(assets, "regular-font.otf"),
        mediumFont = Typeface.createFromAsset(assets, "medium-font.otf"),
        semiBoldFont = Typeface.createFromAsset(assets, "semibold-font.otf"),
        boldFont = Typeface.createFromAsset(assets, "bold-font.otf")
)
```

### onSuccess parameter

A callback closure called once the Apto UI SDK has been initialized correctly.


### onError parameter

A callback closure called if there was a failure initializing the SDK.


## Further configuration

You can set extra configurations overriding certain keys in your strings.xml files

```xml
<string name="google_maps_key">your_google_maps_and_places_key</string>
<string name="apto_deep_link_scheme">your_deep_link_scheme</string>
```

## Contributing & Development

We're looking forward to receive your feedback including new feature requests, bug fixes and documentation improvements. If you waht to help us, please take a look at the [issues](https://github.com/AptoPayments/apto-ui-sdk-android/issues) section in the repository first; maybe someone else had the same idea and it's an ongoing or even better a finished task! If what you want to share with us is not in the issues section, please [create one](https://github.com/AptoPayments/apto-ui-sdk-android/issues/new) and we'll get back to you as soon as possible.

And, if you want to help us improve our SDK by adding a new feature or fixing a bug, we'll be glad to see your [pull requests!](https://github.com/AptoPayments/apto-ui-sdk-android/compare)
