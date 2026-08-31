# Runner image for Forgejo Actions. Carries the exact toolchain the project
# pins, plus node, which Forgejo's JS actions need and which the generic
# Android images omit.
FROM ubuntu:24.04

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
    DEBIAN_FRONTEND=noninteractive

ENV ANDROID_BUILD_TOOLS_VERSION=36.0.0 \
    ANDROID_PLATFORM_VERSION=36 \
    ANDROID_NDK_VERSION=27.2.12479018 \
    ANDROID_CMAKE_VERSION=3.22.1 \
    ANDROID_CMDLINE_TOOLS_VERSION=11076708

RUN apt-get update && apt-get install -y --no-install-recommends \
        openjdk-17-jdk-headless git curl unzip zip ca-certificates \
        nodejs ninja-build file \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && curl -fsSL -o /tmp/clt.zip \
        "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip" \
    && unzip -q /tmp/clt.zip -d "${ANDROID_HOME}/cmdline-tools" \
    && mv "${ANDROID_HOME}/cmdline-tools/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest" \
    && rm /tmp/clt.zip

ENV PATH="${JAVA_HOME}/bin:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

RUN yes | sdkmanager --licenses > /dev/null 2>&1 || true
RUN sdkmanager --install \
        "platform-tools" \
        "platforms;android-${ANDROID_PLATFORM_VERSION}" \
        "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
        "ndk;${ANDROID_NDK_VERSION}" \
        "cmake;${ANDROID_CMAKE_VERSION}" > /dev/null

# Gradle refuses to run as root's default home in some setups; keep a writable cache
ENV GRADLE_USER_HOME=/opt/gradle-cache
RUN mkdir -p "${GRADLE_USER_HOME}"
