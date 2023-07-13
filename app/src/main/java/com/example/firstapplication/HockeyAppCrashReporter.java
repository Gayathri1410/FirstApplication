package com.example.firstapplication;
import static com.example.firstapplication.WatsonStage.kWatsonStageRetryStageTwo;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.microsoft.appcenter.crashes.AbstractCrashesListener;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;
import com.microsoft.appcenter.crashes.ingestion.models.ManagedErrorLog;
import com.microsoft.appcenter.crashes.ingestion.models.json.ErrorAttachmentLogFactory;
import com.microsoft.appcenter.crashes.ingestion.models.json.ManagedErrorLogFactory;
import com.microsoft.appcenter.crashes.utils.ErrorLogHelper;
import com.microsoft.appcenter.ingestion.models.json.DefaultLogSerializer;
import com.microsoft.appcenter.ingestion.models.json.LogSerializer;
import com.microsoft.appcenter.utils.AppCenterLog;
import com.microsoft.appcenter.utils.storage.FileManager;
import com.microsoft.office.crashreporting.CrashUtils;
import com.microsoft.appcenter.crashes.model.ErrorReport;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.json.JSONException;
import org.json.JSONStringer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class HockeyAppCrashReporter {

    private static final String NUMBER_OF_LOGCAT_LINES = "600";
    public static final String LOG_TAG = "HockeyAppCrashReporter";
    private static final String FIELD_URL = "https://nw-umwatson.events.data.microsoft.com/telemetry.request";
    private static final String PARAM_HTTP_METHOD = "POST";
    private static final String PARAM_CONTENT_TYPE = "Content-Type";
private static final String CONTENT_TYPE_VALUE = "application/xml";
    private static final String PARAM_HOST= "Host";
    private static final String PARAM_HOST_VALUE = "watson.telemetry.microsoft.com";
    private static final String PARAM_EXPECT= "Expect";
    private static final String PARAM_EXPECT_VALUE = "100-continue";
    private static final String PARAM_CONNECTION = "CONNECTION";
    private static final String PARAM_CONNECTION_VALUE = "Keep-Alive";
    private static final String PARAM_CONTENT_TYPE_NOTPOST = "Content-Type";
    private static final String CONTENT_TYPE_VALUE_NOTPOST =  "Application/octet-stream";
    private static String m_stageOneHitReceiptBucketID;
    private static String  m_stageOneCabUploadToken;
    private static String mStageTwoCabID;
    private static String  mStageTwoCabGUID;
    private static String m_stageOneHitReceiptBucketHash;
    private static String m_stageOneHitReceiptBucketTBL;
    private static String cabSize;
    public static WatsonStage stage = WatsonStage.kWatsonStageOne;
    private final static String s_ls = System.getProperty("line.separator");
    private static String dumpSystemMetadata(Context context, LinkedHashMap<String, String> metadataMap, String countryLocale, String countryLanguage) {

        String metadata = "";

        try {
            JSONStringer stringer = new JSONStringer();
            stringer.object();
            stringer.key("DeviceId").value(DeviceUtil.getDeviceId(context));

            stringer.key("Board").value(android.os.Build.BOARD);

            stringer.key("BootLoader").value(android.os.Build.BOOTLOADER);

            stringer.key("Brand").value(android.os.Build.BRAND);

            stringer.key("CPU_ABI").value(android.os.Build.CPU_ABI);

            stringer.key("CPU_ABI2").value(android.os.Build.CPU_ABI2);

            stringer.key("Device").value(android.os.Build.DEVICE);

            stringer.key("Display").value(android.os.Build.DISPLAY);

            stringer.key("FingerPrint").value(android.os.Build.FINGERPRINT);

            stringer.key("Hardware").value(android.os.Build.HARDWARE);

            stringer.key("SDK_INT").value(Build.VERSION.SDK_INT);

            stringer.key("Id").value(android.os.Build.ID);

            stringer.key("Manufacturer").value(android.os.Build.MANUFACTURER);

            stringer.key("Model").value(android.os.Build.MODEL);

            stringer.key("Product").value(android.os.Build.PRODUCT);

            stringer.key("Country").value(countryLocale);

            stringer.key("Language").value(countryLanguage);

            stringer.key("Total Memory").value(DeviceUtil.getTotalMemory() + " MB");

            String userId = SharePreferenceKaizalaSHelper.getInstance().getCurrentUserId();

            if (userId != null) {

                userId = ClientUtils.sanitizeUserId(userId);

                stringer.key("UserId").value(userId);

            }
            if (metadataMap != null) {

                for (Map.Entry<String, String> entry : metadataMap.entrySet())

                    stringer.key(entry.getKey()).value(entry.getValue());

            }
            stringer.endObject();

            metadata = getFormattedJsonString(stringer);

        } catch (Exception ex) {

//            LogFile.LogGenericDataToFile(LogLevel.ERROR, LOG_TAG, ex.getMessage());

        }
        return metadata;

    }

    private static String getFormattedJsonString(JSONStringer stringer) {
        // Adding newlines for better readability on the hockey portal.
        return stringer.toString()
                .replaceAll("\\{", "\\{" + s_ls)
                .replaceAll(",", "," + s_ls)
                .replaceAll("\\}", s_ls + "\\}");
    }

    private static String getLocaleCountry() {
        String country = "";
        try {
            country = Locale.getDefault().getISO3Country();
        } catch (MissingResourceException ex) {
            Log.w(LOG_TAG, "Country code not found");
        }
        return country;
    }

    private static String getLocaleLanguage() {
        String language = "";
        try {
            language = Locale.getDefault().getISO3Language();
        } catch (MissingResourceException ex) {
            Log.w(LOG_TAG, "Language code not found");
        }
        return language;
    }

    static class OfficeCrashListener extends AbstractCrashesListener {


        private final WeakReference<Context> mContext;
        private final boolean mUploadAttachments;
        private final String mCountryLocale;
        private final String mCountryLanguage;
        private boolean _crashLogHasBeenProcessed = false;

        private LogSerializer mLogSerializer;

        private boolean stageOneRetried = false;
        private boolean stageTwoRetried = false;


        public OfficeCrashListener(Context context, boolean uploadAttachments) {
            mContext = new WeakReference<>(context);
            mUploadAttachments = uploadAttachments;
            mCountryLocale = getLocaleCountry();
            mCountryLanguage = getLocaleLanguage();
        }

        public ArrayList<String> getCustomAttachments() {
            ArrayList<String> customAttachments = new ArrayList<>(4);
            Context ctx = mContext.get();
            String systemMetadataText = "";
            String logcatLogsText = "";
            if (mUploadAttachments) {
                logcatLogsText = CrashUtils.getLogcatLogs(NUMBER_OF_LOGCAT_LINES);
            }
            if (ctx != null) {
                systemMetadataText = HockeyAppCrashReporter.dumpSystemMetadata(ctx, null, mCountryLocale, mCountryLanguage);
            }
            Collections.addAll(customAttachments, systemMetadataText, CrashUtils.ATTACHMENT_METADATA_EXT,

                    logcatLogsText, CrashUtils.ATTACHMENT_LOGS_EXT);
            return customAttachments;
        }



        public String xmlStringOne(ManagedErrorLog log, ErrorReport errorReport) throws NoSuchAlgorithmException {

            String kStageOneXMLBodyRequest = "<req ver=\"2\"><tlm><src><desc><mach><os><arg nm=\"vermaj\" val=\"%s\" /><arg nm=\"vermin\" val=\"%s\" /><arg nm=\"verbld\" val=\"%s\" /><arg nm=\"arch\" val=\"%s\" /><arg nm=\"lcid\" val=\"%s\" /></os><hw><arg nm=\"sysmfg\" val=\"%s\" /><arg nm=\"syspro\" val=\"%s\" /></hw><ctrl><arg nm=\"tm\" val=\"%s\" /><arg nm=\"mid\" val=\"%s\" /><arg nm=\"sample\" val=\"1\" /><arg nm=\"msft\" val=\"%s\" /><arg nm=\"test\" val=\"%s\" /></ctrl></mach></desc></src><reqs><req key=\"1\"><namespace svc=\"watson\" ptr=\"APEX\" gp=\"Office\" app=\"%s\"></namespace><cmd nm=\"event\"><arg nm=\"cat\" val=\"generic\" /><arg nm=\"eventtype\" val=\"%s\" /><arg nm=\"p1\" val=\"%s\" /><arg nm=\"p2\" val=\"%s\" /><arg nm=\"p3\" val=\"%s\" /><arg nm=\"p4\" val=\"%s\" /><arg nm=\"p5\" val=\"%s\" /></cmd></req></reqs></tlm></req>";
            long timeStamp = (long) (new Date().getTime() / 1000) * 1000;
            String timeStampString = String.format("%.0f", (double) timeStamp);
            String architecture = "0";
            if (log.getArchitecture().equals("arm64")) {
                architecture = "12";
            } else if (log.getArchitecture().equals("x86_64")) {
                architecture = "9";
            }
            String stackHashValue = getStackHash(errorReport);
            String formattedXmlBodyOne = String.format(kStageOneXMLBodyRequest,
                    //Os parameters
                    log.getDevice().getOsVersion(), log.getDevice().getOsVersion(), log.getDevice().getOsBuild(), architecture, log.getDevice().getLocale(),
                    //HW parameters
                    log.getDevice().getOemName(), log.getDevice().getModel(),
                    //CTRL parameters
//                    timeStampString, log.getId(), "1", "1",
                    timeStampString,"{9C91D8D6-F88C-4F7C-81BC-2E622C5BBCEB}", "1", "1",
                    //namespace parameter
                    log.getDevice().getAppNamespace(),
                    //cmd parameters
                    "AndroidAppCrash", log.getDevice().getAppNamespace(), log.getDevice().getAppVersion(), log.getDevice().getAppBuild(), log.getException().getType(), stackHashValue);

            return formattedXmlBodyOne;

        }

        public String xmlStringTwo(ManagedErrorLog log) {


            String kStageTwoXMLBodyRequest = "<req ver=\"2\"><tlm><src><desc><mach><os><arg nm=\"vermaj\" val=\"%@\" /><arg nm=\"vermin\" val=\"%@\" /><arg nm=\"verbld\" val=\"%@\" /><arg nm=\"arch\" val=\"%@\" /><arg nm=\"lcid\" val=\"%u\" /></os><hw><arg nm=\"sysmfg\" val=\"apple\" /><arg nm=\"syspro\" val=\"%@\" /></hw><ctrl><arg nm=\"tm\" val=\"%@\" /><arg nm=\"mid\" val=\"%@\" /><arg nm=\"sample\" val=\"1\" /><arg nm=\"msft\" val=\"%@\" /><arg nm=\"test\" val=\"%@\" /></ctrl></mach></desc></src><reqs><payload><arg nm=\"size\" val=\"%@\" /><arg nm=\"comp\" val=\"zip\" /></payload><req key=\"dflt\"><namespace svc=\"watson\" ptr=\"apex\" gp=\"office\" app=\"%@\" /><cmd nm=\"dataupload\"><arg nm=\"bucket\" val=\"%@\" /><arg nm=\"buckettbl\" val=\"%@\" /><arg nm=\"eventtype\" val=\"%@\" /><arg nm=\"size\" val=\"%@\" /><arg nm=\"token\" val=\"%@\" /></cmd></req></reqs></tlm></req>";
            long timeStamp = (long) (new Date().getTime() / 1000) * 1000;
            String timeStampString = String.format("%.0f", (double) timeStamp);
            String architecture = "0";
            if (log.getArchitecture() == "arm64") {
                architecture = "12";
            } else if (log.getArchitecture() == "x86_64") {
                architecture = "9";
            }

            String formattedXmlBodyTwo = String.format(kStageTwoXMLBodyRequest,
                    //Os parameters
                    log.getDevice().getOsVersion(), log.getDevice().getOsVersion(), log.getDevice().getOsBuild(), architecture, log.getDevice().getLocale(),
                    //HW parameters
                    log.getDevice().getOemName(), log.getDevice().getModel(),
                    //ctrl parameters
//                    timeStampString, log.getId(), "1", "1",
                    timeStampString,"9C91D8D6-F88C-4F7C-81BC-2E622C5BBCEB", "1", "1",
                    //payload elements
                    cabSize,
                    //namespace parameters
                    log.getDevice().getAppNamespace(),
                    //cmd parameters
                    m_stageOneHitReceiptBucketHash, m_stageOneHitReceiptBucketTBL, "AndroidAppCrash", cabSize, m_stageOneCabUploadToken);

            return formattedXmlBodyTwo;
        }

        public int handleHTTPRequestNewProtocol(String method, String urlString, byte[] body, byte[] dataReturned, int encodedLength) throws IOException {
            int httpStatus = 0;
            byte[] responseData = null;
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty(PARAM_HOST, PARAM_HOST_VALUE);
                urlConnection.setRequestProperty(PARAM_EXPECT, PARAM_EXPECT_VALUE);

                if (method != null) {
                    urlConnection.setRequestMethod(method);
                    if (method == PARAM_HTTP_METHOD) {
                        urlConnection.setRequestProperty(PARAM_CONTENT_TYPE, CONTENT_TYPE_VALUE);
                        urlConnection.setRequestProperty(PARAM_CONNECTION, PARAM_CONNECTION_VALUE);
                    } else {
                        urlConnection.setRequestProperty(PARAM_CONTENT_TYPE_NOTPOST, CONTENT_TYPE_VALUE_NOTPOST);
                    }
                }


                if (body != null) {
                    byte[] requestBody = new byte[4 + body.length];
                    System.arraycopy(ByteBuffer.allocate(4).putInt(encodedLength).array(), 0, requestBody, 0, 4);
                    System.arraycopy(body, 0, requestBody, 4, body.length);
                    urlConnection.setRequestProperty("Content-Length", Integer.toString(requestBody.length));

                    try (OutputStream outputStream = urlConnection.getOutputStream()) {
                        outputStream.write(requestBody);
                    }
//                    ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
//                    requestBody.write(ByteBuffer.allocate(4).putInt(encodedLength).array());
//                    requestBody.write(body);
//                    byte[] requestBodyBytes = requestBody.toByteArray();
//                    ByteArrayOutputStream requestBody = new ByteArrayOutputStream();
//                    DataOutputStream dataOutputStream = new DataOutputStream(requestBody);
//                    dataOutputStream.writeInt(encodedLength);
//                    dataOutputStream.write(body);
//                    urlConnection.setRequestProperty(PARAM_CONTENT_LENGTH,String.valueOf(requestBodyBytes) );
//                    urlConnection.setDoInput(true);
//                    urlConnection.setDoOutput(true);
////                    urlConnection.setRequestProperty(PARAM_CONTENT_LENGTH, String.valueOf(body));

                    urlConnection.connect();
                }
                httpStatus = urlConnection.getResponseCode();
                if (httpStatus == HttpURLConnection.HTTP_OK) {
//                    if (dataReturned != null) {
//                        responseData = urlConnection.getInputStream().readAllBytes();
//                   responseData= urlConnection.getInputStream().readAllBytes();
//                        dataReturned[0] = responseData;
//                    }
//                    InputStream inputStream = urlConnection.getInputStream();
//                    responseData = IOUtils.toByteArray(inputStream);
//                    inputStream.close();


                    //////****** DONT DELETE IT*****///
                      String responseBody = "";
                      InputStream inputStream = urlConnection.getInputStream();
                      if (inputStream != null)
                      {
                          Scanner scanner = new Scanner(inputStream);
                          StringBuilder stringBuilder = new StringBuilder();
                          while (scanner.hasNextLine())
                          {
                              stringBuilder.append(scanner.nextLine());
                          }
                          responseBody = stringBuilder.toString();
                      }
                     System.out.println(responseBody);
//                  }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            urlConnection.disconnect();

            if (dataReturned != null) {
                dataReturned = responseData;
            }


            return httpStatus;
        }

            private static byte[] intToBytes(int value) {
                return new byte[] {
                        (byte) (value >>> 24),
                        (byte) (value >>> 16),
                        (byte) (value >>> 8),
                        (byte) value
                };
            }


        private static String getStackHash(ErrorReport errorReport) throws NoSuchAlgorithmException {

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(errorReport.getStackTrace().getBytes());

            // Convert the byte array to hexadecimal representation
            StringBuilder hashBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                hashBuilder.append(String.format("%02x", b));
            }

            return hashBuilder.toString();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public WatsonStage executeStageOneNewProtocol(File curLogFile,ManagedErrorLog log, ErrorReport errorReport) throws IOException, JSONException, NoSuchAlgorithmException {
            byte[] stageOneResponse = null;
            Integer httpResult = 0;
            WatsonStage newStage = WatsonStage.kWatsonStageStarted;
            byte[] requestXMLBody = xmlStringOne(log, errorReport).getBytes(StandardCharsets.UTF_8);
            // Get the encoded length of the requestXMLBody
            int encodedLength = requestXMLBody.length;
//
//             Before doing any kind of reporting, let's check if this app is not under any throttling.
//             If so, don't send a hit request unless it is a Wednesday just in case there's a throttle bypass.
//            Date endThrottleDate = (Date) SharedPreferences.getInstance().getObject(v_exceptionContext.m_appBundleID + "_" + kThrottleDays);
//            int dayOfTheWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
//
//            if (endThrottleDate != null && endThrottleDate.compareTo(new Date()) > 0 && dayOfTheWeek != Calendar.WEDNESDAY) {
//                newStage = kWatsonStageDone;
//                _crashLogHasBeenProcessed = true; //We are throttled, so assume Watson doesn't want the crash log.
//            }
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(new Callable<Integer>() {
                @RequiresApi(api = 33)
                public Integer call() throws Exception {
                    try {
                        return handleHTTPRequestNewProtocol("POST", FIELD_URL, requestXMLBody, stageOneResponse, encodedLength);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            });

            try {
                httpResult = future.get(); // Get the result of the background execution
            } catch (Exception e) {
                httpResult = 0; // Default value or error handling
            }
            executor.shutdown(); // Shutdown the executor

            if (httpResult >= 200 && httpResult < 300) {
                parseStageOneResponseNewProtocol(stageOneResponse);
            }
            if (m_stageOneHitReceiptBucketID != null && (httpResult >= 200 && httpResult < 300)) {
                if (m_stageOneCabUploadToken != null && m_stageOneHitReceiptBucketHash != null && m_stageOneHitReceiptBucketTBL != null) {    // If the response contains a send command item (<cmd nm="send">) along with a token and bucket info, it means we should upload a CAB file and move to stage two.

                    newStage = WatsonStage.kWatsonStageComplete;
                    executeStageTwoNewProtocol(curLogFile, log);
                } else {   // If no token but http result == 200 and the response <cmd nm="receipt"> contains an iBucket, then reporting was successful. We are done.

                    newStage = WatsonStage.kWatsonStageComplete;
                }
            } else if (!stageOneRetried) {
                // If something went wrong, let's give it one more shot.
                newStage = WatsonStage.kWatsonStageRetryStageOne;
                executeStageOneNewProtocol(curLogFile ,log, errorReport);
                stageOneRetried = true;
                _crashLogHasBeenProcessed = true; //Marking this as processed regardless of what happens with our retry. At this point we are done.

            }
            return newStage;
        }

        private static void parseStageOneResponseNewProtocol(byte[] response) {
            m_stageOneHitReceiptBucketID = null;
            m_stageOneCabUploadToken = null;
//            try {
//                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//                DocumentBuilder builder = factory.newDocumentBuilder();
//                InputSource is = new InputSource(new ByteArrayInputStream(response));
//                Document doc = builder.parse(is);
//
//                ParserDelegate parserDelegate = new ParserDelegate();
//                if (parserDelegate.isInvalidRequest()) {
//                    throw new AssertionError("Invalid request, we should not retry. Something is formatted incorrectly.");
//                }
//                else {
//                    m_stageOneHitReceiptBucketID = parserDelegate.getBucketID();
//                }
//                if (parserDelegate.isShouldCollectCab() || parserDelegate.isShouldBypassThrottle()) {
//                    m_stageOneCabUploadToken = parserDelegate.getCabUploadToken();
//                    m_stageOneHitReceiptBucketHash = parserDelegate.getBucketHash();
//                    m_stageOneHitReceiptBucketTBL = parserDelegate.getBuckettbl();
//                }

//            if (parserDelegate.isShouldThrottle()) {
//                // Store the date this app should go back to sending hit requests to Watson.
//                Calendar theCalendar = Calendar.getInstance();
//                theCalendar.add(Calendar.DAY_OF_MONTH, parserDelegate.getThrottleDays());
//                Date endThrottleDate = theCalendar.getTime();
//                UserDefaults.standard.set(endThrottleDate, forKey: "\(v_exceptionContext.m_appBundleID)_\(kThrottleDays)")
//            }
//
//            if (parserDelegate.isShouldBypassThrottle()) {
//                // There was a throttle bypass, get rid of the endThrottleDate as a way to resume sending hits to Watson.
//                // Watson can allways throttle us back at any time.
//                UserDefaults.standard.removeObject(forKey: "\(v_exceptionContext.m_appBundleID)_\(kThrottleDays)")
//            }
            try {
                ParserDelegate parserDelegate = new ParserDelegate();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                if (response != null) {
                    InputSource is = new InputSource(new ByteArrayInputStream(response));
                    saxParser.parse(is, parserDelegate);
                }

               //xml parsing happens here
                if (parserDelegate.isInvalidRequest()) {
                    throw new AssertionError("Invalid request, we should not retry. Something is formatted incorrectly.");
                } else
                {
                    m_stageOneHitReceiptBucketID = parserDelegate.getBucketID();
                }
                if (parserDelegate.isShouldCollectCab() || parserDelegate.isShouldBypassThrottle()) {
                    m_stageOneCabUploadToken = parserDelegate.getCabUploadToken();
                    m_stageOneHitReceiptBucketHash = parserDelegate.getBucketHash();
                    m_stageOneHitReceiptBucketTBL = parserDelegate.getBuckettbl();
                }
//
//            if (parserDelegate.isShouldThrottle()) {
//                // Store the date this app should go back to sending hit requests to Watson.
//                Calendar theCalendar = Calendar.getInstance();
//                theCalendar.add(Calendar.DAY_OF_MONTH, parserDelegate.getThrottleDays());
//                Date endThrottleDate = theCalendar.getTime();
//                UserDefaults.standard.set(endThrottleDate, "\(v_exceptionContext.m_appBundleID)_\(kThrottleDays)");
////                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
////                sharedPreferences.edit().remove("(v_exceptionContext.m_appBundleID)_(kThrottleDays)").apply();
//            }

//            if (parserDelegate.isShouldBypassThrottle()) {
//                // There was a throttle bypass, get rid of the endThrottleDate as a way to resume sending hits to Watson.
//                // Watson can allways throttle us back at any time.
//                UserDefaults.standard.removeObject(forKey: "\(v_exceptionContext.m_appBundleID)_\(kThrottleDays)")
//            }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }

        }

        public WatsonStage executeStageTwoNewProtocol(File curLogFile, ManagedErrorLog log) throws IOException, JSONException {
            byte[] stageTwoResponse = null;
            int httpResult = 0;
            WatsonStage newStage = WatsonStage.kWatsonStageStarted;
            boolean shouldRetryCabUpload = false;
            URL reportURL = new URL(FIELD_URL);
            File logFile = curLogFile;
            FileInputStream fis = new FileInputStream(logFile);
            byte[] fileBytes = new byte[(int) logFile.length()];
            fis.read(fileBytes);
            byte[] cabFile = fileBytes;
            int cabSizeInBytes = fileBytes.length;
            cabSize = Integer.toString(cabSizeInBytes);
            // Create a ByteArrayOutputStream object to hold the request XML body
            ByteArrayOutputStream requestXMLBody = new ByteArrayOutputStream();
// Write the XML body with parameters to the ByteArrayOutputStream object in UTF-8 format
            requestXMLBody.write(xmlStringTwo(log).getBytes(StandardCharsets.UTF_8));
// Calculate the encoded length of the requestXMLBody
            int encodedLength = requestXMLBody.size();
// Append the report data to the end of the requestXMLBody
            requestXMLBody.write(cabFile);
//            httpResult = handleHTTPRequestNewProtocol("PUT", FIELD_URL, requestXMLBody.toByteArray(), stageTwoResponse, encodedLength);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(new Callable<Integer>() {
                @RequiresApi(api = 33)
                public Integer call() throws Exception {
                    try {
                        return handleHTTPRequestNewProtocol("PUT", FIELD_URL, requestXMLBody.toByteArray(), stageTwoResponse, encodedLength);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            });

            try {
                httpResult = future.get(); // Get the result of the background execution
            } catch (Exception e) {
                httpResult = 0; // Default value or error handling
            }
            executor.shutdown();
            if (httpResult >= 200 && httpResult < 300) {
                shouldRetryCabUpload = parseStageTwoResponseNewProtocol(stageTwoResponse);
            }
            if (!shouldRetryCabUpload || (mStageTwoCabID != null && mStageTwoCabGUID != null)) {
                // If we failed to upload but the service is not asking for a retry or if we succeeded uploading, we are done.
                newStage = WatsonStage.kWatsonStageComplete;
//#if MS_TARGET_IOS
                _crashLogHasBeenProcessed = true; //The service didn't ask for a retry, safe to delete the crash log now.
//#endif
            } else if (!stageTwoRetried) {
                newStage = WatsonStage.kWatsonStageRetryStageTwo;
                stageTwoRetried = true;
            }

            return newStage;
        }

        private boolean parseStageTwoResponseNewProtocol(byte[] response) {
            mStageTwoCabID = null;
            mStageTwoCabGUID = null;
            boolean shouldRetryCabUpload = false;

//
//            try {
//                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//                factory.setNamespaceAware(true);
//                XmlPullParser xpp = factory.newPullParser();
//                xpp.setInput(new ByteArrayInputStream(response), null);
//                int eventType = xpp.getEventType();
//                while (eventType != XmlPullParser.END_DOCUMENT) {
//                    if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("CabUploadResponse")) {
//                        String cabID = xpp.getAttributeValue(null, "CabID");
//                        String cabGUID = xpp.getAttributeValue(null, "CabGUID");
//                        String cabUploadFailed = xpp.getAttributeValue(null, "CabUploadFailed");
//                        if (cabUploadFailed == null || !cabUploadFailed.equalsIgnoreCase("true")) {
//                            mStageTwoCabID = cabID;
//                            mStageTwoCabGUID = cabGUID;
//
//                            if (mStageTwoCabID != null && mStageTwoCabGUID != null) {
//                                _crashLogHasBeenProcessed = true;
//                            }
//
//                        } else {
//                            shouldRetryCabUpload = true;
//                        }
//                    }
//                    eventType = xpp.next();
//                }
//            } catch (IOException | XmlPullParserException e) {
//                e.printStackTrace();
//            }
//            return shouldRetryCabUpload;
//
//
//        }
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                ParserDelegate parserDelegate = new ParserDelegate();
                saxParser.parse(new ByteArrayInputStream(response), parserDelegate);

                if (!parserDelegate.isDidCabUploadFailed()) {
                    mStageTwoCabID = parserDelegate.getCabID();
                    mStageTwoCabGUID = parserDelegate.getCabGUID();
                    if (mStageTwoCabID != null && mStageTwoCabGUID != null) {
                        _crashLogHasBeenProcessed = true;
                    }

                } else {
                    shouldRetryCabUpload = parserDelegate.isShouldRetryCabUpload();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return shouldRetryCabUpload;
        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void watsonCode(WatsonStage stage) throws JSONException, IOException, NoSuchAlgorithmException {
            File[] folder = ErrorLogHelper.getStoredErrorLogFiles();
            for (File curLogFile : ErrorLogHelper.getStoredErrorLogFiles()) {
                ErrorReport errorReport = null;
                String logfileContents = FileManager.read(curLogFile);
                mLogSerializer = new DefaultLogSerializer();
                mLogSerializer.addLogFactory(ManagedErrorLog.TYPE, ManagedErrorLogFactory.getInstance());
                mLogSerializer.addLogFactory(ErrorAttachmentLog.TYPE, ErrorAttachmentLogFactory.getInstance());
                ManagedErrorLog log = null;
                try {
                    log = (ManagedErrorLog) mLogSerializer.deserializeLog(logfileContents, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                errorReport = CrashUtils.buildErrorReport(log);

                if (stage == WatsonStage.kWatsonStageOne || stage == WatsonStage.kWatsonStageRetryStageOne) {
                        stage = executeStageOneNewProtocol(curLogFile ,log, errorReport);
                }
                if (stage == WatsonStage.kWatsonStageTwo || stage == WatsonStage.kWatsonStageRetryStageTwo) {
                        executeStageTwoNewProtocol(curLogFile,log);

                }

                if (stage != WatsonStage.kWatsonStageComplete) {
                    watsonCode(stage);
                }
                curLogFile.delete();
                System.out.println(folder);
            }
        }


    }}








