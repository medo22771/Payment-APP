package com.yelloco.payment.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.yelloco.payment.R;
import com.yelloco.payment.data.UserDAO;
import com.yelloco.payment.data.db.model.User;

public class LoginFragment extends Fragment {

    private OnLoginFragmentInteractionListener mListener;

    private EditText mEditTextLogin;
    private EditText mEditTextPassword;
    private Button mButtonSubmit;

    private Fragment nextFragment;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(Fragment nextFragment) {
        LoginFragment fragment = new LoginFragment();
        fragment.setNextFragment(nextFragment);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mEditTextLogin = (EditText) view.findViewById(R.id.input_login);
        mEditTextPassword = (EditText) view.findViewById(R.id.input_password);
        mButtonSubmit = (Button) view.findViewById(R.id.btn_submit);

        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(mEditTextLogin.getText().toString(), mEditTextPassword.getText().toString());
            }
        });

        return view;
    }

    /**
     * Launch the GetUserTask if the form is valid with the login and the password
     * @param username
     * @param password
     */
    private void login(String username, String password) {
        if (validateLogin()) {
            GetUserTask task = new GetUserTask();
            task.execute(new User(username, password));
        }
    }

    /**
     * Check if the login form is valid
     * @return
     */
    private boolean validateLogin() {
        return !mEditTextLogin.getText().toString().isEmpty() && !mEditTextPassword.getText().toString().isEmpty();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginFragmentInteractionListener) {
            mListener = (OnLoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setNextFragment(Fragment fragment){
        this.nextFragment = fragment;
    }

    public interface OnLoginFragmentInteractionListener {
        void onLoginSuccessful(Fragment nextFragment);

        void onLoginFailed();
    }

    /**
     * Task used to check if the user exists in the database
     */
    private class GetUserTask extends AsyncTask<User, Void, Boolean> {

        private UserDAO dao;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dao = new UserDAO(getActivity());
            dao.open();
        }

        @Override
        protected Boolean doInBackground(User... users) {
            return dao.exists(users[0].getUsername(), users[0].getPassword());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            dao.close();

            if (result) {
                mListener.onLoginSuccessful(nextFragment);
            } else {
                mListener.onLoginFailed();
            }
        }
    }
}
