# Photon Camera

Photon Camera is an Android photography application built with Jetpack Compose and the Camera2 API, faithfully reproducing the tactical ergonomics and analog color science of modern mirrorless digital cameras.

## Key Features

- **Mirrorless Digital Ergonomics**:
  - Tactile physical-feel PASM mode dial (Program, Aperture priority, Shutter priority, Manual).
  - Rotary jog wheel for precise parameter adjustments (Shutter Speed, ISO, EV Bias, Manual Focus Distance).
  - Two-stage physical shutter button with haptic feedback, countdown timer, and continuous high-speed burst shooting.
  - Hardware volume key integration for physical shutter release.
  - Multi-lens optical switching (Ultra-wide 0.6x, Main 1.0x, Telephoto 2.0x, Front camera).

- **Viewfinder & On-Screen Display (OSD)**:
  - Real-time OSD telemetry readout: Aperture, Shutter Speed, ISO, EV compensation, Kelvin temperature, SD card capacity, and battery status.
  - Live real-time luminance histogram.
  - Sensor-driven electronic horizon level line with degree readout and level alignment lock.
  - Rule-of-thirds and Golden Ratio composition framing guides.
  - Focus peaking and zebra overexposure clipping warnings.
  - Multi-aspect ratio optical masks (4:3, 16:9, 1:1, 3:2).

- **Color Science & Film Simulation Recipes**:
  - Built-in film stock simulations: *Classic Chrome*, *Provia Standard*, *Velvia Vivid*, *Acros Monochrome*, *Bleach Bypass*, *Nostalgic Negative*, *Cinema Teal & Orange*, and *Cinematic Warm 35*.
  - Full parametric recipe tuning: Dynamic Contrast, Highlight Recovery, Shadow Tone, Color Saturation, Kelvin White Balance, Magenta/Green Tint, Lens Vignette, Film Grain texture, and HDF Highlight Diffusion.
  - Custom Recipe Studio: Create, fine-tune, name, and save personalized color recipes.

- **Integrated Review Gallery**:
  - Full-screen photo inspector with interactive pinch-to-zoom.
  - Comprehensive EXIF metadata viewer (Focal length, Aperture, Shutter speed, ISO, Film Profile, Resolution, Date/Time).
  - Native system share and image management.

## Technical Architecture

- **UI Framework**: Modern Jetpack Compose with Material Design 3 and custom mirrorless camera body theming.
- **Camera Core**: Direct Camera2 API (`android.hardware.camera2.*`) with manual exposure (`SENSOR_EXPOSURE_TIME`, `SENSOR_SENSITIVITY`), manual focus (`LENS_FOCUS_DISTANCE`), and exposure compensation (`CONTROL_AE_EXPOSURE_COMPENSATION`).
- **Target Platform**: Android 11+ (minSdk 30, targetSdk 36).
