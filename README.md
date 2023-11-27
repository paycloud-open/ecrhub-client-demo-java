# ECR-Hub Demo

## Version 1.0.0

ECR-Hub Demo version 1.0.0 introduces three major features, enhancing the experience of device connection, demo demonstrations, and simulated transactions. Users can easily connect devices, perform demo debugging, and simulate transactions.

## New Features

### 1. Setting

#### USB Connection Mode
- Choose USB connection mode by clicking the "Enable Service" button.
- Real-time feedback, including success and failure messages, is displayed at the bottom.
  ![img_2.png](imgs/img_2.png)

#### WLAN/LAN Connection Mode
- Switch to WLAN/LAN mode by clicking the "Enable Service" button to start listening.
- The left side displays connectable devices in the current network with options for refreshing (`Refresh`) and pairing (`Pairing`).
- The right side displays already paired devices with options to connect (`Connect`), disconnect (`Disconnect`), and unpair (`Unpairing`).
  ![img_5.png](imgs/img_5.png)

### 2. Demo Demonstration

- Connect via USB to perform demo demonstrations.
- Select a transaction type on the left, fill in transaction parameters.
- Click "Create Sample Message" to generate and display the sent message (Send Message) on the right.
- Users can manually input the message and click "Send" to simulate sending a transaction request to the device.
![img_3.png](imgs/img_3.png)

### 3. Shopping

- Connect the device to simulate transactions.
- Choose products on the first screen, click "Charge" to enter the confirmation screen.
![img_4.png](imgs/img_4.png)
- Select the device and transaction type, click "Submit" to trigger the device to send a request.
![img.png](imgs/img.png)
- The transaction result is displayed in real-time on the screen.
![img_1.png](imgs/img_1.png)
- Click the "Query" button to navigate to the order query page for order details. 
![img_6.png](imgs/img_6.png)
- Click the "Refund" button to navigate to the refund page. Input the order number for refund processing.
![img_7.png](imgs/img_7.png)

## Other Changes

- Updated project documentation, providing detailed usage instructions.

## Links

- [Download this version](https://github.com/paycloud-open/ecrhub-client-demo-java/releases/tag/v1.0.0)
- [Project documentation](https://github.com/paycloud-open/ecrhub-client-demo-java)

Released on November 24, 2023