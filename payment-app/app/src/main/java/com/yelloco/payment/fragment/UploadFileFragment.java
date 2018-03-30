package com.yelloco.payment.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import com.alcineo.administrative.SendFileExecutor;
import com.alcineo.administrative.commands.GetConfList;
import com.alcineo.connection.dispatcher.DispatcherService;
import com.alcineo.connection.executor.AbstractExecutor;
import com.alcineo.connection.executor.CommandExecutor;
import com.yelloco.payment.PaymentFramework;
import com.yelloco.payment.R;
import com.yelloco.payment.utils.ConfigFileType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import yogesh.firzen.filelister.FileListerDialog;
import yogesh.firzen.filelister.OnFileSelectedListener;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class UploadFileFragment extends BaseFragment {

    private static final String TAG = "UploadFileFragment";

    private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST = 1;
    private static final Object LOCK = new Object();

    private AppCompatSpinner mFileTypeSpinner;
    private ProgressDialog mProgressDialog;

    private class UploadFileTask extends AsyncTask<Void, Double, Boolean> {

        private final String mFilePath;
        private DispatcherService mDispatchService;
        private List<GetConfList.UploadableFile> mUploadableFiles;

        public UploadFileTask(String filePath) {
            mFilePath = filePath;
        }

        @Override
        protected void onPreExecute() {
            showLoading(true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            showLoading(false);
            if (result) {
                showAlert(R.string.dialog_progress_title, R.string.dialog_upload_success,
                        android.R.drawable.ic_dialog_info);
            } else {
                showAlert(R.string.dialog_progress_title, R.string.dialog_upload_fail,
                        android.R.drawable.ic_dialog_alert);
            }
        }

        @Override
        protected void onProgressUpdate(Double... progress) {
            mProgressDialog.setProgress((int) Math.round(progress[0] * 100));
        }

        @Override
        protected void onCancelled() {
            showLoading(false);
            showAlert(R.string.dialog_progress_title, R.string.dialog_upload_fail,
                    android.R.drawable.ic_dialog_alert);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mDispatchService = PaymentFramework.getInstance().getDispatcherService();
            if (mDispatchService == null) {
                Log.e(TAG, "Cannot perform transaction command, payment dispatcher service unavailable.");
                return false;
            }

            getConfList();

            Log.d(TAG, "Uploading file: " + mFilePath);

            byte fileID = ConfigFileType.LANGUAGES.getId(mUploadableFiles);
            File file = new File(mFilePath);

            SendFileExecutor executor = null;
            try {
                executor = new SendFileExecutor(mDispatchService.getDispatcher(), mDispatchService,
                        fileID, (int) file.length(), new FileInputStream(file));

                executor.setErrorListener(new AbstractExecutor.ErrorListener() {
                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to upload file: ", e);
                        cancel(true);
                    }
                });
                executor.setProgressListener(new AbstractExecutor.ProgressListener() {
                    @Override
                    public void onProgressChanged(double progress) {
                        Log.i(TAG, "Progress: " + Math.round(progress * 100) + "%");
                        publishProgress(progress);
                    }
                });
                executor.setResultListener(new AbstractExecutor.ResultListener<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        Log.i(TAG, "File upload result: " + result);
                        synchronized (LOCK) {
                            LOCK.notifyAll();
                        }
                    }
                });
                synchronized (LOCK) {
                    executor.execute();
                    LOCK.wait(10000);
                }
                return true;

            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: ", e);
                return false;
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted: ", e);
                return false;
            }
        }

        private void getConfList() {
            CommandExecutor<GetConfList, List<GetConfList.UploadableFile>> executor = GetConfList
                    .getExecutor(mDispatchService.getDispatcher(), mDispatchService);
            executor.setResultListener(new AbstractExecutor.ResultListener<List<GetConfList.UploadableFile>>() {
                @Override
                public void onResult(List<GetConfList.UploadableFile> uploadableFiles) {
                    synchronized (LOCK) {
                        mUploadableFiles = uploadableFiles;
                        LOCK.notifyAll();
                    }
                }
            });
            synchronized (LOCK) {
                executor.execute();
                try {
                    LOCK.wait(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted: ", e);
                }
            }
        }
    }

    public UploadFileFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkStoragePermission();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_upload_file, container, false);
        Button uploadBtn = (Button) rootView.findViewById(R.id.button_upload);
        mFileTypeSpinner = (AppCompatSpinner) rootView.findViewById(R.id.spinner_file_type);

        List<String> types = new ArrayList<>();

        types.add(ConfigFileType.REVOCATION_LIST.name());
        types.add(ConfigFileType.CA_PUBLIC_KEYS.name());
        types.add(ConfigFileType.LANGUAGES.name());
        types.add(ConfigFileType.EXCEPTION_FILE.name());
        types.add(ConfigFileType.CONTACT.name());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item,
                types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFileTypeSpinner.setAdapter(adapter);

        final FileListerDialog fileListerDialog = FileListerDialog.createFileListerDialog(getContext());
        fileListerDialog.setDefaultDir(Environment.getExternalStorageDirectory());
        fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.ALL_FILES);

        fileListerDialog.setOnFileSelectedListener(new OnFileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                new UploadFileTask(path).execute();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileListerDialog.show();
            }
        });

        return rootView;
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_PERMISSION_REQUEST);
        }
    }

    public void showProgress() {
        hideProgress();
        mProgressDialog = new ProgressDialog(getContext(), R.style.DialogTheme);

        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(getText(R.string.dialog_progress_message));
        mProgressDialog.setTitle(getText(R.string.dialog_progress_title));

        mProgressDialog.show();
    }

    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void showAlert(int title, int message, int icon) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);

        alertDialogBuilder.setTitle(title)
                .setIcon(icon)
                .setMessage(message).setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialogBuilder.create().show();
    }

}
