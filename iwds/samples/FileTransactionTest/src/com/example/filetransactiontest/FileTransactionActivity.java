package com.example.filetransactiontest;

import java.io.File;
import java.io.FileNotFoundException;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.FileInfo;
import com.ingenic.iwds.datatransactor.FileTransactionModel;
import com.ingenic.iwds.datatransactor.FileTransferErrorCode;
import com.ingenic.iwds.datatransactor.FileTransactionModel.FileTransactionModelCallback;
import com.ingenic.iwds.utils.IwdsAssert;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 演示用 FileTransactionModel 在两个设备之间（手机和手表之间）传输文件。 发送端的文件路径为
 * /sdcard/Download/test.zip，此文件必须存在。接收端 文件自动存到 /sdcard/iwds/ 文件夹。
 * 
 * 1. 两个设备先用 start 启动 FileTransactionModel 2. 通过 onLinkConnected
 * 监控设备间连接状态，通过onChannelAvailable 监控蓝牙数据传输通道。 如果设备已经连接，数据传输通道有效，就可以传输数据了。 3.
 * 发送端设备中用requestSendFile() 请求发送文件。 4. 接收端设备在 onRequestSendFile()
 * 确认是否接受该文件。用notifyConfirmForReceiveFile 确认接收文件，用 notifyCancelForReceiveFile
 * 取消文件接收。 5.发送端设备收到 ConfirmForReceiveFile时发送文件，收到 CancelForReceiveFile则取消文件发送。
 * 6.文件传输结束后，发送端和接收端都会发生 onSendResult，用以通知传输成功或失败。接收端发生 onFileArrived，通知文件接收完成。
 *
 * 注意：需要在两个设备上同时安装这个应用
 */
public class FileTransactionActivity extends Activity implements
        FileTransactionModelCallback {
    private final static String SEND_FILE_PATH = "/sdcard/Download/test.zip";
    private final static String RECV_FILE_PATH = "/sdcard/iwds/test.zip";

    private static final String INTERRUPTED_SEND_INDEX = "send_index";
    private static final String INTERRUPTED_RECV_INDEX = "recv_index";

    private final static int MSG_SEND_OK = 0;
    private final static int MSG_SEND_ERROR = 1;
    private final static int MSG_DATA_CHANNEL_AVAILABLE = 2;
    private final static int MAS_DATA_CHANNEL_UNAVAILABLE = 3;
    private final static int MSG_RECV_OK = 4;
    private final static int MSG_SENDING = 5;
    private final static int MSG_CONNECTING = 6;
    private final static int MSG_DISCONNECTING = 7;
    private final static int MSG_SEND_DONE = 8;
    private final static int MSG_SEND_PROGRESS = 9;
    private final static int MSG_RECV_PROGRESS = 10;

    private final static String UUID = "a1dc19e2-17a4-0797-9362-68a0dd4bfb69";

    private FileTransactionModel mFileTransactionModel;

    private int mRecvCounter;
    private int mSendCounter;
    private int mSendNum;
    private long mSendFileSize;
    private long mRecvFileSize;

    private File mSendFile;
    private File mRecvFile;

    private int mSendIndex;
    private int mChunkSize = 64 * 1024;

    private long mSendStartTime;
    private long mSendEndTime;

    private EditText mIterationsEdit;
    private Button mSendButton;
    private Button mConnectButton;
    private Button mDisconnectButton;
    private Button mClearLogButton;
    private TextView mLogView;
    private ScrollView mScrollView;
    private TextView mSendProgressView;
    private TextView mRecvProgressView;

    private SharedPreferences mSettingsPreference;
    private Editor mSettingsEditor;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            case MSG_SEND_OK:
                mSendEndTime = System.currentTimeMillis();
                mLogView.append("No." + ++mSendCounter + " size: "
                        + getFileSendSize() + " bytes" + ", consume time: "
                        + (mSendEndTime - mSendStartTime) + "ms" + " speed: "
                        + (float) getFileSendSize() * 1000 / 1024
                        / (mSendEndTime - mSendStartTime) + "KB/s "
                        + "\nsend ok\n");

                saveSendIndex(0);
                mSendIndex = 0;

                if (mSendNum > 0 && mSendCounter < mSendNum) {
                    try {
                        mFileTransactionModel.requestSendFile(SEND_FILE_PATH,
                                getSendIndex());

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                } else if (mSendCounter == mSendNum) {
                    mHandler.obtainMessage(MSG_SEND_DONE).sendToTarget();
                }
                break;

            case MSG_SEND_ERROR:
                mSendEndTime = System.currentTimeMillis();
                mLogView.append("No." + ++mSendCounter + " size: "
                        + (getSendIndex() - mSendIndex) * mChunkSize + " bytes"
                        + ", consume time: " + (mSendEndTime - mSendStartTime)
                        + "ms" + " speed: "
                        + (float) ((getSendIndex() - mSendIndex) * mChunkSize)
                        * 1000 / 1024 / (mSendEndTime - mSendStartTime)
                        + "KB/s " + "\nsend fail\n");
                mSendStartTime = System.currentTimeMillis();
                mSendIndex = getSendIndex();
                break;

            case MSG_RECV_OK:
                saveRecvIndex(0);
                mLogView.append("No." + ++mRecvCounter + " size: "
                        + getFileRecvSize() + " bytes" + " recv ok\n");
                break;

            case MSG_DATA_CHANNEL_AVAILABLE:
                mConnectButton.setEnabled(false);
                mSendButton.setEnabled(true);
                mDisconnectButton.setEnabled(true);
                mIterationsEdit.setEnabled(true);

                Toast.makeText(FileTransactionActivity.this,
                        getString(R.string.data_channel_available),
                        Toast.LENGTH_SHORT).show();
                break;

            case MAS_DATA_CHANNEL_UNAVAILABLE:
                Toast.makeText(FileTransactionActivity.this,
                        getString(R.string.data_channel_unavailable),
                        Toast.LENGTH_SHORT).show();
                mConnectButton.setEnabled(true);
                mSendButton.setEnabled(false);
                mDisconnectButton.setEnabled(false);
                mIterationsEdit.setEnabled(false);
                break;

            case MSG_SENDING:
                mConnectButton.setEnabled(false);
                mSendButton.setEnabled(false);
                mDisconnectButton.setEnabled(true);
                mIterationsEdit.setEnabled(false);
                break;

            case MSG_CONNECTING:
                mConnectButton.setEnabled(false);
                mSendButton.setEnabled(false);
                mDisconnectButton.setEnabled(false);
                mIterationsEdit.setEnabled(false);
                break;

            case MSG_DISCONNECTING:
                mDisconnectButton.setEnabled(false);
                mSendButton.setEnabled(false);
                mConnectButton.setEnabled(false);
                mIterationsEdit.setEnabled(false);
                break;

            case MSG_SEND_DONE:
                mDisconnectButton.setEnabled(true);
                mSendButton.setEnabled(true);
                mIterationsEdit.setEnabled(true);
                mConnectButton.setEnabled(false);
                break;

            case MSG_SEND_PROGRESS:
                mSendProgressView.setText("Send progress: " + msg.arg1);
                break;

            case MSG_RECV_PROGRESS:
                mRecvProgressView.setText("Recv progress: " + msg.arg1);
                break;

            default:
                IwdsLog.e(FileTransactionActivity.this, "error message");
                break;
            }

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    };

    private void saveSendIndex(int index) {
        mSettingsEditor.putInt(INTERRUPTED_SEND_INDEX, index);
        mSettingsEditor.commit();
    }

    private void saveRecvIndex(int index) {
        mSettingsEditor.putInt(INTERRUPTED_RECV_INDEX, index);
        mSettingsEditor.commit();
    }

    private int getFileRecvSize() {
        return (int) (mRecvFileSize - getRecvIndex() * mChunkSize);
    }

    private int getFileSendSize() {
        return (int) (mSendFileSize - getSendIndex() * mChunkSize);
    }

    private int getSendIndex() {
        return mSettingsPreference.getInt(INTERRUPTED_SEND_INDEX, 0);
    }

    private int getRecvIndex() {
        return mSettingsPreference.getInt(INTERRUPTED_RECV_INDEX, 0);
    }

    private void showDialog(int titleId, String name, int length) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleId);
        builder.setCancelable(false);
        builder.setMessage(FileTransactionActivity.this
                .getString(R.string.file_name)
                + ": "
                + name
                + "\n"
                + FileTransactionActivity.this.getString(R.string.file_size)
                + ": " + length + "bytes");

        builder.setPositiveButton(R.string.confirm,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mFileTransactionModel.notifyConfirmForReceiveFile();
                    }
                });

        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFileTransactionModel.notifyCancelForReceiveFile();
                    }
                });
        builder.create().show();
    }

    private void buildView() {
        mIterationsEdit = (EditText) findViewById(R.id.iterations_edit);
        mSendButton = (Button) findViewById(R.id.send_button);
        mConnectButton = (Button) findViewById(R.id.connect_button);
        mDisconnectButton = (Button) findViewById(R.id.disconnect_button);
        mClearLogButton = (Button) findViewById(R.id.clear_button);
        mLogView = (TextView) findViewById(R.id.log_view);
        mSendProgressView = (TextView) findViewById(R.id.send_progress_view);
        mRecvProgressView = (TextView) findViewById(R.id.recv_progress_view);

        mScrollView = (ScrollView) findViewById(R.id.scroll_view);

        mSendButton.setOnClickListener(mListener);
        mConnectButton.setOnClickListener(mListener);
        mDisconnectButton.setOnClickListener(mListener);
        mClearLogButton.setOnClickListener(mListener);

        mConnectButton.setEnabled(true);
        mSendButton.setEnabled(false);
        mDisconnectButton.setEnabled(false);
        mIterationsEdit.setEnabled(false);
    }

    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.send_button:
                mSendCounter = 0;
                mSendNum = getNumber(mIterationsEdit, Integer.MAX_VALUE);

                try {
                    mFileTransactionModel.requestSendFile(SEND_FILE_PATH,
                            getSendIndex());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                mHandler.obtainMessage(MSG_SENDING).sendToTarget();
                break;

            case R.id.connect_button:
                mHandler.obtainMessage(MSG_CONNECTING).sendToTarget();
                mFileTransactionModel.start();
                break;

            case R.id.disconnect_button:
                mHandler.obtainMessage(MSG_DISCONNECTING).sendToTarget();
                mFileTransactionModel.stop();
                break;

            case R.id.clear_button:
                IwdsLog.d(this, "clear_button.");
                mSendCounter = 0;
                mRecvCounter = 0;
                mLogView.setText("");
                break;

            default:
                break;
            }
        }
    };

    private int getNumber(EditText editText, int defaultValue) {
        int value;

        try {
            value = Integer.parseInt(editText.getText().toString());
        } catch (NumberFormatException e) {
            value = defaultValue;
            e.printStackTrace();
        }

        return value;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mFileTransactionModel != null && mFileTransactionModel.isStarted())
            mFileTransactionModel.stop();

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSendFile = new File(SEND_FILE_PATH);
        if (!mSendFile.exists()) {
            IwdsLog.e(this, "File not found at " + SEND_FILE_PATH);
            Toast.makeText(this,
                    getString(R.string.file_not_found) + SEND_FILE_PATH,
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_main);

            getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            IwdsLog.i(this, "onCreate");

            mSendFileSize = mSendFile.length();
            mRecvFile = new File(RECV_FILE_PATH);
            mRecvFileSize = mRecvFile.length();

            if (mFileTransactionModel == null)
                mFileTransactionModel = new FileTransactionModel(this, this,
                        UUID);

            mSettingsPreference = getSharedPreferences("FileTransferTest",
                    MODE_PRIVATE);
            mSettingsEditor = mSettingsPreference.edit();

            buildView();
        }
    }

    /*
     * 监控设备间的连接状态变化。isConnected 为 true 表示两个设备连接成功。
     */
    @Override
    public void onLinkConnected(DeviceDescriptor descriptor, boolean isConnected) {
        if (isConnected) {
            IwdsLog.i(this, "Link connected: " + descriptor.toString());
        } else {
            IwdsLog.i(this, "Link disconnected: " + descriptor.toString());
        }
    }

    /*
     * 监控蓝牙传输通道状态，isAvailable 为 true 表示传输通道可用，这时才能传输数据。
     */
    @Override
    public void onChannelAvailable(boolean isAvailable) {
        if (isAvailable) {
            IwdsLog.i(this, "Data channel is available.");
            mHandler.obtainMessage(MSG_DATA_CHANNEL_AVAILABLE).sendToTarget();

        } else {
            IwdsLog.i(this, "Data channel is unavaiable.");
            mHandler.obtainMessage(MAS_DATA_CHANNEL_UNAVAILABLE).sendToTarget();
        }
    }

    /*
     * 监控传输是否成功。传输结束后发送端和接收端都会发生。
     */
    @Override
    public void onSendResult(DataTransactResult result) {
        if (result.getResultCode() == DataTransactResult.RESULT_OK) {
            if (result.getTransferedObject() instanceof File) {
                IwdsLog.i(this, "Send success");
                mHandler.obtainMessage(MSG_SEND_OK).sendToTarget();
            }

        } else {
            mHandler.obtainMessage(MSG_SEND_ERROR, result.getResultCode())
                    .sendToTarget();
            IwdsLog.i(this,
                    "Send failed by error code: " + result.getResultCode());
        }
    }

    /*
     * 文件接收完成。仅接收端发生。
     */
    @Override
    public void onFileArrived(File file) {
        IwdsLog.i(this, "Recv success");
        mHandler.obtainMessage(MSG_RECV_OK).sendToTarget();
    }

    /*
     * 文件发送进度。仅发送端发生。
     */
    @Override
    public void onSendFileProgress(int progress) {
        mHandler.obtainMessage(MSG_SEND_PROGRESS, progress, 0).sendToTarget();
    }

    /*
     * 文件接收进度。仅接收端发生。
     */
    @Override
    public void onRecvFileProgress(int progress) {
        mHandler.obtainMessage(MSG_RECV_PROGRESS, progress, 0).sendToTarget();
    }

    /*
     * 接收端确认是否接收文件。
     */
    @Override
    public void onRequestSendFile(FileInfo info) {
        mRecvFileSize = info.length;
        mChunkSize = info.chunkSize;

        showDialog(R.string.file_transfer, info.name, getFileRecvSize());
    }

    /*
     * 当文件接收端调用 notifyConfirmForReceiveFile 确认接收文件时发生。仅在发送端发生。
     */
    @Override
    public void onConfirmForReceiveFile() {
        IwdsLog.d(this, "onConfirmForReceiveFile");
        mSendStartTime = System.currentTimeMillis();
    }

    @Override
    public void onCancelForReceiveFile() {
        IwdsLog.d(this, "onCancelForReceiveFile");
        Toast.makeText(
                FileTransactionActivity.this,
                FileTransactionActivity.this.getString(R.string.unable_to_send)
                        + ": " + mSendFile.getName(), Toast.LENGTH_SHORT)
                .show();
        mHandler.obtainMessage(MSG_SEND_DONE).sendToTarget();
    }

    @Override
    public void onFileTransferError(int errorCode) {
        IwdsLog.d(
                this,
                "FileTransferError error "
                        + FileTransferErrorCode.errorString(errorCode));
        Toast.makeText(FileTransactionActivity.this,
                FileTransferErrorCode.errorString(errorCode), Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onSendFileInterrupted(int index) {
        saveSendIndex(index);
        IwdsLog.d(this, "onSendFileInterrupted index " + index);
    }

    @Override
    public void onRecvFileInterrupted(int index) {
        saveRecvIndex(index);
        IwdsLog.d(this, "onRecvFileInterrupted index " + index);

    }

}
