package com.ingenic.iwds.jarapidemo;

import java.io.File;
import java.io.FileNotFoundException;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.util.Log;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.FileInfo;
import com.ingenic.iwds.datatransactor.FileTransactionModel;
import com.ingenic.iwds.datatransactor.FileTransactionModel.FileTransactionModelCallback;
import com.ingenic.iwds.utils.IwdsLog;

/**
 * 演示用 FileTransactionModel 在两个设备之间（手机和手表之间）传输文件。
 * 发送端的文件路径为 /sdcard/Download/test.zip，此文件必须存在。接收端
 * 文件自动存到 /sdcard/iwds/ 文件夹。
 * 
 * 1. 两个设备先用 start 启动 FileTransactionModel
 * 2. 通过 onLinkConnected 监控设备间连接状态，通过 onChannelAvailable 监控蓝牙数据传输通道。
 *    如果设备已经连接，数据传输通道有效，就可以传输数据了。
 * 3. 发送端设备中用 requestSendFile() 发送文件
 * 4. 接收端设备在 onRequestSendFile() 确认是否接受该文件。用 notifyConfirmForReceiveFile
 *    确认接收文件，用 notifyCancelForReceiveFile 取消文件接收。
 * 5. 文件传输结束后，发送端和接收端都会发生 onSendResult，用以通知传输成功或失败。接收端发生
 *    onFileArrived，通知文件接收完成。
 *
 * 注意：需要在两个设备上同时安装这个应用
 */
public class FileTransactorFragment extends DemoFragment {
    private View mContentView;
    private Context mContext;

    private final static String TAG = "IWDS ---> FileTransactorFragment";
    private final static String FILE_PATH = "/sdcard/Download/test.zip";

    private final static int MSG_SEND_OK = 0;
    private final static int MSG_SEND_ERROR = 1;
    private final static int MSG_LINK_CONNECTED = 2;
    private final static int MSG_LINK_DISCONNECTED = 3;
    private final static int MSG_DATA_CHANNEL_AVAILABLE = 4;
    private final static int MAS_DATA_CHANNEL_UNAVAILABLE = 5;
    private final static int MSG_RECV_OK = 6;
    private final static int MSG_RECVING = 7;
    private final static int MSG_SENDING = 8;
    private final static int MSG_CONNECTING = 9;
    private final static int MSG_DISCONNECTING = 11;
    private final static int MSG_SEND_DONE = 13;
    private final static int MSG_UPDATE_PROGRESS = 14;

    private final static String UUID = "a1dc19e2-17a4-0797-9362-68a0dd4bfb68";

    private FileTransactionModel mFileTransactionModel;

    private long mFileSize;

    private File mFile;
    private long mStartTime;

    private final static int TYPE_NONE = 0;
    private final static int TYPE_SEND = 1;
    private final static int TYPE_RECV = 2;
    private int mTransactionType = TYPE_NONE;

    private Button mSendButton;
    private Button mConnectButton;
    private Button mDisconnectButton;
    private TextView mLogView;
    private ScrollView mScrollView;
    private TextView mProgressView;
    private boolean mChannelAvailable = false;
    private boolean mServiceConnected = false;

    private void showToast(String text) {
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    private void setButtonStatus(boolean connect, boolean sendEnable) {
        mConnectButton.setEnabled(!connect);
        mDisconnectButton.setEnabled(connect);
        if (connect && sendEnable) {
            mSendButton.setEnabled(true);
        } else {
            mSendButton.setEnabled(false);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            case MSG_SEND_OK: {
                long time = System.currentTimeMillis() - mStartTime;
                String log = String.format("size: %d bytes, consume time: %d ms\nsend ok\n",
                        mFileSize, time);
                mLogView.append(log);
                //mHandler.obtainMessage(MSG_SEND_DONE).sendToTarget();
                setButtonStatus(true, mChannelAvailable);
                break;
            }
            
            case MSG_SEND_ERROR: {
                long time = System.currentTimeMillis() - mStartTime;
                String log = String.format("size: %d bytes, consume time: %d ms\nsend fail\n",
                        mFileSize, time);
                mLogView.append(log);
                setButtonStatus(true, mChannelAvailable);
                break;
            }

            case MSG_RECV_OK: {
                long time = System.currentTimeMillis() - mStartTime;
                String log = String.format("size: %d bytes, consume time: %d ms\nreceive ok\n",
                        mFileSize, time);
                mLogView.append(log);
                setButtonStatus(true, mChannelAvailable);
                break;
            }
            case MSG_LINK_CONNECTED:
                showToast("Link connected");
                setButtonStatus(true, mChannelAvailable);
                break;

            case MSG_LINK_DISCONNECTED:
                showToast("Link disconnected");
                setButtonStatus(false, mChannelAvailable);
                break;

            case MSG_DATA_CHANNEL_AVAILABLE:
                showToast("Data channel is available");
                setButtonStatus(true, mChannelAvailable);
                break;

            case MAS_DATA_CHANNEL_UNAVAILABLE:
                showToast("Data channel is unavailable");
                setButtonStatus(false, mChannelAvailable);
                break;

            case MSG_RECVING:
                setButtonStatus(true, false);
                break;

            case MSG_SENDING:
                setButtonStatus(true, false);
                break;

            case MSG_CONNECTING:
            case MSG_SEND_DONE:
                setButtonStatus(true, mChannelAvailable);
                break;

            case MSG_DISCONNECTING:
                setButtonStatus(false, mChannelAvailable);
                break;

            case MSG_UPDATE_PROGRESS:
                int progress = msg.arg1;
                mProgressView.setText("progress: " + progress);
                break;

            default:
                IwdsLog.e(FileTransactorFragment.this, "error message");
                break;
            }
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    };

    private void buildView() {
        mSendButton = (Button) mContentView.findViewById(R.id.send_button);
        mSendButton.setOnClickListener(mListener);
        
        mConnectButton = (Button) mContentView.findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(mListener);

        mDisconnectButton = (Button) mContentView.findViewById(R.id.disconnect_button);
        mDisconnectButton.setOnClickListener(mListener);
        
        mLogView = (TextView) mContentView.findViewById(R.id.log_view);
        mProgressView = (TextView) mContentView.findViewById(R.id.progress_view);

        mScrollView = (ScrollView) mContentView.findViewById(R.id.scroll_view);
    }

    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.send_button:
                Log.d(TAG, "MSG_SENDING.");
                try {
                    mFileTransactionModel.requestSendFile(FILE_PATH);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                mHandler.obtainMessage(MSG_SENDING).sendToTarget();
                break;

            case R.id.connect_button:
                Log.d(TAG, "MSG_CONNECTING.");
                mHandler.obtainMessage(MSG_CONNECTING).sendToTarget();
                mFileTransactionModel.start();
                mServiceConnected = true;
                break;

            case R.id.disconnect_button:
                Log.d(TAG, "MSG_DISCONNECTING.");
                mHandler.obtainMessage(MSG_DISCONNECTING).sendToTarget();
                mFileTransactionModel.stop();
                mServiceConnected = false;
                break;

            default:
                break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.file_transactor, container, false);

        IwdsLog.i(this, "onCreate");

        buildView();

        mFileTransactionModel = new FileTransactionModel(mContext, mCallback, UUID);

        mFile = new File(FILE_PATH);

        if (!mFile.exists()) {
            IwdsLog.e(this, "File not found at " + FILE_PATH);
            showToast("File not found at " + FILE_PATH);
            setButtonStatus(false, mChannelAvailable);
        } else {
            mFileSize = mFile.length();
            mHandler.obtainMessage(MSG_LINK_DISCONNECTED).sendToTarget();
        }

        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mServiceConnected) {
            mFileTransactionModel.stop();
            mServiceConnected = false;
        }
        super.onPause();
    }

    FileTransactionModelCallback mCallback = new FileTransactionModelCallback() {
        /*
         * 监控设备间的连接状态变化。isConnected 为 true 表示两个设备连接成功。
         */
        @Override
        public void onLinkConnected(DeviceDescriptor descriptor, boolean isConnected) {
            if (isConnected) {
                IwdsLog.i(this, "Link connected: " + descriptor.toString());
                mHandler.obtainMessage(MSG_LINK_CONNECTED).sendToTarget();
            } else {
                IwdsLog.i(this, "Link disconnected: " + descriptor.toString());
                mHandler.obtainMessage(MSG_LINK_DISCONNECTED).sendToTarget();
            }
        }

        /*
         * 监控蓝牙传输通道状态，isAvailable 为 true 表示传输通道可用，这时才能传输数据。
         */
        @Override
        public void onChannelAvailable(boolean isAvailable) {
            mChannelAvailable = isAvailable;
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
                IwdsLog.i(this, "Send success");
                if (mTransactionType == TYPE_SEND) {
                    mHandler.obtainMessage(MSG_SEND_OK).sendToTarget();
                }
            } else {
                mHandler.obtainMessage(MSG_SEND_ERROR, result.getResultCode()).sendToTarget();
                IwdsLog.i(this, "Send failed by error code: " + result.getResultCode());
            }
        }

        /*
         * 文件接收完成。仅接收端发生。
         */
        @Override
        public void onFileArrived(File file) {
            IwdsLog.i(this, "receive success");
            mFileSize = file.length();
            mHandler.obtainMessage(MSG_RECV_OK).sendToTarget();
        }

        /*
         * 文件发送进度。仅发送端发生。
         */
        @Override
        public void onSendFileProgress(int progress) {
            mHandler.obtainMessage(MSG_UPDATE_PROGRESS, progress, 0).sendToTarget();
        }

        /*
         * 文件接收进度。仅接收端发生。
         */
        @Override
        public void onRecvFileProgress(int progress) {
            mHandler.obtainMessage(MSG_UPDATE_PROGRESS, progress, 0).sendToTarget();
        }

        /*
         * 接收端确认是否接收文件。
         */
        @Override
        public void onRequestSendFile(FileInfo info) {
            mTransactionType = TYPE_RECV;
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("File transfer");
            builder.setCancelable(false);
            builder.setMessage("File name: " + info.name + "\n"
                    + "File size: " + info.length + "bytes");
            builder.setPositiveButton(R.string.confirm,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mFileTransactionModel.notifyConfirmForReceiveFile();
                            mStartTime = System.currentTimeMillis(); //receiver start time
                            mHandler.obtainMessage(MSG_RECVING, 0, 0).sendToTarget();
                        }
                    });
            builder.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mFileTransactionModel.notifyCancelForReceiveFile();
                        }
            });
            builder.create().show();
        }

        /*
         * 当文件接收端调用 notifyConfirmForReceiveFile 确认接收文件时发生。仅在发送端发生。
         */
        @Override
        public void onConfirmForReceiveFile() {
            Log.d(TAG, "onConfirmForReceiveFile");
            mStartTime = System.currentTimeMillis(); //send start time
            mTransactionType = TYPE_SEND;
        }

        /*
         * 当文件接收端调用 notifyCancelForReceiveFile 取消接收文件时发生。仅在发送端发生。
         */
        @Override
        public void onCancelForReceiveFile() {
            Log.d(TAG, "onCancelForReceiveFile");
            showToast("Unable to send: " + mFile.getName());

            mHandler.obtainMessage(MSG_SEND_DONE).sendToTarget();
        }

        @Override
        public void onFileTransferError(int arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onRecvFileInterrupted(int arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onSendFileInterrupted(int arg0) {
            // TODO Auto-generated method stub
            
        }
    };
}
