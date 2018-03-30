package com.yelloco.payment;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;
import com.yelloco.payment.data.UserDAO;
import com.yelloco.payment.data.db.model.User;

/**
 * Created by sylchoquet on 19/09/17.
 */

public class UserPreference extends DialogPreference {

    private static final String TAG = UserPreference.class.getSimpleName();

    private int mDialogLayoutResId = R.layout.preference_dialog_add_user;

    public UserPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setPersistent(false);
    }

    public UserPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setPersistent(false);
    }

    public UserPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
    }

    public UserPreference(Context context) {
        super(context);
        setPersistent(false);
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    public void saveUserToDatabase(String login, String password) {
        Log.d(TAG, "Saving user to database...");
        InsertUserTask task = new InsertUserTask();
        task.execute(new User(login, password));
    }

    private class InsertUserTask extends AsyncTask<User, Void, Boolean> {

        private UserDAO dao;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dao = new UserDAO(getContext());
            dao.open();
        }

        @Override
        protected Boolean doInBackground(User... users) {
            return dao.insert(users[0]) != -1;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dao.close();
            if (result) {
                Toast.makeText(getContext(), getContext().getString(R.string.database_add_ok), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getContext().getString(R.string.database_add_fail), Toast.LENGTH_LONG).show();
            }
        }
    }
}
