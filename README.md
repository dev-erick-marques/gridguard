# Grid Guard

Grid Guard is a distributed, cryptographically-secured monitoring and control system for IoT devices.  
Each device periodically sends **digitally signed heartbeats** containing its voltage reading, and the Coordinator performs real-time statistical analysis to detect instability.  
When a trigger threshold is exceeded, the Coordinator issues a **signed shutdown command**. A device is only allowed to restart after **10 consecutive stable cycles**.

---

## 📌 Overview

Every device continuously reports its status through signed heartbeats, each containing:

- `deviceId`
- `voltage`
- `timestamp`
- Digital signature of the payload

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