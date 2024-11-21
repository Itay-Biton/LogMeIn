# Log Me In

**LogMeIn** is a utility app designed for Android devices that allows users to monitor and interact with various device settings. The app features visual indicators (circles) that represent the current status of different system conditions, such as screen time, battery level, Wi-Fi connection, and more.

## Features

- **Screen Time Monitoring**: Tracks and displays the current screen time of the app.
- **Wi-Fi Network**: Monitors the status of the device's Wi-Fi connection and network status.
- **Orientation Check**: Detects the device’s orientation (landscape or portrait mode).
- **Volume Check**: Monitors the volume level of the device to ensure it’s set within a specific range.
- **Battery Level & Charging**: Tracks the device's battery status, including whether the device is charging or connected to a power source.
- **Proximity Sensor**: Detects whether the device is close to the user's face or a nearby object using the proximity sensor.

## Purpose

The purpose of **LogMeIn** is to provide a challenging experience that simulates an "escape room" style puzzle. Users must complete several checks and pass specific conditions to proceed. Each check verifies a critical system setting (e.g., battery level, screen orientation, proximity) before they can continue. This app is designed to make the login process more difficult, requiring users to engage with various aspects of their device's settings and pass all challenges to proceed.

## Installation

To install **LogMeIn** on your Android device, follow these steps:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Itay-Biton/LogMeIn.git
   ```

2. **Navigate to the project directory**:
   ```bash
   cd LogMeIn
   ```

3. **Open the project in Android Studio**.

4. **Build and run the project** on your Android device.

## Gameplay

**LogMeIn** doesn't follow the traditional "gameplay" model, but here’s how users interact with the app:

- **Circles**: The nine circles represent different settings (e.g., battery level, screen time, Wi-Fi connection, etc.). Each circle updates in real time based on the system's current status. They change color from red to green as each condition is met.
- **Slider**: Users should modify the slider position based on the current battery level of their device.
- **Password Input**: Users must enter a correct password to proceed. The password check is one of the conditions that must be met before the "Continue" button is enabled.
- **Continue Button**: The button remains disabled until all conditions are met, ensuring that the user has correctly configured their device settings.

### Screenshots and Video
<p align="center">
   <img src="./Screenshots/Page.png" alt="LogMeIn Page" width="200"/>
   <a href="https://www.youtube.com/watch?v=Zy3WGDr95Jc">
    <img src="https://img.youtube.com/vi/Zy3WGDr95Jc/0.jpg" alt="Watch the video" width="400">
   </a>
</p>

### How to Pass Each Check (9 Checks)

1. **Screen Time Monitoring**:
   - Ensure the app has been in use for the required screen time duration (10 seconds), resets after orientation change.
   - The circle will turn green once the required screen time is met.

2. **Wi-Fi Network**:
   - Connect the device to specific Wi-Fi network (itay's iPhone).
   - The circle will turn green once a stable Wi-Fi connection is detected.

3. **Orientation Check**:
   - Hold the device in landscape mode.
   - The circle will turn green when the correct orientation is detected.

4. **Volume Check**:
   - Adjust the volume of the device to at least 80% of the max volume.
   - The circle will turn green when the volume is within the acceptable range.

5. **Battery Charging**:
   - Plug the device into a charger or check if the battery is fully charged.
   - The circle will turn green when the device is charging or has an acceptable battery level.

6. **Proximity Sensor**:
   - Hold the device near your face or close to a surface that can trigger the proximity sensor.
   - The circle will turn green when the proximity sensor detects the device is close.

7. **Password Validation**:
   - Enter the password "hello" in the password input field.
   - The circle will turn green when the correct password is entered.

8. **Slider for Battery Condition**:
   - Adjust the slider based on the current battery level (or higher).
   - The slider will visually represent the battery condition and will update accordingly.

9. **Dynamic Continue Button**:
   - The "Continue" button will only become enabled when all checks pass.
   - Make sure all conditions (screen time, Wi-Fi, volume, battery, proximity, password) are correct before the button becomes active.

## Contact

If you have any questions or feedback, don't hesitate to get in touch via [email](mailto:itaybit10@gmail.com).

## License and Copyright

© 2024 Itay Biton. All rights reserved.

This project is owned by Itay Biton. Any unauthorized reproduction, modification, distribution, or use of this project, in whole or in part, is strictly prohibited without explicit permission from the author.

For academic purposes or personal review, please ensure proper credit is given to Itay Biton, and include a link to the original repository.

This project is intended for portfolio demonstration purposes only and must not be duplicated or repurposed without permission. If you're interested in collaborating or using parts of this project for educational reasons, please contact me directly.
