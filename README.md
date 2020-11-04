# Apto Android UI SDK

Welcome to the Apto Android UI SDK. This SDK provides access to Apto's mobile platform, and provides a pre-built / standard UI/UX flow to onboard cardholders and enable users to manage their cards.

**Note:** If you want to control the UI/UX, use the Mobile SDK. 

You can quickly set up a UI/UX cardholder experience by dropping the SDK into your existing application or distributing it as a standalone mobile application. Some UI/UX elements may be configured to match your organization's branding look and feel, such as fonts, themes and enabled features.

**Note:** Branding features such as the card background image and button colors require configuration changes on Apto Payment's backend. Please [contact us](mailto:developers@aptopayments.com) for more information.

This document provides an overview of how to:

* [Install the SDK](#user-content-install-the-sdk)
* [Initialize the SDK](#user-content-initialize-the-sdk)
* [Start the Cardholder Onboarding Flow](#user-content-start-the-cardholder-onboarding-flow)
* [Override Configurations and Keys](#user-content-override-configurations-and-keys)

**Note:** The UI SDK automatically imports the [PCI SDK](https://github.com/AptoPayments/apto-pci-sdk-android). Therefore, the main screen will display a view provided by the [PCI SDK](https://github.com/AptoPayments/apto-pci-sdk-android).

For more information, see the [Apto Developer Guides](http://docs.aptopayments.com) or the [Apto API Docs](https://docs.aptopayments.com/api/MobileAPI.html).

To contribute to the SDK development, see [Contributions & Development](#user-content-contributions--development)

**Note:** The Apto Mobile API has a request rate limit of 1 request per 30 seconds for the verification and login endpoints.

## Requirements

* Android SDK, minimum API Level 23 (Marshmallow)
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

	**Note:** In order to register or login to your [Apto Developer Portal](https://developer.aptopayments.com) account, you will need to download a 2FA app such as the [Google Authenticator App](https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2) for your mobile device.

2. 	Your account has different Mobile API Keys: Sandbox and Production. Ensure you choose the correct environment from the dropdown located in the lower left of the page. 
	
	![Mobile API Key](readme_images/environment.jpg)

3. Select **Developers** from the left menu. Your **Mobile API Key** is listed on this page.

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

2. In your app's `build.gradle`, add the following compile options and build features:

```
android {
	
	...
	
	compileOptions {
		sourceCompatibility = 1.8
		targetCompatibility = 1.8
	}
	
	buildFeatures {
		dataBinding = true
	}
		
	...
	
}
```

**Note:** The `compileOptions` block is required if your minimum Android SDK version is below Level 26 (Oreo).

3. In your app's `build.gradle`, also add the following dependency:

```
    dependencies {
		...

		implementation 'com.aptopayments.sdk:ui:3.4.0'

		...
	}
```

This dependency already includes the [Android Mobile SDK](https://github.com/AptoPayments/apto-sdk-android) and the [Android PCI SDK](https://github.com/AptoPayments/apto-pci-sdk-android).

**Note:** The other files in the repo are not required for installation or initialization. Those files are available so you can:

* See the SDK code.
* Modify and recompile the code, to create a custom SDK experience. For example, if you want to create your own SDK to customize the onboarding flow.

## Initialize the SDK

The SDK must be initialized using the `onCreate` method of your Application class, and requires your `MOBILE_API_KEY`: 

* **One-step initialization:** Load your `MOBILE_API_KEY` in the `onCreate` method of your Application class.
* **Two-step initialization:** Initialize the SDK, and set up the `MOBILE_API_KEY` prior to interacting with the SDK methods.

### One-step Initialization

In the `onCreate` method of your Application class, invoke `initializeWithApiKey` and pass in the `MOBILE_API_KEY` from the [Apto Developer Portal](https://developer.aptopayments.com). This fully initializes the SDK for your application.

```kotlin
AptoUiSdk.initializeWithApiKey(application, "MOBILE_API_KEY")
```

The default deployment environment is the Production environment (`AptoSdkEnvironment.PRD`). If you are deploying using the Sandbox environment, you must set the optional environment parameter to `AptoSdkEnvironment.SBX`:

```kotlin
AptoUiSdk.initializeWithApiKey(application, "MOBILE_API_KEY", AptoSdkEnvironment.SBX)
```

### Two-step Initialization

If you want to defer setting your `MOBILE_API_KEY`, use the two-step initialization process:

1. In the `onCreate` method of your Application class, invoke `AptoUiSdk.initialize` initialize the SDK for your application.

```kotlin
AptoUiSdk.initialize(application)
```

2. Prior to your app's first interaction with the SDK, ensure you set your `MOBILE_API_KEY`. This fully initializes the SDK for your application.

	The default deployment environment is the Production environment (`AptoSdkEnvironment.PRD`). If you are deploying using the Sandbox environment, you must set the optional environment parameter to `AptoSdkEnvironment.SBX`:

```kotlin
AptoUiSdk.setApiKey("MOBILE_API_KEY", AptoSdkEnvironment.SBX)
```

## Start the Cardholder Onboarding Flow

Once the SDK is initialized, you can implement our pre-built cardholder onboarding flow. The onboarding flow enables users to verify their credentials login and to manage their cardholder information. For an example of the cardholder onboarding UI/UX screens, please view the [Apto Developer Guides](http://docs.aptopayments.com).

To start the cardholder onboarding flow:

1. Initialize a `CardOptions` object.

```kotlin
    val cardOptions = CardOptions()
```

2. Invoke the `startCardFlow` method, passing in the `activity` and `cardOptions` values. Ensure you replace the `activity` parameter with the Android Activity where the UI SDK will start from.

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


The `startCardFlow` method has one required parameter and four optional parameters:

Parameter|Required?|Description
---|---|---
`activity`|Yes|This is the Android `Activity` object where the SDK will be started. This `Activity` is used to present the UI to the user.
`cardOptions`|No|The UI SDK has multiple features that can be enabled / disabled. This parameter is used to enable / disable card management features and can be used to define the card theme and fonts. See [CardOptions Parameters](#user-content-cardoptions-parameters) for more information.
`metadata`|No|Metadata that will be stored in our servers.
`onSuccess` (optional)|No|This is the callback closure called once the Apto UI SDK has been initialized.
`onError` (optional)|No|This is the callback closure called if there was a failure during the SDK initialization process.


### CardOptions Parameters

The UI SDK retrieves all card design configurations from our servers and applies the configurations to the included [PCI SDK](https://github.com/AptoPayments/apto-pci-sdk-android). This ensures the correct card styles and design are shown to the user, including:

* Card Background
* Card Text Colors

Use the `CardOptions` object to customize the the card management features and card themes and fonts.

Some of the features that can be customized are:

* Enable / Disable management of:
	* Account Settings
	* Stats
	* Funding Source
	* Notification Preferences
	* Detailed Card Transaction Activity
	* Monthly Statements
* Authentication Settings
* Card Theme
* UI opening display mode
* Font Options
* Card logo and design
	
	**Note:** If you need to customize the logo and/or card design, it will need to be configured on our servers. You will need to send us a 969px × 612px png file of the entire card, including the background, company logo, and network logo (IE Visa, Mastercard, etc). Once your PNG file is set up, the UI SDK will communicate with our servers to retrieve the PNG file and display it to the user. Please [contact us](mailto:developers@aptopayments.com) for more information.

The `CardOptions` object can accept multiple unordered parameters. No parameters are required to create a `CardOptions` object.

```kotlin
val cardOptions = CardOptions(...)
```

The available parameters are:

Parameter|Default Value|Description
---|---|---
`showAccountSettingsButton`|`true`|Controls if the Account Settings button is displayed. This enables the user to see the account settings screen.<br/><br/>![Account Settings button](readme_images/accounts.jpg)
`showStatsButton`|`false`|Controls the Stats button is displayed. This enables the user to see their monthly consumption statistics.<br/><br/>![Stats button](readme_images/stats.jpg)
`hideFundingSourcesReconnectButton`|`false`|Controls if the **Add funding source** button is shown.
`showNotificationPreferences`|`false`|Controls if the user can customize their notification preferences.
`showDetailedCardActivityOption`|`false`|Controls if the user can view detailed transaction activity. For example, declined transactions.
`showMonthlyStatementsOption`|`true`|Controls if the user can view their monthly statements.
`authenticateOnStartup`|`false`|Controls if the user must authenticate their account (using a Passcode or Biometrics), when the app opens or after returning from background mode. Enabling this option will require the user to create a Passcode when signing up.
`authenticateWithPINOnPCI`|`false`|Controls if the user must authenticate using their Passcode, prior to viewing their full card data. <br/><br/>**Note:** If biometric authentication is enabled, it will appear first. The user may choose to cancel biometric authentication and use their Passcode instead.
`darkThemeEnabled`|`false`|Controls if the UI's dark theme is enabled. *(Only available on devices with Android 10+)*.<br/><br/>**Note:** If this value is set to `true`, you should also change your app theme to support the *DayNight Theme*. See [darkThemeEnabled Parameter](#user-content-darkthemeenabled-parameter)
`inAppProvisioningEnabled`|`false`|This feature will be available in the future, and will require an additional Google authorization.
`openingMode`|`standalone`|Defines how the UI opens and closes.<ul><li>**`embedded`**: This displays a close button on the manage card screen, so the user can close the screen and return to the host app. This mode is the recommended when starting the Apto UI SDK from an existing app.</li><li>**`standalone`**: This does not display a close button on the manage card screen. The card UI can only be closed when the user logs out of the app. Use this mode when the host app only uses the Apto UI SDK.</li></ul>
`fontOptions` *(optional*)|`Phone fonts`|Specifies custom fonts for the UI. See [FontOptions Parameter](#user-content-fontoptions-parameter) for more information.

#### darkThemeEnabled Parameter

The `darkThemeEnabled` parameter controls if the UI's dark theme is enabled.

If you have `darkThemeEnabled` set to `true`, ensure you have also change your app theme to support the *DayNight Theme*.

For example, in your `/src/main/res/values/styles.xml` file, change the following:

```xml
    <style name="AppTheme" parent="Theme.AppCompat.DayNight.*">
        
        ...
        
    </style>
```

#### FontOptions Parameter

The `fontOptions` parameter specifies custom fonts for the UI. A `FontOptions` object and can have up to 4 type face parameters:

* `regularFont`
* `mediumFont`
* `semiBoldFont`
* `boldFont`

**Note:** Although no parameters are required to create a `FontOptions` object, we recommend you:

* Set no type faces, and use the default phone fonts for the UI.
* Set all 4 type faces to provide consistent fonts throughout the UI.

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

You can set additional configurations to override keys in your `strings.xml` file found in the folder path `src/main/res/values/`. For example, replace `your_deep_link_scheme` with your custom deep linking scheme:

```xml
<string name="apto_deep_link_scheme">your_deep_link_scheme</string>
```

**Note:** By default, deep linking uses `aptopayments`. You should replace it with your custom deep linking scheme.

## Contributions & Development

We look forward to receiving your feedback, including new feature requests, bug fixes and documentation improvements.

If you would like to help: 

1. Refer to the [issues](https://github.com/AptoPayments/apto-ui-sdk-android/issues) section of the repository first, to ensure your feature or bug doesn't already exist (The request may be ongoing, or newly finished task).
2. If your request is not in the [issues](https://github.com/AptoPayments/apto-ui-sdk-android/issues) section, please feel free to [create one](https://github.com/AptoPayments/apto-ui-sdk-android/issues/new). We'll get back to you as soon as possible.

If you want to help improve the SDK by adding a new feature or bug fix, we'd be happy to receive [pull requests](https://github.com/AptoPayments/apto-ui-sdk-android/compare)!
