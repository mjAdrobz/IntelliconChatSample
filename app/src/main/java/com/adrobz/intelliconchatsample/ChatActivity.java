package com.adrobz.intelliconchatsample;


import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.ImageCapture;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adrobz.intelliconlibrary.Model.Attachment;
import com.adrobz.intelliconlibrary.Model.ChatMessages;

import com.adrobz.intelliconlibrary.Model.Payload;
import com.adrobz.intelliconlibrary.Model.UserConversation.AllChatMessages;
import com.adrobz.intelliconlibrary.Model.UserConversation.UserConversation;
import com.adrobz.intelliconlibrary.MyLibrary;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "CHATACtivity";
    RecyclerView chatMessageRv;
    LinearLayoutManager chatMessageLayoutManager;
    MaterialButton audioSend;
    ChatAdapter chatAdapter;
    List<AllChatMessages> allChatMessages = new ArrayList<>();
    EditText newMessageEt;
    Button sendMessage;
    ArrayList<ChatMessages> chatMessagesList = new ArrayList<>();
    String token;
    String configs;
    String userId;
    String cId;
    MyLibrary myLibrary = new MyLibrary(ChatActivity.this);
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 367;
    MediaRecorder mRecorder;
    MediaPlayer mediaPlayer;
    String mFileName;
    String cloudUrl;
    String currentPhotoPath;
    final String[] Items = {"Photo", "Video"};
    String selectedCameraOption;
    String filePath;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        token = getIntent().getStringExtra("token");
        configs = getIntent().getStringExtra("configs");
        userId = getIntent().getStringExtra("userId");

        chatMessageRv = findViewById(R.id.chat_message_rv);
        newMessageEt = findViewById(R.id.message_edittext);
        audioSend = findViewById(R.id.audio_send);
        sendMessage = findViewById(R.id.send_btn);
        mRecorder = new MediaRecorder();
        chatMessageLayoutManager = new LinearLayoutManager(this);
        chatAdapter = new ChatAdapter(chatMessagesList, userId, mediaPlayer, myLibrary);
        chatMessageRv.setLayoutManager(chatMessageLayoutManager);
        chatMessageRv.setAdapter(chatAdapter);
        mediaPlayer = new MediaPlayer();

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj = new JSONObject(configs);
            cloudUrl = jsonObj.getString("cloud_store");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        myLibrary.initializeSocket(token,configs);

        myLibrary.letsChatResponse(chatResponse -> {
            Log.d("letsChatResponse", chatResponse);
            if (!chatResponse.isEmpty()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject = new JSONObject(chatResponse);
                    cId = jsonObject.getJSONObject("conversation").getString("cid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        myLibrary.fetchConversationResponse(response -> {
            ChatActivity.this.runOnUiThread(() -> {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                UserConversation userConversation = gson.fromJson(response, UserConversation.class);
                if (userConversation.conversation != null) {
                    cId = String.valueOf(userConversation.conversation.cid);
                    if (!userConversation.getConversation().messages.isEmpty() && userConversation.getConversation().messages != null) {
                        allChatMessages.addAll(userConversation.getConversation().messages);
                        for (int i = 0; i < allChatMessages.size(); i++) {
                            ChatMessages chatMessages = new ChatMessages();
                            Attachment attachment = new Attachment();
                            Payload payload = new Payload();
                            if (userConversation.getConversation().messages.get(i).attachment != null) {
                                attachment.setType(allChatMessages.get(i).getAttachment().getType());
                                payload.setFilename(allChatMessages.get(i).getAttachment().getPayload().filename);
                                payload.setUrl(allChatMessages.get(i).getAttachment().getPayload().getUrl());
                                attachment.setPayload(payload);
                                chatMessages.setAttachment(attachment);
                            }
                            chatMessages.setAuthor(allChatMessages.get(i).getAuthor());
                            chatMessages.setText(allChatMessages.get(i).getText());
                            chatMessages.setOptions(allChatMessages.get(i).getOptions());
                            chatMessages.setCid(String.valueOf(userConversation.conversation.cid));
                            addItemToRecyclerView(chatMessages);
                        }
                    }
                    myLibrary.initiateChat("");
                } else {
                    myLibrary.initiateChat("");
                }
            });

            Log.d("fetchConverResponseOn", response);
        });

        myLibrary.fetchMessage(response -> {
            try {
                Log.d("messageResponse", response);
                String textMessage;
                ArrayList<String> listData = new ArrayList<String>();
                Attachment attachment = new Attachment();
                Payload payload = new Payload();
                JSONObject jsonObject = new JSONObject();

                jsonObject = new JSONObject(response);
                textMessage = jsonObject.getString("text");
                ChatMessages chatMessages = new ChatMessages();
                if (!jsonObject.isNull("attachment")) {
                    attachment.type = jsonObject.getJSONObject("attachment").getString("type");
                    payload.filename = jsonObject.getJSONObject("attachment").getJSONObject("payload").getString("filename");
                    payload.url = jsonObject.getJSONObject("attachment").getJSONObject("payload").getString("url");
                    attachment.setPayload(payload);
                    chatMessages.setAttachment(attachment);
                }
                chatMessages.setAuthor(jsonObject.getString("author"));
                if (jsonObject.getJSONArray("options") != null) {
                    for (int i = 0; i < jsonObject.getJSONArray("options").length(); i++) {
                        listData.add(jsonObject.getJSONArray("options").get(i).toString());
                    }
                }

                chatMessages.setOptions(listData);
                chatMessages.setText(textMessage);
                chatMessages.setCid(cId);

                addItemToRecyclerView(chatMessages);
            } catch (Exception e) {
                Log.d("message exception", e.toString());
            }

        });


        sendMessage.setOnClickListener(v -> {
            if (newMessageEt.getText().toString().isEmpty()) {
                myLibrary.myToast(ChatActivity.this, "Please Enter Message");
            } else {
                ChatMessages chatMessages = new ChatMessages();
                chatMessages.setAuthor(userId);
                chatMessages.setText(newMessageEt.getText().toString());
                chatMessages.setCid(cId);
                myLibrary.sendMessage(newMessageEt.getText().toString(), cId);
            }
        });

        audioSend.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!checkRecorderPermission()) {
                    requestAudioPermission();
                } else {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Toast.makeText(ChatActivity.this, "Recording Audio", Toast.LENGTH_SHORT).show();
                        startRecording();

                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        //Toast.makeText(ChatActivity.this, "Audio is saved", Toast.LENGTH_SHORT).show();
                        pauseRecording();

                    }
                }
                return false;
            }
        });

        myLibrary.fetchUserConversation(userId, response -> {
            Log.d("fetchConverResponseEmit", response);
        });

    }

    private void pauseRecording() {
        File audioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudioRecording1.3gp");
        String audioFileName;
//        String audioFileURL = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
        mRecorder.reset();
        mRecorder.release();
        try {
            mRecorder.stop();
        } catch (RuntimeException stopException) {
            // handle cleanup here
        }
        int cut = mFileName.lastIndexOf('/');
        if (cut != -1)
            mFileName = mFileName.substring(cut + 1);
        audioFileName = mFileName;
        String extension = audioFileName.substring(audioFileName.lastIndexOf("."));
        Log.d("AudioFileName", audioFileName);
        uploadAttachment(audioFile, "audio", audioFileName, extension);
        //myLibrary.sendAttachment(cId, "audio", audioFileName, audioFileURL);
        mRecorder = null;

    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/AudioRecording1.3gp";
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mFileName);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addItemToRecyclerView(ChatMessages chatMessages) {
        ChatActivity.this.runOnUiThread(() -> {
            chatMessagesList.add(chatMessages);
            chatAdapter.notifyItemInserted(chatMessagesList.size() - 1);
            chatMessageRv.smoothScrollToPosition(chatMessagesList.size() - 1);
            newMessageEt.setText("");
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.imageMenuBtn) {
            if (isPermissionGranted()) {
                pickImage();
            } else {
                takePermission();
            }


        } else if (item.getItemId() == R.id.videoMenuBtn) {
            if (!checkCameraPermission()) {
                requestCameraPermission();
            } else {
                pickImageFromCamera();
            }
        } else if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestAudioPermission() {
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);

    }

    private boolean checkRecorderPermission() {
        boolean request = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean request1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return request && request1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
    }

    private boolean checkCameraPermission() {
        boolean request = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean request1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return request && request1;
    }

    public static String getFileName(Uri uri, Context context) {
        String result;

        //if uri is content
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    //local filesystem
                    int index = cursor.getColumnIndex("_data");
                    if (index == -1)
                        //google drive
                        index = cursor.getColumnIndex("_display_name");
                    result = cursor.getString(index);
                    if (result != null)
                        uri = Uri.parse(result);
                    else
                        return null;
                }
            } finally {
                cursor.close();
            }
        }

        result = uri.getPath();

        //get filename + ext of path
        int cut = result.lastIndexOf('/');
        if (cut != -1)
            result = result.substring(cut + 1);
        return result;
    }

    //
    public void uploadAttachment(File attachmentFile, String fileType, String fileName, String extension) {
        ApiInterface service = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("*/*"),
                        attachmentFile
                );

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", attachmentFile.getName(), requestFile);

        Call<AttachmentResponse> call = service.uploadAttachment(cloudUrl, body);

        call.enqueue(new Callback<AttachmentResponse>() {
            @Override
            public void onResponse(@NonNull Call<AttachmentResponse> call, @NonNull Response<AttachmentResponse> response) {
                assert response.body() != null;
                myLibrary.sendAttachment(cId, fileType, fileName, response.body().url);
                Log.d("attachmentURl", response.body().url);
                // Toast.makeText(ChatActivity.this, response.body().url, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<AttachmentResponse> call, @NonNull Throwable t) {
                Log.d("attachment Error", t.getMessage());
                Toast.makeText(ChatActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isPermissionGranted() {
        if (SDK_INT == Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return readExternalStorage == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void takePermission() {
        if (SDK_INT == Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 100);
            } catch (Exception exception) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                if (SDK_INT == Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        if (requestCode == 102) {
                            pickImage();
                        } else {
                            pickImageFromCamera();
                        }

                    } else {
                        takePermission();
                    }
                }
            } else if (requestCode == 102) {
                if (data != null) {
                    Uri resultUri = data.getData();
                    if (resultUri != null) {
                        String mimeType = FileUtils.getMimeType(ChatActivity.this, resultUri);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (resultUri.toString().contains("gallery")) {
                                if (mimeType.toLowerCase().contains("image")) {
                                    uploadFileAndSendToSocket(resultUri, "image");
                                } else if (mimeType.toLowerCase().contains("video")) {
                                    uploadFileAndSendToSocket(resultUri, "video");
                                }
                            } else {
                                if (resultUri.toString().contains("image")) {
                                    uploadFileAndSendToSocket(resultUri, "image");
                                } else if (resultUri.toString().contains("video")) {
                                    uploadFileAndSendToSocket(resultUri, "video");
                                } else {
                                    String path = FileUtils.getPath(ChatActivity.this, resultUri);
                                    File file = new File(path);
                                    String mime = FileUtils.getMimeType(file);
                                    if (mime.toLowerCase().contains("image")) {
                                        uploadFileAndSendToSocket(resultUri, "image");
                                    } else if (mime.toLowerCase().contains("video")) {
                                        uploadFileAndSendToSocket(resultUri, "video");
                                    } else {
                                        uploadFileAndSendToSocket(resultUri, "document");
                                    }

                                }
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (mimeType.contains("image")) {
                                uploadFileAndSendToSocket(resultUri, "image");
                            } else if (mimeType.contains("video")) {
                                uploadFileAndSendToSocket(resultUri, "video");
                            } else {
                                uploadFileAndSendToSocket(resultUri, "document");
                            }
                        } else {
                            if (mimeType.contains("image")) {
                                uploadFileAndSendToSocket(resultUri, "image");
                            } else if (mimeType.contains("video")) {
                                uploadFileAndSendToSocket(resultUri, "video");
                            } else {
                                uploadFileAndSendToSocket(resultUri, "document");
                            }
                        }


                    }
                }

            } else if (requestCode == 1) {
                File file = new File(currentPhotoPath);
                int cut = currentPhotoPath.lastIndexOf('/');
                if (cut != -1)
                    currentPhotoPath = currentPhotoPath.substring(cut + 1);
//                Toast.makeText(this, currentPhotoPath, Toast.LENGTH_SHORT).show();
                uploadAttachment(file, selectedCameraOption, currentPhotoPath, "");
                selectedCameraOption = null;

            }

        }
    }

    public void uploadFileAndSendToSocket(Uri resultUri, String fileType) {
        String path = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path = FileUtils.getPath(ChatActivity.this, resultUri);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            path = new NewFileUtils(ChatActivity.this).getPath(resultUri);
        } else {
            path = FileUtils.getPath(ChatActivity.this, resultUri);
        }
//        String path = FileUtils.getPath(ChatActivity.this, resultUri);
        File file = new File(path);
        String fileName = getFileName(resultUri, this);
        uploadAttachment(file, fileType, fileName, "");
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String sel;
        Cursor cursor;
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA, MediaStore.Video.Media.DATA};
//|| uri.toString().contains("document")
        // where id is equal to
        if (uri.toString().contains("image")) {
            sel = MediaStore.Images.Media._ID + "=?";
            cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);
        } else {
            sel = MediaStore.Video.Media._ID + "=?";
            cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);
        }

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        Log.d("realPath", filePath);
        return filePath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (requestCode == 101) {
                boolean readExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (readExternalStorage) {
                    pickImage();
                } else {
                    takePermission();
                }
            } else if (requestCode == 1) {
                boolean readExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (readExternalStorage) {
                    pickImageFromCamera();
                } else {
                    takePermission();
                }
            }
        }
    }


    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //String[] mimetypes = {"image/*", "video/*","application/msword"};
        intent.setType("*/*");
        //intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 102);
    }

    private void pickImageFromCamera() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Choose Camera Option");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Take Photo",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        selectedCameraOption = "image";
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // Ensure that there's a camera activity to handle the intent
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                                        "com.adrobz.intelliconchatsample.fileprovider",
                                        photoFile);

                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                                startActivityForResult(takePictureIntent, 1);
                            }
                        }
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Take Video",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        selectedCameraOption = "video";
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        // Ensure that there's a camera activity to handle the intent
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createVideoFile();
                            } catch (IOException ex) {
                                // Error occurred while creating the File
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                                        "com.adrobz.intelliconchatsample.fileprovider",
                                        photoFile);

                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                                startActivityForResult(takePictureIntent, 1);
                            }
                        }
                        dialog.cancel();
                    }
                });
        builder1.show();

    }

    private File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File video = File.createTempFile(
                videoFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = video.getAbsolutePath();
        return video;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onBackPressed() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        finishAffinity();
        super.onBackPressed();
    }
}