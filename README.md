# Java ChatGPT and DALL·E Integration with AdMob

Welcome to our Java-based application repository, integrating OpenAI's ChatGPT and DALL·E APIs with AdMob for monetization. This application offers a seamless chatbot experience along with the capability to generate AI art, providing an engaging user interface and an innovative way to interact with AI technologies.

## Features

- ChatGPT integration for conversational AI.
- DALL·E integration for AI-powered art generation.
- AdMob integration for app monetization.

## Prerequisites

Before you begin, ensure you have the following:

- Android Studio installed on your machine.
- An OpenAI API key for accessing ChatGPT and DALL·E services.
- An AdMob account with generated ad units and application ID.

## Setup Instructions

### Cloning the Repository

Start by cloning the repository to your local machine:

```bash
git clone <repository-url>
cd <repository-name>
```

### Configuring API Keys and AdMob

1. **OpenAI API Key**: Replace the API Key named in the `E:\Users\frink\Documents\GitHub\ChatGPT-Android\app\src\main\java\com\ai\alchemist\MainActivity.java` directory. Add your OpenAI API key as follows:

``` Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer OpenAI API KEY")
                .post(body)
                .build(); ```

Do the same for the ImageActivity.java in the same directory.


2. **AdMob Configuration**: In your `AndroidManifest.xml`, replace the placeholders with your actual AdMob App ID and ad unit IDs:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="your_admob_app_id_here"/>
```

Add the ad units in `E:\Users\frink\Documents\GitHub\ChatGPT-Android\app\src\main\res\layout` in the activity_images.xml and activity_main.xml

### Building the Application

Open the project in Android Studio, and perform a Gradle sync to download all the necessary dependencies. Once the sync is complete, you can build and run the application on your emulator or physical device through Android Studio.

## Usage

After launching the app, you can interact with the ChatGPT bot through the text input field. To generate art with DALL·E, navigate to the art generation section and input your prompt. Advertisements will be displayed according to your AdMob configuration.

## Contributing

Contributions are welcome! If you'd like to contribute, please fork the repository and use a feature branch. Pull requests are warmly welcomed.
