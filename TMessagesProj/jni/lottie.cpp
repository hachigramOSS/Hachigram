#include <jni.h>
#include <android/bitmap.h>
#include <tlottie.h>
#include <zlib.h>

#include <algorithm>
#include <cstdint>
#include <cstdio>
#include <string>
#include <vector>

namespace {

constexpr float kDefaultCurveTolerance = 0.125f;

struct LayerColorReplacements {
    // The C ABI borrows these UTF-8 buffers during tlottie_new_with_options.
    std::vector<std::string> prefixes;
    std::vector<TLottieLayerColorReplacement> values;
};

static uint32_t toTlottieFitz(jint modifier) {
    switch (modifier) {
        case 12: return TLOTTIE_FITZ_TYPE_12;
        case 3: return TLOTTIE_FITZ_TYPE_3;
        case 4: return TLOTTIE_FITZ_TYPE_4;
        case 5: return TLOTTIE_FITZ_TYPE_5;
        case 6: return TLOTTIE_FITZ_TYPE_6;
        default: return TLOTTIE_FITZ_NONE;
    }
}

static std::string normalizePrefix(std::string prefix) {
    if (prefix.size() >= 3 && prefix.compare(prefix.size() - 3, 3, ".**") == 0) {
        prefix.resize(prefix.size() - 3);
    } else if (prefix.size() >= 2 && prefix.compare(prefix.size() - 2, 2, "**") == 0) {
        prefix.resize(prefix.size() - 2);
    }
    return prefix;
}

static LayerColorReplacements readLayerColors(JNIEnv *env, jobjectArray names, jintArray colors) {
    LayerColorReplacements result;
    if (names == nullptr || colors == nullptr) {
        return result;
    }
    const jsize count = std::min(env->GetArrayLength(names), env->GetArrayLength(colors));
    jint *values = env->GetIntArrayElements(colors, nullptr);
    if (values == nullptr) {
        return result;
    }
    // Reserving is important: moving an SSO string later could invalidate the
    // pointer already stored in the corresponding C replacement descriptor.
    result.prefixes.reserve(static_cast<size_t>(count));
    result.values.reserve(static_cast<size_t>(count));
    for (jsize i = 0; i < count; ++i) {
        auto name = static_cast<jstring>(env->GetObjectArrayElement(names, i));
        if (name != nullptr) {
            const char *chars = env->GetStringUTFChars(name, nullptr);
            if (chars != nullptr) {
                result.prefixes.push_back(normalizePrefix(chars));
                const std::string &prefix = result.prefixes.back();
                result.values.push_back({reinterpret_cast<const uint8_t *>(prefix.data()),
                                         prefix.size(), static_cast<uint32_t>(values[i])});
                env->ReleaseStringUTFChars(name, chars);
            }
            env->DeleteLocalRef(name);
        }
    }
    env->ReleaseIntArrayElements(colors, values, JNI_ABORT);
    return result;
}

static std::vector<TLottieColorReplacement> readColorReplacements(JNIEnv *env, jintArray replacements) {
    std::vector<TLottieColorReplacement> result;
    if (replacements == nullptr) {
        return result;
    }
    jint *values = env->GetIntArrayElements(replacements, nullptr);
    if (values == nullptr) {
        return result;
    }
    const jsize count = env->GetArrayLength(replacements);
    result.reserve(static_cast<size_t>(count / 2));
    for (jsize i = 0; i + 1 < count; i += 2) {
        // Android colors are 0xAARRGGBB. tlottie replacement matching is RGB-only;
        // keep the standard RRGGBB byte order and discard the Java alpha byte.
        result.push_back({static_cast<uint32_t>(values[i]) & 0x00ffffffu,
                          static_cast<uint32_t>(values[i + 1]) & 0x00ffffffu});
    }
    env->ReleaseIntArrayElements(replacements, values, JNI_ABORT);
    return result;
}

static bool readFile(const char *path, std::string &out) {
    gzFile file = gzopen(path, "rb");
    if (file == nullptr) {
        return false;
    }
    char buffer[16 * 1024];
    int read;
    while ((read = gzread(file, buffer, sizeof(buffer))) > 0) {
        out.append(buffer, static_cast<size_t>(read));
    }
    const bool ok = read == 0;
    gzclose(file);
    return ok && !out.empty();
}

static TLottieInstance *createInstance(JNIEnv *env, const char *json, size_t jsonLength,
                                       jobjectArray layerNames, jintArray layerValues,
                                       jintArray colorReplacement, jint fitzModifier) {
    const LayerColorReplacements layerReplacements =
            readLayerColors(env, layerNames, layerValues);
    const std::vector<TLottieColorReplacement> colorReplacements =
            readColorReplacements(env, colorReplacement);
    return tlottie_new_with_options(
            reinterpret_cast<const uint8_t *>(json), jsonLength, toTlottieFitz(fitzModifier),
            layerReplacements.values.data(), layerReplacements.values.size(),
            colorReplacements.data(), colorReplacements.size(), TlottieChannelOrder::TLOTTIE_CHANNEL_RGBA);
}

static bool writeMetadata(JNIEnv *env, jintArray data, TLottieInstance *instance) {
    const uint32_t frameCount = tlottie_frame_count(instance);
    const int32_t fps = static_cast<int32_t>(tlottie_frame_rate(instance));
    if (fps <= 0 || fps > 60 || frameCount > 600) {
        return false;
    }
    if (data == nullptr || env->GetArrayLength(data) < 3) {
        return true;
    }
    const jint values[3] = {static_cast<jint>(frameCount), fps, 0};
    env->SetIntArrayRegion(data, 0, 3, values);
    return true;
}

// tlottie stretches the composition to the target size, rlottie fitted it preserving aspect ratio
template<typename Pixel, typename Render>
static int32_t renderFitted(const TLottieInstance *instance, const AndroidBitmapInfo &info,
                            void *rawPixels, bool clear, Render render) {
    const uint32_t contentWidth = tlottie_width(instance);
    const uint32_t contentHeight = tlottie_height(instance);
    uint32_t width = info.width;
    uint32_t height = info.height;
    if (contentWidth != 0 && contentHeight != 0) {
        const uint64_t widthByContentHeight = static_cast<uint64_t>(info.width) * contentHeight;
        const uint64_t heightByContentWidth = static_cast<uint64_t>(info.height) * contentWidth;
        if (widthByContentHeight > heightByContentWidth) {
            width = std::max<uint32_t>(1, static_cast<uint32_t>((heightByContentWidth + contentHeight / 2) / contentHeight));
        } else if (widthByContentHeight < heightByContentWidth) {
            height = std::max<uint32_t>(1, static_cast<uint32_t>((widthByContentHeight + contentWidth / 2) / contentWidth));
        }
    }
    auto *bytes = static_cast<uint8_t *>(rawPixels);
    const size_t count = static_cast<size_t>(width) * height;
    if (width == info.width && height == info.height && info.stride == width * sizeof(Pixel)) {
        return render(width, height, reinterpret_cast<Pixel *>(bytes), count);
    }
    const uint32_t left = (info.width - width) / 2;
    const uint32_t top = (info.height - height) / 2;
    auto *region = bytes + static_cast<size_t>(top) * info.stride + left * sizeof(Pixel);
    std::vector<Pixel> temporary(count);
    if (!clear) {
        for (uint32_t y = 0; y < height; ++y) {
            std::copy_n(reinterpret_cast<const Pixel *>(region + static_cast<size_t>(y) * info.stride),
                        width, temporary.data() + static_cast<size_t>(y) * width);
        }
    }
    const int32_t result = render(width, height, temporary.data(), count);
    if (result != TLOTTIE_OK) return result;
    if (clear) {
        for (uint32_t y = 0; y < info.height; ++y) {
            std::fill_n(reinterpret_cast<Pixel *>(bytes + static_cast<size_t>(y) * info.stride), info.width, Pixel{});
        }
    }
    for (uint32_t y = 0; y < height; ++y) {
        std::copy_n(temporary.data() + static_cast<size_t>(y) * width, width,
                    reinterpret_cast<Pixel *>(region + static_cast<size_t>(y) * info.stride));
    }
    return result;
}

} // namespace

extern "C" {

JNIEXPORT jlong JNICALL Java_org_telegram_ui_Components_RLottieNative_nCreate(
        JNIEnv *env, jclass, jstring src, jstring json, jintArray data,
        jintArray colorReplacement, jint fitzModifier,
        jobjectArray layerNames, jintArray layerColors) {
    std::string input;
    if (json != nullptr) {
        const char *chars = env->GetStringUTFChars(json, nullptr);
        if (chars != nullptr) {
            input.assign(chars, static_cast<size_t>(env->GetStringUTFLength(json)));
            env->ReleaseStringUTFChars(json, chars);
        }
    } else if (src != nullptr) {
        const char *path = env->GetStringUTFChars(src, nullptr);
        if (path != nullptr) {
            readFile(path, input);
            env->ReleaseStringUTFChars(src, path);
        }
    }
    TLottieInstance *instance = input.empty() ? nullptr : createInstance(env, input.data(), input.size(),
            layerNames, layerColors, colorReplacement, fitzModifier);
    if (instance == nullptr) return 0;
    if (!writeMetadata(env, data, instance)) {
        tlottie_drop(instance);
        return 0;
    }
    return reinterpret_cast<jlong>(instance);
}

JNIEXPORT jlong JNICALL Java_org_telegram_ui_Components_RLottieNative_nCreateWithJson(
        JNIEnv *env, jclass, jstring json, jintArray data, jintArray colorReplacement,
        jobjectArray layerNames, jintArray layerColors) {
    if (json == nullptr) return 0;
    const char *chars = env->GetStringUTFChars(json, nullptr);
    if (chars == nullptr) return 0;
    TLottieInstance *instance = createInstance(env, chars, static_cast<size_t>(env->GetStringUTFLength(json)),
            layerNames, layerColors, colorReplacement, 0);
    env->ReleaseStringUTFChars(json, chars);
    if (instance == nullptr) return 0;
    if (!writeMetadata(env, data, instance)) {
        tlottie_drop(instance);
        return 0;
    }
    return reinterpret_cast<jlong>(instance);
}

JNIEXPORT void JNICALL Java_org_telegram_ui_Components_RLottieNative_nDestroy(JNIEnv *, jclass, jlong ptr) {
    tlottie_drop(reinterpret_cast<TLottieInstance *>(ptr));
}

JNIEXPORT jint JNICALL Java_org_telegram_ui_Components_RLottieNative_nGetFrame(
        JNIEnv *env, jclass, jlong ptr, jint frame, jobject bitmap, jboolean clear) {
    if (ptr == 0 || bitmap == nullptr) return 0;
    auto *instance = reinterpret_cast<TLottieInstance *>(ptr);
    AndroidBitmapInfo bitmapInfo;
    if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) != ANDROID_BITMAP_RESULT_SUCCESS ||
        (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
         bitmapInfo.format != ANDROID_BITMAP_FORMAT_A_8)) return 0;
    void *rawPixels = nullptr;
    if (AndroidBitmap_lockPixels(env, bitmap, &rawPixels) < 0) return 0;
    int32_t result;
    if (bitmapInfo.format == ANDROID_BITMAP_FORMAT_A_8) {
        result = renderFitted<uint8_t>(instance, bitmapInfo, rawPixels, clear,
                [&](uint32_t width, uint32_t height, uint8_t *pixels, size_t count) {
                    return tlottie_render_alpha8_with_options(instance, static_cast<float>(frame),
                            width, height, pixels, count, 1, kDefaultCurveTolerance, clear ? 1 : 0);
                });
    } else {
        result = renderFitted<uint32_t>(instance, bitmapInfo, rawPixels, clear,
                [&](uint32_t width, uint32_t height, uint32_t *pixels, size_t count) {
                    return tlottie_render_with_options(instance, static_cast<float>(frame),
                            width, height, pixels, count, 1, kDefaultCurveTolerance, clear ? 1 : 0);
                });
    }
    AndroidBitmap_unlockPixels(env, bitmap);
    return result == TLOTTIE_OK ? frame : -5;
}
}
