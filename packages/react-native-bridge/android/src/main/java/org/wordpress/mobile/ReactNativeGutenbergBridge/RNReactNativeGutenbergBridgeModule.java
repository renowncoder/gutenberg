package org.wordpress.mobile.ReactNativeGutenbergBridge;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.wordpress.mobile.ReactNativeGutenbergBridge.GutenbergBridgeJS2Parent.GutenbergUserEvent;
import org.wordpress.mobile.ReactNativeGutenbergBridge.GutenbergBridgeJS2Parent.MediaType;
import org.wordpress.mobile.ReactNativeGutenbergBridge.GutenbergBridgeJS2Parent.OtherMediaOptionsReceivedCallback;
import org.wordpress.mobile.ReactNativeGutenbergBridge.GutenbergBridgeJS2Parent.StarterPageTemplatesTooltipShownCallback;
import org.wordpress.mobile.WPAndroidGlue.DeferredEventEmitter;
import org.wordpress.mobile.WPAndroidGlue.MediaOption;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RNReactNativeGutenbergBridgeModule extends ReactContextBaseJavaModule implements
        DeferredEventEmitter.JSEventEmitter {
    private final ReactApplicationContext mReactContext;
    private final GutenbergBridgeJS2Parent mGutenbergBridgeJS2Parent;

    private static final String EVENT_NAME_REQUEST_GET_HTML = "requestGetHtml";
    private static final String EVENT_NAME_UPDATE_HTML = "updateHtml";
    private static final String EVENT_NAME_UPDATE_TITLE = "setTitle";
    private static final String EVENT_NAME_FOCUS_TITLE = "setFocusOnTitle";
    private static final String EVENT_NAME_MEDIA_APPEND = "mediaAppend";
    private static final String EVENT_NAME_TOGGLE_HTML_MODE = "toggleHTMLMode";
    private static final String EVENT_NAME_NOTIFY_MODAL_CLOSED = "notifyModalClosed";
    private static final String EVENT_NAME_PREFERRED_COLOR_SCHEME = "preferredColorScheme";
    private static final String EVENT_NAME_MEDIA_REPLACE_BLOCK = "replaceBlock";
    private static final String EVENT_NAME_UPDATE_THEME = "updateTheme";

    private static final String MAP_KEY_UPDATE_HTML = "html";
    private static final String MAP_KEY_UPDATE_TITLE = "title";
    public static final String MAP_KEY_MEDIA_FILE_UPLOAD_MEDIA_ID = "mediaId";
    public static final String MAP_KEY_MEDIA_FILE_UPLOAD_MEDIA_URL = "mediaUrl";
    public static final String MAP_KEY_MEDIA_FILE_UPLOAD_MEDIA_TYPE = "mediaType";
    private static final String MAP_KEY_THEME_UPDATE_COLORS = "colors";
    private static final String MAP_KEY_THEME_UPDATE_GRADIENTS = "gradients";

    private static final String MAP_KEY_IS_PREFERRED_COLOR_SCHEME_DARK = "isPreferredColorSchemeDark";

    private static final String MEDIA_SOURCE_MEDIA_LIBRARY = "SITE_MEDIA_LIBRARY";
    private static final String MEDIA_SOURCE_DEVICE_LIBRARY = "DEVICE_MEDIA_LIBRARY";
    private static final String MEDIA_SOURCE_DEVICE_CAMERA = "DEVICE_CAMERA";

    private static final String MAP_KEY_REPLACE_BLOCK_HTML = "html";
    private static final String MAP_KEY_REPLACE_BLOCK_BLOCK_ID = "clientId";

    private boolean mIsDarkMode;

    public RNReactNativeGutenbergBridgeModule(ReactApplicationContext reactContext,
            GutenbergBridgeJS2Parent gutenbergBridgeJS2Parent, boolean isDarkMode) {
        super(reactContext);
        mIsDarkMode = isDarkMode;
        mReactContext = reactContext;
        mGutenbergBridgeJS2Parent = gutenbergBridgeJS2Parent;
    }

    @Override
    public String getName() {
        return "RNReactNativeGutenbergBridge";
    }

    @Override
    public Map<String, Object> getConstants() {
        final HashMap<String, Object> constants = new HashMap<>();
        constants.put("isInitialColorSchemeDark", mIsDarkMode);
        return constants;
    }

    @Override
    public void emitToJS(String eventName, @Nullable WritableMap data) {
        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, data);
    }

    public void getHtmlFromJS() {
        emitToJS(EVENT_NAME_REQUEST_GET_HTML, null);
    }

    public void setHtmlInJS(String html) {
        WritableMap writableMap = new WritableNativeMap();
        writableMap.putString(MAP_KEY_UPDATE_HTML, html);
        emitToJS(EVENT_NAME_UPDATE_HTML, writableMap);
    }

    public void setTitleInJS(String title) {
        WritableMap writableMap = new WritableNativeMap();
        writableMap.putString(MAP_KEY_UPDATE_TITLE, title);
        emitToJS(EVENT_NAME_UPDATE_TITLE, writableMap);
    }

    public void setFocusOnTitleInJS() {
        WritableMap writableMap = new WritableNativeMap();
        emitToJS(EVENT_NAME_FOCUS_TITLE, writableMap);
    }

    public void appendNewMediaBlock(int mediaId, String mediaUri, String mediaType) {
        WritableMap writableMap = new WritableNativeMap();
        writableMap.putString(MAP_KEY_MEDIA_FILE_UPLOAD_MEDIA_TYPE, mediaType);
        writableMap.putString(MAP_KEY_MEDIA_FILE_UPLOAD_MEDIA_URL, mediaUri);
        writableMap.putInt(MAP_KEY_MEDIA_FILE_UPLOAD_MEDIA_ID, mediaId);
        emitToJS(EVENT_NAME_MEDIA_APPEND, writableMap);
    }

    public void setPreferredColorScheme(boolean isDarkMode) {
        WritableMap writableMap = new WritableNativeMap();
        writableMap.putBoolean(MAP_KEY_IS_PREFERRED_COLOR_SCHEME_DARK, isDarkMode);
        emitToJS(EVENT_NAME_PREFERRED_COLOR_SCHEME, writableMap);
    }

    public void updateTheme(@Nullable Bundle editorTheme) {
        if (editorTheme == null) return;

        WritableMap writableMap = new WritableNativeMap();
        Serializable colors = editorTheme.getSerializable(MAP_KEY_THEME_UPDATE_COLORS);
        Serializable gradients = editorTheme.getSerializable(MAP_KEY_THEME_UPDATE_GRADIENTS);

        if (colors != null) {
            writableMap.putArray(MAP_KEY_THEME_UPDATE_COLORS, Arguments.fromList((ArrayList)colors));
        }

        if (gradients != null) {
            writableMap.putArray(MAP_KEY_THEME_UPDATE_GRADIENTS, Arguments.fromList((ArrayList)gradients));
        }

        emitToJS(EVENT_NAME_UPDATE_THEME, writableMap);
    }

    @ReactMethod
    public void provideToNative_Html(String html, String title, boolean changed, ReadableMap contentInfo) {
        mGutenbergBridgeJS2Parent.responseHtml(title, html, changed, contentInfo);
    }

    @ReactMethod
    public void editorDidMount(ReadableArray unsupportedBlockNames) {
        mGutenbergBridgeJS2Parent.editorDidMount(unsupportedBlockNames);
    }

    @ReactMethod
    public void requestMediaPickFrom(String mediaSource, ReadableArray filter, Boolean allowMultipleSelection, final Callback onMediaSelected) {
        MediaType mediaType = getMediaTypeFromFilter(filter);
        if (mediaSource.equals(MEDIA_SOURCE_MEDIA_LIBRARY)) {
            mGutenbergBridgeJS2Parent.requestMediaPickFromMediaLibrary(getNewMediaSelectedCallback(allowMultipleSelection, onMediaSelected), allowMultipleSelection, mediaType);
        } else if (mediaSource.equals(MEDIA_SOURCE_DEVICE_LIBRARY)) {
            mGutenbergBridgeJS2Parent.requestMediaPickFromDeviceLibrary(getNewMediaSelectedCallback(allowMultipleSelection, onMediaSelected), allowMultipleSelection, mediaType);
        } else if (mediaSource.equals(MEDIA_SOURCE_DEVICE_CAMERA)) {
            mGutenbergBridgeJS2Parent.requestMediaPickerFromDeviceCamera(getNewMediaSelectedCallback(allowMultipleSelection, onMediaSelected), mediaType);
        } else {
            mGutenbergBridgeJS2Parent.requestMediaPickFrom(mediaSource, getNewMediaSelectedCallback(allowMultipleSelection, onMediaSelected), allowMultipleSelection);
        }
    }

    private MediaType getMediaTypeFromFilter(ReadableArray filter) {
        switch (filter.size()) {
            case 1:
                return MediaType.getEnum(filter.getString(0));
            case 2:
                MediaType filter0 = MediaType.getEnum(filter.getString(0));
                MediaType filter1 = MediaType.getEnum(filter.getString(1));

                if ((filter0.equals(MediaType.VIDEO) && filter1.equals(MediaType.IMAGE))
                    || (filter0.equals(MediaType.IMAGE) && filter1.equals(MediaType.VIDEO))) {
                    return MediaType.MEDIA;
                }
            default:
                return MediaType.OTHER;
        }
    }

    @ReactMethod
    public void requestMediaImport(String url, final Callback onUploadMediaSelected) {
        mGutenbergBridgeJS2Parent.requestMediaImport(url, getNewMediaSelectedCallback(false, onUploadMediaSelected));
    }

    @ReactMethod
    public void mediaUploadSync() {
        mGutenbergBridgeJS2Parent.mediaUploadSync(getNewMediaSelectedCallback(false,null));
    }

    @ReactMethod
    public void requestImageFailedRetryDialog(final int mediaId) {
        mGutenbergBridgeJS2Parent.requestImageFailedRetryDialog(mediaId);
    }

    @ReactMethod
    public void requestImageUploadCancelDialog(final int mediaId) {
        mGutenbergBridgeJS2Parent.requestImageUploadCancelDialog(mediaId);
    }

    @ReactMethod
    public void requestImageUploadCancel(final int mediaId) {
        mGutenbergBridgeJS2Parent.requestImageUploadCancel(mediaId);
    }

    @ReactMethod
    public void requestImageFullscreenPreview(String mediaUrl) {
        mGutenbergBridgeJS2Parent.requestImageFullscreenPreview(mediaUrl);
    }

    @ReactMethod
    public void requestMediaEditor(String mediaUrl, final Callback onUploadMediaSelected) {
        mGutenbergBridgeJS2Parent.requestMediaEditor(getNewMediaSelectedCallback(false, onUploadMediaSelected), mediaUrl);
    }

    @ReactMethod
    public void requestStoryCreatorLoad(ReadableArray mediaFiles, String blockId) {
        mGutenbergBridgeJS2Parent.requestStoryCreatorLoad((savedMediaFiles, savedBlockId) ->
                replaceBlock(savedMediaFiles, savedBlockId), mediaFiles, blockId);
    }

    @ReactMethod
    public void editorDidEmitLog(String message, int logLevel) {
        mGutenbergBridgeJS2Parent.editorDidEmitLog(message, GutenbergBridgeJS2Parent.LogLevel.valueOf(logLevel));
    }

    @ReactMethod
    public void editorDidAutosave() {
        mGutenbergBridgeJS2Parent.editorDidAutosave();
    }

    @ReactMethod
    public void getOtherMediaOptions(ReadableArray filter, final Callback jsCallback) {
        OtherMediaOptionsReceivedCallback otherMediaOptionsReceivedCallback = getNewOtherMediaReceivedCallback(jsCallback);
        MediaType mediaType = getMediaTypeFromFilter(filter);
        mGutenbergBridgeJS2Parent.getOtherMediaPickerOptions(otherMediaOptionsReceivedCallback, mediaType);
    }

    @ReactMethod
    public void fetchRequest(String path, Promise promise) {
        mGutenbergBridgeJS2Parent.performRequest(path,
                promise::resolve,
                errorBundle -> {
                    WritableMap writableMap = Arguments.makeNativeMap(errorBundle);
                    if (writableMap.hasKey("code")) {
                        String code = String.valueOf(writableMap.getInt("code"));
                        promise.reject(code, new Error(), writableMap);
                    } else {
                        promise.reject(new Error(), writableMap);
                    }
                });
    }

    @ReactMethod
    public void logUserEvent(String eventName, ReadableMap eventProperties) {
        mGutenbergBridgeJS2Parent.logUserEvent(GutenbergUserEvent.getEnum(eventName), eventProperties);
    }

    @ReactMethod
    public void requestUnsupportedBlockFallback(String content, String blockId, String blockName, String blockTitle) {
        mGutenbergBridgeJS2Parent.gutenbergDidRequestUnsupportedBlockFallback((savedContent, savedBlockId) ->
                replaceBlock(savedContent, savedBlockId), content, blockId, blockName, blockTitle);
    }

    @ReactMethod
    public void actionButtonPressed(String buttonType) {
        mGutenbergBridgeJS2Parent.gutenbergDidSendButtonPressedAction(buttonType);
    }

    private void replaceBlock(String content, String blockId) {
        WritableMap writableMap = new WritableNativeMap();
        writableMap.putString(MAP_KEY_REPLACE_BLOCK_HTML, content);
        writableMap.putString(MAP_KEY_REPLACE_BLOCK_BLOCK_ID, blockId);
        emitToJS(EVENT_NAME_MEDIA_REPLACE_BLOCK, writableMap);
    }

    private OtherMediaOptionsReceivedCallback getNewOtherMediaReceivedCallback(final Callback jsCallback) {
        return new OtherMediaOptionsReceivedCallback() {
            @Override public void onOtherMediaOptionsReceived(ArrayList<MediaOption> mediaOptions) {
                WritableArray writableArray = new WritableNativeArray();
                for (MediaOption mediaOption : mediaOptions) {
                    writableArray.pushMap(mediaOption.toMap());
                }
                jsCallback.invoke(writableArray);
            }
        };
    }

    @ReactMethod
    public void addMention(Promise promise) {
        mGutenbergBridgeJS2Parent.onAddMention(promise::resolve);
    }

    @ReactMethod
    public void setStarterPageTemplatesTooltipShown(boolean tooltipShown) {
        mGutenbergBridgeJS2Parent.setStarterPageTemplatesTooltipShown(tooltipShown);
    }

    @ReactMethod
    public void requestStarterPageTemplatesTooltipShown(final Callback jsCallback) {
        StarterPageTemplatesTooltipShownCallback starterPageTemplatesTooltipShownCallback = requestStarterPageTemplatesTooltipShownCallback(jsCallback);
        mGutenbergBridgeJS2Parent.requestStarterPageTemplatesTooltipShown(starterPageTemplatesTooltipShownCallback);
    }

    private StarterPageTemplatesTooltipShownCallback requestStarterPageTemplatesTooltipShownCallback(final Callback jsCallback) {
        return new StarterPageTemplatesTooltipShownCallback() {
            @Override public void onRequestStarterPageTemplatesTooltipShown(boolean tooltipShown) {
                jsCallback.invoke(tooltipShown);
            }
        };
    }

    private GutenbergBridgeJS2Parent.MediaSelectedCallback getNewMediaSelectedCallback(final Boolean allowMultipleSelection, final Callback jsCallback) {
        return new GutenbergBridgeJS2Parent.MediaSelectedCallback() {
            @Override
            public void onMediaFileSelected(List<RNMedia> mediaList) {
                if (allowMultipleSelection) {
                    WritableArray writableArray = new WritableNativeArray();
                    for (RNMedia media : mediaList) {
                        writableArray.pushMap(media.toMap());
                    }
                    jsCallback.invoke(writableArray);
                } else if (!mediaList.isEmpty()) {
                    jsCallback.invoke(mediaList.get(0).toMap());
                } else {
                    // if we have no media (e.g. when a content provider throws an exception during file copy), invoke
                    // the js callback with no arguments
                    jsCallback.invoke();
                }
            }

        };
    }


    public void toggleEditorMode() {
        emitToJS(EVENT_NAME_TOGGLE_HTML_MODE, null);
    }

    public void notifyModalClosed() {
        emitToJS(EVENT_NAME_NOTIFY_MODAL_CLOSED, null);
    }
}