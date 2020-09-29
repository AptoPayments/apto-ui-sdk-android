# Apto Android UI SDK

Welcome to the Apto Android UI SDK. This SDK provides access to Apto's mobile platform.

With this SDK you can quickly:

* Onboard new users
* Issue cards
* Obtain card activity information
* Manage the card (set pin, lock / unlock, etc.)

**Note:** The Apto Mobile API has a request rate limit of 1 request per 30 seconds for the verification and login endpoints.

This document provides an overview of how to:

* [Install the SDK](#user-content-install-the-sdk)
* [Initialize the SDK](#user-content-initialize-the-sdk)
* [Start the Card UI Flow Process](#user-content-start-the-card-ui-flow-process)
* [Override Configurations and Keys](#user-content-override-configurations-and-keys)

For more information, see the [Apto Developer Docs](http://docs.aptopayments.com) or the [Apto API Docs](https://docs.aptopayments.com/api/MobileAPI.html).

To contribute to the SDK development, see [Contributions & Development](#user-content-contributions--development)

## Requirements

* Android SDK, minimum API Level 23 (Android 6.0)
* Kotlin, minimum version 1.4.0
* Gradle, minimum version 4.0.1

**Note:** The SDK is built using Kotlin, but is fully interoperable with Java. Code adjustments may be needed, if used within a Java project.

The following Android permissions are included with the UI SDK:

* `<uses-permission android:name="android.permission.INTERNET" />`
* `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />`
* `<uses-permission android:name="android.permission.CALL_PHONE" />`
* `<uses-permission android:name="android.permission.RECORD_AUDIO"/>`
* `<uses-permission android:name="android.permission.USE_BIOMETRIC" />`


### Get the Mobile API key

A Mobile API Key is required to run the SDK. To retrieve your Mobile API Key:

1. Register for an account or login into the [Apto Developer Portal](https://developer.aptopayments.com). 

2. Select **Developers** from the menu. Your **Mobile API Key** is listed on this page.

	![Mobile API Key](readme_images/devPortal_mobileApiKey.jpg)

	**Note:** `MOBILE_API_KEY` is used throughout this document to represent your Mobile API key. Ensure you replace `MOBILE_API_KEY` with the Mobile API Key in your account.

## Install the SDK 

We suggest using [Gradle](https://gradle.org) to install the SDK:

1. In your project's `build.gradle` file, add the Apto repository:

```
	allprojects {
		repositories {
		
			...
			
		    jcenter()
		    maven { url 'https://dl.bintray.com/apto/maven' }
		    maven { url "https://jitpack.io" }
			
			...
		}
	}
```

2. In your app's `build.gradle`, add the following dependency:

```
    dependencies {
    	...
    
		implementation 'com.aptopayments.sdk:mobile:3.2.0'
		implementation 'com.aptopayments.sdk:ui:3.2.0'
    
    	...
    }
```

**Note:** The other files in the repo are not required for installation or initialization. Those files are available so you can:

* See the SDK code.
* Modify and recompile the code, to create a custom SDK experience.

## Initialize the SDK

The SDK must be initialized using the `onCreate` method of your Application class, and requires your `MOBILE_API_KEY`: 

* **One-step initialization:** Load your `MOBILE_API_KEY` in the `onCreate` method of your Application class.
* **Two-step initialization:** Initialize the SDK, and set up the `MOBILE_API_KEY` prior to interacting with the SDK methods.

### One-step Initialization

In the `onCreate` method of your Application class, invoke `initializeWithApiKey` and pass in the `MOBILE_API_KEY` from the [Apto Developer Portal](https://developer.aptopayments.com). This fully initializes the SDK for your application.

```kotlin
AptoUiSdk.initializeWithApiKey(application, "MOBILE_API_KEY", AptoSdkEnvironment.SBX)
```

**Note:** The last parameter is optional and indicates the deployment environment. The default deployment environment is the Production environment (`AptoSdkEnvironment.PRD`). This example uses the Sandbox environment (`AptoSdkEnvironment.SBX`). 

### Two-step Initialization

If you want to defer setting your `MOBILE_API_KEY`, use the two-step initialization process:

1. In the `onCreate` method of your Application class, invoke the `initialize` method to initialize the SDK for your application.

```kotlin
AptoUiSdk.initialize(application)
```

2. Prior to your app's first interaction with the SDK, ensure you set your `MOBILE_API_KEY`. This fully initializes the SDK for your application.

```kotlin
AptoUiSdk.setApiKey("MOBILE_API_KEY", AptoSdkEnvironment.SBX)
```
**Note:** The last parameter is optional and indicates the deployment environment. The default deployment environment is Production. This example uses the Sandbox environment (`AptoSdkEnvironment.SBX`).

## Start the Card UI Flow Process

Once the SDK is initialized, you can initiate the card UI flow process with the `startCardFlow` SDK method:

1. Initialize a `CardOptions` object.

```kotlin
    val cardOptions = CardOptions()
```

2. Invoke the `startCardFlow` method, passing in the `activity` and `cardOptions` values.

```kotlin
    AptoUiSdk.startCardFlow(activity, cardOptions,
        onSuccess = {
            // SDK successfully initialized
        },
        onError = {
            // SDK initialized with errors
        }
    )
```


The `startCardFlow` method has two required parameters and two optional parameters:

Parameter|Required?|Description
---|---|---
`Activity`|Yes|This is the Android `Activity` object where the SDK will be started. This `Activity` is used to present the UI to the user.
`CardOptions`|Yes|This enables you to customize the SDK features. Use this parameter to specify which features are available to your users, and/or the font type for the UI. See [CardOptions Parameter](#user-content-cardoptions-parameter) for more information.
`onSuccess`|No|This is the callback closure called once the Apto UI SDK has been initialized.
`onError`|No|This is the callback closure called if there was a failure during the SDK initialization process.


### CardOptions Parameter

The `CardOptions` object can accept multiple unordered parameters. 

```kotlin
val cardOptions = CardOptions(...)
```

The available parameters are:

Parameter|Default Value|Description
---|---|---
`showStatsButton`|`false`|Controls if the user can see their monthly consumption statistics.
`showNotificationPreferences`|`false`|Controls if the user can customize their notification preferences.
`showDetailedCardActivityOption`|`false`| Controls if the user can view detailed transaction activity. For example, declined transactions.
`hideFundingSourcesReconnectButton`|`false`|Controls if the **Funding Sources Reconnect** button is shown.
`showAccountSettingsButton`|`true`|Controls if the user can see the the account settings screen.
`showMonthlyStatementsOption`|`true`|Controls if the user can view their monthly statements.
`authenticateOnStartup`|`false`|Controls if the user must authenticate their account (using a Passcode or Biometrics), when the app starts or after returning from background mode.
`authenticateWithPINOnPCI`|`false`|Controls if the user must authenticate using a Passcode, prior to viewing their full card data.
`darkThemeEnabled`|`false`|Controls if the UI's dark theme is enabled *(Only available on devices with Android 10+)*.<br><br>**Note:** If this value is set to `true`, you should also change your main theme to support the *DayNight Theme*.<br><br>`<style name="AppTheme" parent="Theme.AppCompat.DayNight.*">...</style>`
`inAppProvisioningEnabled`|`false`|This feature will be available in the future, and will require a additional Google authorization.
`openingMode`|`standalone`|Defines how the UI opens and closes.<ul><li>**`embedded`**: This displays a close button on the manage card screen, so the user can close the screen and return to the host app. This mode is the recommended when starting the Apto UI SDK from an existing app.</li><li>**`standalone`**: This does not display a close button on the manage card screen. The card UI can only be closed when the user logs out of the app. Use this mode when the host app only uses the Apto UI SDK.</li></ul>
`fontOptions` *(optional*)|`Phone fonts`|Specifies custom fonts for the UI. See [FontOptions Parameter](#user-content-fontoptions-parameter) for more information.

#### FontOptions Parameter

The `FontOptions` object requires you to specify four different type faces (`regularFont`, `mediumFont`, `semiBoldFont`, `boldFont`). 

```kotlin
val fontOptions = FontOptions(
        regularFont = Typeface.createFromAsset(assets, "regular-font.otf"),
        mediumFont = Typeface.createFromAsset(assets, "medium-font.otf"),
        semiBoldFont = Typeface.createFromAsset(assets, "semibold-font.otf"),
        boldFont = Typeface.createFromAsset(assets, "bold-font.otf")
)
```

**Note:** Ensure you replace the `.otf` file names with the files included in your project's `assets` folder.

## Override Configurations and Keys

You can set additional configurations to override keys in your `strings.xml` file found in the folder path `src/main/res/values/`. For example:

```xml
<string name="google_maps_key">your_google_maps_and_places_key</string>
<string name="apto_deep_link_scheme">your_deep_link_scheme</string>
```

## Contributions & Development

We look forward to receiving your feedback, including new feature requests, bug fixes and documentation improvements.

If you would like to help: 

1. Refer to the [issues](issues) section of the repository first, to ensure your feature or bug doesn't already exist (The request may be ongoing, or newly finished task).
2. If your request is not in the [issues](issues) section, please feel free to [create one](issues/new). We'll get back to you as soon as possible.

If you want to help improve the SDK by adding a new feature or bug fix, we'd be happy to receive [pull requests](compare)!
