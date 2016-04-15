package com.example.objectexchangecallbacktest;

import com.ingenic.iwds.DeviceDescriptor;
import com.ingenic.iwds.datatransactor.DataTransactor;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactResult;
import com.ingenic.iwds.datatransactor.DataTransactor.DataTransactorCallback;
import com.ingenic.iwds.utils.IwdsLog;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements DataTransactorCallback {
    private String m_uuid = "ee3c19e2-17a4-0797-9352-aca0dd4b886f";
    private SafeParcelableClass m_object = new SafeParcelableClass();
    private DataTransactor m_transactor;
    private ObjectTransfer m_objectTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_objectTransfer = new ObjectTransfer();
        m_transactor = new DataTransactor(this, this, m_objectTransfer, m_uuid);
    }

    @Override
    protected void onPause() {
        super.onPause();

        m_transactor.stop();

        IwdsLog.i(this, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();

        m_transactor.start();

        IwdsLog.i(this, "onResume");
    }

    @Override
    public void onLinkConnected(DeviceDescriptor descriptor, boolean isConnected) {
        if (isConnected) {
            IwdsLog.i(this, "Link connected: " + descriptor.toString());
        } else {
            IwdsLog.i(this, "Link disconnected: " + descriptor.toString());
        }
    }

    @Override
    public void onChannelAvailable(boolean isAvailable) {
        if (isAvailable) {
            IwdsLog.i(this, "Data channel is available.");

            m_transactor.send(m_object);

        } else {
            IwdsLog.i(this, "Data channel is unavaiable.");
        }
    }

    @Override
    public void onSendResult(DataTransactResult result) {
        if (result.getResultCode() == DataTransactResult.RESULT_OK) {
            IwdsLog.i(this, "Send success");
        } else {
            IwdsLog.i(this,
                    "Send failed by error code: " + result.getResultCode());
        }
    }

    @Override
    public void onDataArrived(Object object) {
        SafeParcelableClass recvObject = (SafeParcelableClass) object;

        IwdsLog.d(this, "Receive===>" + recvObject);
    }

    @Override
    public void onSendFileProgress(int progress) {

    }

    @Override
    public void onRecvFileProgress(int progress) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSendFileInterrupted(int index) {

    }

    @Override
    public void onRecvFileInterrupted(int index) {

    }
}
