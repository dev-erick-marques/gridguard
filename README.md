# Grid Guard

Grid Guard is a distributed, cryptographically-secured monitoring and control system for IoT devices.  
Each device periodically sends **digitally signed heartbeats** containing its voltage reading, and the Coordinator performs real-time statistical analysis to detect instability.  
When a trigger threshold is exceeded, the Coordinator issues a **signed shutdown command**. A device is only allowed to restart after **10 consecutive stable cycles**.

---

## 📌 Overview

Every device continuously reports its status through **digitally signed heartbeats**, each containing the following fields:

- `deviceId` — Unique identifier of the device
- `voltage` — Current measured voltage
- `status` — Current operational state of the device
- `reason` — Reason for the current status
- `deviceAddress` — Network address of the device
- `deviceName` — Human-readable name of the device
- `timestamp` — Instant when the heartbeat was generated
- **signature** — Digital signature (ECDSA) of the payload ensuring authenticity and integrity


The Coordinator validates all signatures and feeds the values into a stability model based on:

- Mean
- Standard deviation
- Variation over time

If instability is detected, the Coordinator sends a **secure shutdown command**.  
After shutdown, the device only receives a **restart command** once stability is confirmed across 10 sequential heartbeat cycles.

---

## ⚙️ System Workflow

### 1. Heartbeat Transmission
Each device:
1. Reads its current voltage.
2. Builds the heartbeat payload.
3. Signs the payload using its private key.
4. Sends the heartbeat to the Coordinator.

### 2. Signature Validation
The Coordinator:
- Verifies digital signatures.
- Ensures authenticity and integrity of the heartbeat.

### 3. Statistical Processing
For each device, the Coordinator maintains a sliding window of recent readings and calculates:
- Average (mean)
- Standard deviation
- Voltage variation between cycles

### 4. Trigger Detection
If any metric exceeds configured thresholds:
- A **signed shutdown command** is sent.
- Only devices with the matching public key accept the command.

### 5. Controlled Restart
After shutdown, the device remains in safe mode while still sending minimal heartbeats.  
The Coordinator only sends `RESTART` when **10 stable cycles** occur consecutively.

---

## 🔐 Security Model

Communications use asymmetric cryptography:

- Heartbeats → **signed by the device**
- Commands (shutdown/restart) → **signed by the Coordinator**

This prevents:
- Command forgery
- Payload tampering
- Replay attacks

---

## 🛠 Tech Stack
- Java 17+,
- Spring Boot, 
- Maven
- SHA-256
- React Typescript + vite (Frontend)

--- 
## 📘 Statistical Calculations

### 1. Mean
$$ \mu = \frac{1}{N} \sum_{i=1}^{N} x_i $$

### 2. Variance
$$ \sigma^{2} = \frac{1}{N} \sum_{i=1}^{N} (x_i - \mu)^2 $$

### 3. % Standard Deviation
$$ \sigma = \sqrt{\sigma^{2}} $$

### 4. Variation Percentage (Coefficient of Variation %)
$$ CV = \left( \frac{\sigma}{\mu} \right) \times 100 $$


### 5. Instability Trigger Rule %
$$ CV > \text{threshold} $$


### 6. Stable Cycle Requirement
$$ \text{stable cycles} \geq 10 $$

---
## ⚡ Storm Spike Simulation & Weather-Based Shutdown Logic

The device is capable of generating **simulated voltage spikes of up to 20 kV**, representing extreme electrical surges that may occur during thunderstorms. These spikes are used to validate how the system reacts to sudden and dangerous electrical conditions.

### 🔍 Why simulate 20 kV spikes?
- During heavy storms, the electrical grid may experience abrupt surges caused by **lightning strikes**, **electromagnetic induction**, and **rapid voltage oscillations**.
- These events can severely damage connected equipment.
- Simulating them allows the Grid Guard system to be tested safely under real-world-like conditions.

### ⚡ Future Integration with the Coordinator
A **storm-detection mode** will be introduced in the Coordinator.  
The Coordinator will track a metric representing atmospheric electrical activity, such as a simulated lightning strike counter.

When storm intensity reaches the defined threshold, the Coordinator will issue a **signed safe shutdown command** to devices and prevent restart until the environment becomes stable again.


### 🧠 Planned Logic

```java
if (lightningStrikes >= threshold) {
    sendSignedShutdownCommand();
}

````
### 🛡 Why this matters

This mechanism increases the resilience and safety of the system:

- Real 20 kV spikes can physically damage hardware
- Weather-aware decisions add a crucial protective layer
- Combines external environmental signals with internal statistical analysis

Together, these components enhance Grid Guard's ability to protect devices in both predictable and extreme conditions.

----
## 🚀 How to Run

#### Frontend
```
cd frontend
npm install
npm run dev
```
- Access: http://localhost:5173

#### Coordinator
```
cd coordinator
mvn clean install
java -jar target/coordinator.jar
```

#### Device
```
cd device
mvn clean install
java -jar target/device.jar
```
