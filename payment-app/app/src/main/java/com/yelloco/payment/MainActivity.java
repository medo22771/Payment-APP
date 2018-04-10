package com.yelloco.payment;

import static com.yelloco.payment.utils.TlvUtils.createTagStore;
import static com.yelloco.payment.utils.TlvUtils.isSignatureRequired;
import static com.yelloco.payment.utils.TlvUtils.listTags;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alcineo.transaction.TransactionListener;
import com.alcineo.transaction.TransactionManager;
import com.alcineo.transaction.events.DekRequestEvent;
import com.alcineo.transaction.events.DelayedAuthorizationEvent;
import com.alcineo.transaction.events.DisplayMenuRequestEvent;
import com.alcineo.transaction.events.DisplayTextRequestEvent;
import com.alcineo.transaction.events.GetAmountRequestEvent;
import com.alcineo.transaction.events.NotifyKernelIdEvent;
import com.alcineo.transaction.events.OnlineRequestEvent;
import com.alcineo.transaction.events.OnlineReversalEvent;
import com.alcineo.transaction.events.OutcomeReceivedEvent;
import com.alcineo.transaction.events.PinRequestEvent;
import com.alcineo.transaction.events.ReceiptDataEvent;
import com.alcineo.transaction.events.TransactionFinishedEvent;
import com.alcineo.transaction.events.TransactionStartedEvent;
import com.google.common.util.concurrent.Service;
import com.seikoinstruments.sdk.thermalprinter.PrinterEvent;
import com.seikoinstruments.sdk.thermalprinter.PrinterException;
import com.seikoinstruments.sdk.thermalprinter.PrinterInfo;
import com.seikoinstruments.sdk.thermalprinter.PrinterListener;
import com.seikoinstruments.sdk.thermalprinter.PrinterManager;
import com.yelloco.payment.api.ErrorCode;
import com.yelloco.payment.api.PaymentRequest;
import com.yelloco.payment.api.PaymentResponse;
import com.yelloco.payment.api.PaymentResult;
import com.yelloco.payment.data.SaveTransactionTask;
import com.yelloco.payment.fragment.AmountFragment;
import com.yelloco.payment.fragment.BaseFragment;
import com.yelloco.payment.fragment.DwnReceiptFragment;
import com.yelloco.payment.fragment.HistoryFragment;
import com.yelloco.payment.fragment.LoginFragment;
import com.yelloco.payment.fragment.MenuFragment;
import com.yelloco.payment.fragment.MessageFragment;
import com.yelloco.payment.fragment.ProcessingFragment;
import com.yelloco.payment.fragment.QRCodeGenFragment;
import com.yelloco.payment.fragment.ReceiptFragment;
import com.yelloco.payment.fragment.SettingsFragment;
import com.yelloco.payment.fragment.SignatureFragment;
import com.yelloco.payment.fragment.TransactionDetailDialogFragment;
import com.yelloco.payment.fragment.UploadFileFragment;
import com.yelloco.payment.gateway.Gateway;
import com.yelloco.payment.gateway.NexoTestGateway;
import com.yelloco.payment.gateway.SafechargeGateway;
import com.yelloco.payment.gateway.YelloGateway;
import com.yelloco.payment.host.HostManager;
import com.yelloco.payment.host.HostManagerImpl;
import com.yelloco.payment.nexo.NexoTransactionHelper;
import com.yelloco.payment.rpc.RpcChannel;
import com.yelloco.payment.transaction.SharedPreferencesTransactionReferencePersistence;
import com.yelloco.payment.transaction.TransactionContext;
import com.yelloco.payment.transaction.TransactionHelper;
import com.yelloco.payment.utils.RequestType;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.AsynchronousCloseException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main activity provides:
 * - payment fragments management
 * - payment framework control - init, close, perform operation, listen to payment events
 */
public class MainActivity extends BaseActivity implements TransactionListener, BaseFragment
        .CommandDispatcher, BaseFragment.TransactionProvider, AmountFragment
        .AmountEnteredListener, TransactionDetailDialogFragment.TransactionDetailListener,
        NavigationView.OnNavigationItemSelectedListener, LoginFragment.OnLoginFragmentInteractionListener, PrinterListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TRANSACTION_CONTEXT = "TRANSACTION_CONTEXT";
    private static final String AMOUNT_FRAGMENTS = "AMOUNT_FRAGMENTS";
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private static final int REQUEST_CODE_LOCATION = 12345;
    private static final int REQUEST_CODE_BLUETOOTH = 12346;
    private static final int MAX_REF_VALUE = 999999;

    private final Object frameworkLock = new Object();

    private Toolbar mToolbar;

    private PreferencesListener mPreferencesListener;

    private static FragmentManager fragmentManager;
    private DrawerLayout mDrawerLayout;
    private TransactionContext transactionContext;

    private RelativeLayout mRelativeLayoutLoading;
    private LinearLayout mLinearLayoutLoading;

    // particular amount fragment types are stored to switch between them
    private ArrayList<Integer> amountFragments;

    private boolean mAmountSwitch = true;

    PrinterManager printerManager;

    private String mGatewayPreferencesKey;
    private String mPrefGatewayYello;
    private String mPrefGatewayTest;
    private String mPrefGatewaySafecharge;
    private String mPrefGatewayDisable;

    private String mGatewayYelloUrlPreferenceKey;

    private Gateway mCurrentGateway;
    private HostManager mHostManager;
    private TransactionHelper mTransactionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //This Is Used For API 23 +
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 100);
        //END

        mPreferencesListener = new PreferencesListener();

        mapGatewayPreferences();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext());
        if (!TransactionPreferences.isInitialized(getPreferences(Context.MODE_PRIVATE))) {
            TransactionPreferences.PAYMENT.setValue(getPreferences(Context.MODE_PRIVATE), true);
        }
        mTransactionHelper = new TransactionHelper(getPreferences(Context.MODE_PRIVATE));
        initOnline(prefs);

        mRelativeLayoutLoading = (RelativeLayout) findViewById(R.id.rl_loading);
        mLinearLayoutLoading = (LinearLayout) findViewById(R.id.ll);
        mLinearLayoutLoading.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse));
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        prepareDrawer();
        fragmentManager = getSupportFragmentManager();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (frameworkLock) {
                    try {
                        RpcChannel.getInstance().init(MainActivity.this);
                        PaymentFramework.getInstance().init(new MainActivity.ServiceListener(), MainActivity.this);
                    } catch (Exception e) {
                        //TODO define the error cases (showing error, or closing app etc...)
                        Log.e("ERROR", "Failed to initialize payment framework dispatcher: ", e);
                        showToastOnUI("Failed to initialize payment framework.");
                        PaymentFramework.getInstance().destroy();
                    }
                }
            }
        });
        if (getIntent().getAction() == PaymentRequest.ACTION_PAY) {
            Intent intent = getIntent();
            Log.v(TAG, "Received payment request from YelloPayAPI");
            String amount = intent.getStringExtra("AMOUNT");
            PaymentRequest request = PaymentRequest.fromIntent(intent);
            Log.v(TAG, request.toString());
            startTransactionFromAPI(request);
            return;
        }
        if (savedInstanceState == null) {
            initScreen();
//            mytestinitScreen();
        } else {
            transactionContext = savedInstanceState.getParcelable(TRANSACTION_CONTEXT);
            amountFragments = savedInstanceState.getIntegerArrayList(AMOUNT_FRAGMENTS);
            Log.i(TAG, "transaction context after create: " + transactionContext.toString());
        }

        printerManager = new PrinterManager();
    }

    private void initOnline(SharedPreferences prefs) {
        initializeGateway(prefs);
        mHostManager = new HostManagerImpl(getBaseContext(),
                new NexoTransactionHelper(getPreferences(MODE_PRIVATE)), mCurrentGateway);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).registerOnSharedPreferenceChangeListener(
                mPreferencesListener);
    }

    @Override
    protected void onPause() {
        PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext()).unregisterOnSharedPreferenceChangeListener(
                mPreferencesListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        PaymentFramework.getInstance().destroy();
        RpcChannel.getInstance().destroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (transactionContext != null) {
            outState.putParcelable(TRANSACTION_CONTEXT, transactionContext);
        }
        if (amountFragments != null) {
            outState.putIntegerArrayList(AMOUNT_FRAGMENTS, amountFragments);
        }
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }


    private void mapGatewayPreferences() {
        mGatewayPreferencesKey = getString(R.string.gateway_preferences);
        mPrefGatewayTest = getString(R.string.gateway_nexo_test);
        mPrefGatewaySafecharge = getString(R.string.gateway_safecharge);
        mPrefGatewayDisable = getString(R.string.gateway_disable);
        mPrefGatewayYello = getString(R.string.gateway_yello);
        mGatewayYelloUrlPreferenceKey = getString(R.string.gateway_preferences_yello_url);
    }

    private void initializeGateway(SharedPreferences sharedPreferences) {
        String gatewayPreference = sharedPreferences.getString(mGatewayPreferencesKey,
                mPrefGatewayYello);
        if (gatewayPreference.equals(mPrefGatewayTest)) {
            mCurrentGateway = new NexoTestGateway(
                    getString(R.string.gateway_nexo_test_address),
                    getResources().getInteger(R.integer.gateway_nexo_test_port),
                    getResources().getInteger(R.integer.gateway_nexo_test_timeout_ms));
        }
        if (gatewayPreference.equals(mPrefGatewaySafecharge)) {
            mCurrentGateway = new SafechargeGateway(
                    getString(R.string.gateway_safecharge_test_url));
        }
        if (gatewayPreference.equals(mPrefGatewayYello)) {
            String yelloGatewayUrl = sharedPreferences.getString(mGatewayYelloUrlPreferenceKey, "");
            mCurrentGateway = new YelloGateway(yelloGatewayUrl);
        }
    }

    public void startTransactionFromAPI(PaymentRequest request) {
        transactionContext = mTransactionHelper.initializeTransactionContext(request);
        transactionContext.setTransactionReferencePersistence(
                new SharedPreferencesTransactionReferencePersistence(getBaseContext(),
                        getBaseContext().getPackageName(), MAX_REF_VALUE));
        lockOrientation();
        dispatchCommand(new Runnable() {
            @Override
            public void run() {
                startTransaction();
            }
        }, new Runnable() {
            @Override
            public void run() {
                PaymentResponse response = new PaymentResponse();
                response.setResult(PaymentResult.ERROR);
                response.setErrorCode(ErrorCode.UNKNOWN_ERROR);
                setResult(Activity.RESULT_OK, response.toIntent());
                finish();
            }
        });
    }

    public void finishTransactionFromAPI() {
        PaymentResponse response = mTransactionHelper.createPaymentResponse(transactionContext);
        setResult(Activity.RESULT_OK, response.toIntent());
        finish();
    }

    /**
     * The method serves as the UI entry point for the transaction.
     * This should be always called when changing the settings of the application,
     * on application startup or after the transaction finishes/cancels.
     * The init screen is represented by Amount Fragment. This method takes care of creating it
     * and distinguishing what type of payment it is related to.
     */
    public void initScreen() {
        unlockOrientation();
        transactionContext = mTransactionHelper.initializeTransactionContext(null);

        prepareAmountFragments();
        // start first amount fragment
        Bundle bundle = new Bundle();
        bundle.putInt(AmountFragment.TRANSACTION_TYPE, amountFragments.get(0));
        bundle.putInt(AmountFragment.INDEX, 0);
        switchFragment(BaseFragment.newInstance(AmountFragment.class, bundle), false);
    }

    /**
     * The method serves as the UI entry point for the transaction.
     * This should be always called when changing the settings of the application,
     * on application startup or after the transaction finishes/cancels.
     * The init screen is represented by Amount Fragment. This method takes care of creating it
     * and distinguishing what type of payment it is related to.
     */
    public void mytestinitScreen() {
        unlockOrientation();
        transactionContext = mTransactionHelper.initializeTransactionContext(null);

        prepareAmountFragments();
//         start first amount fragment


        SharedPreferences TransactionIDPref = getSharedPreferences("TransactionIDDB", Context.MODE_PRIVATE);
        SharedPreferences.Editor QREditor = TransactionIDPref.edit();
        QREditor.putString("QRCode", "" + 150);
        QREditor.apply();

        DwnReceiptFragment nextFrag = new DwnReceiptFragment();
        switchFragment(nextFrag, false);
    }

    /**
     * The method should be called from any fragment when the transaction processing was finished
     * within the app (it is not dependent on L2 onTransactionFinished as there might be much more
     * stuff done afterwards within app like signature processing)
     * Here we check whether we should restart the UI for another transaction or finish the application
     * and return result if the transaction was called from API.
     */
    @Override
    public void finishTransaction() {
        if (transactionContext != null &&
                transactionContext.getRequestType() == RequestType.YELLO_PAY_API) {
            finishTransactionFromAPI();
        } else {
            initScreen();
        }
    }

    /**
     * Locks the orientation in current state.
     */
    private void lockOrientation() {
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    private void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    /**
     * Initializes payment transaction according to {@link TransactionContext}.
     * If it succeeds {@link MainActivity#onTransactionStarted(TransactionStartedEvent)} should be received.
     * Within payment framework transaction is in progress until we receive
     * {@link MainActivity#onTransactionFinished(TransactionManager.TransactionStatus, TransactionFinishedEvent)}
     */
    private void startTransaction() {
        final TransactionManager transactionManager = mTransactionHelper.createTransactionManager(transactionContext);
        transactionManager.addListener(this);
        try {
            transactionManager.start(60, TimeUnit.SECONDS);
        } catch (IOException e) {
            Log.i(TAG, "Failed to start payment transaction: ", e);
            unlockOrientation();
            showToastOnUI("Failed to start payment transaction");
        }
    }

    /**
     * Interface for {@link AmountFragment} to confirm the amount. After amount confirmation
     * we can switch to another type of amount fragment or to start transaction - final scenario depends
     * on the application settings (transaction types combination).
     *
     * @param index - index of the amount fragment type in {@link MainActivity#amountFragments}
     */
    @Override
    public void onAmountEntered(int index) {
        if (amountFragments.size() > index + 1) {
            int nextFragmentIndex = index + 1;

            Bundle bundle = new Bundle();
            bundle.putInt(AmountFragment.TRANSACTION_TYPE, amountFragments.get(nextFragmentIndex));
            bundle.putInt(AmountFragment.INDEX, nextFragmentIndex);
            switchFragment(BaseFragment.newInstance(AmountFragment.class, bundle), false);
        } else {
            lockOrientation();
            dispatchCommand(new Runnable() {
                @Override
                public void run() {
                    startTransaction();
                }
            });
        }
    }


    @Override
    public void onRefundTransaction(BigDecimal amount) {
        unlockOrientation();
        transactionContext = mTransactionHelper.initializeTransactionContext(null);

        Bundle bundle = new Bundle();
        bundle.putInt(AmountFragment.TRANSACTION_TYPE, AmountFragment.TYPE_REFUND);
        bundle.putInt(AmountFragment.INDEX, 0);
        transactionContext.setAmount(amount);
        switchFragment(BaseFragment.newInstance(AmountFragment.class, bundle), false);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        mAmountSwitch = true;

        switch (item.getItemId()) {
            case R.id.menu_item_auth_only:
                // CustomDrawerToggle switch
                ((SwitchCompat) item.getActionView().findViewById(R.id.menu_switch)).toggle();
                return true;
            case R.id.menu_item_payment:
            case R.id.menu_item_cash_back:
            case R.id.menu_item_incr_amount:
                // CustomDrawerToggle checkbox
                ((CheckBox) item.getActionView().findViewById(R.id.menu_check_box)).toggle();
                return true;
            case R.id.menu_item_historic:
                HistoryFragment fragment = new HistoryFragment();
                fragment.setHostManager(mHostManager);
                switchFragment(fragment, false);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mAmountSwitch = false;
                return true;
            case R.id.menu_item_upload_file:
                switchFragment(new UploadFileFragment(), false);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mAmountSwitch = false;
                return true;
            case R.id.menu_item_start_transaction:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.menu_item_settings:
                switchFragment(new SettingsFragment(), false);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mAmountSwitch = false;
                return true;
            case R.id.menu_item_printer:
                if (PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBluetoothIntent, REQUEST_CODE_BLUETOOTH);
                    } else {
                        connectToPrinter();
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);
                }
                return true;
            default:
                showToastOnUI("Feature not implemented yet");
                return false;
        }
    }


    @Override
    public void onLoginSuccessful(Fragment nextFragment) {
        switchFragment(nextFragment, false);
    }

    @Override
    public void onLoginFailed() {
        Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show();
    }

    private class CustomDrawerToggle extends ActionBarDrawerToggle {

        public CustomDrawerToggle(Activity activity, DrawerLayout drawerLayout, @StringRes int
                openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        public CustomDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar,
                                  @StringRes int openDrawerContentDescRes, @StringRes int
                                          closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            if (mAmountSwitch)
                initScreen();
        }
    }

    /**
     * Prepare navigation drawer.
     */
    protected void prepareDrawer() {
        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new CustomDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get first submenu (HOME section)
        Menu menu = navigationView.getMenu();

        SwitchCompat authSwitch = (SwitchCompat) menu.findItem(R.id.menu_item_auth_only)
                .getActionView().findViewById(R.id.menu_switch);

        authSwitch.setChecked(TransactionPreferences.AUTH_ONLY.getValue(preferences));
        authSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TransactionPreferences.AUTH_ONLY.setValue(preferences, isChecked);
            }
        });

        // Init check boxes
        initMenuCheckBox(menu, R.id.menu_item_payment, TransactionPreferences.PAYMENT);
        initMenuCheckBox(menu, R.id.menu_item_cash_back, TransactionPreferences.CASHBACK);
        initMenuCheckBox(menu, R.id.menu_item_incr_amount, TransactionPreferences.INCREASED_AMOUNT);

        TextView tvVersion = (TextView) menu.findItem(R.id.menu_item_version).getActionView().findViewById(R.id.menu_tv);
        tvVersion.setText(String.format("v%s", BuildConfig.VERSION_NAME));
        SpannableString s = new SpannableString(getString(R.string.navigation_drawer_item_version));
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.material_secondary_text)), 0, s.length(), 0);
        menu.findItem(R.id.menu_item_version).setTitle(s);

        TextView tvGo = (TextView) menu.findItem(R.id.menu_item_start_transaction).getActionView().findViewById(R.id.menu_tv);
        tvGo.setTextColor(ContextCompat.getColor(this, R.color.colorBlack));
        tvGo.setTypeface(null, Typeface.BOLD);
        tvGo.setTextSize(16);
        tvGo.setText(getString(R.string.navigation_drawer_item_start_transaction));

    }

    private void initMenuCheckBox(final Menu menu, final int itemID, final TransactionPreferences preference) {
        final SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);

        AppCompatCheckBox checkBox = (AppCompatCheckBox)
                menu.findItem(itemID).getActionView().findViewById(R.id.menu_check_box);

        checkBox.setChecked(preference.getValue(sharedPreferences));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preference.setValue(sharedPreferences, isChecked);
            }
        });
    }

    private class ServiceListener extends Service.Listener {
        @Override
        public void failed(Service.State from, final Throwable failure) {
            // This code is executed when the DispatcherService fails - I/O error occurred.
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // AsynchronousCloseException is standard closing from our side
                    if (failure instanceof AsynchronousCloseException)
                        return;
                    //TODO define the error cases (showing error, or closing app etc...)
                    Log.e(TAG, "Service failed: ", failure);
                    showToastOnUI("Connection to payment framework service failed.");
                    synchronized (frameworkLock) {
                        PaymentFramework.getInstance().destroy();
                    }
                }
            });
        }
    }

    /**
     * According to the application settings we prepare the order of amount fragments types.
     * They need to be presented to the user one by one before starting the transaction.
     */
    private void prepareAmountFragments() {
        amountFragments = new ArrayList<>();
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        if (TransactionPreferences.PAYMENT.getValue(prefs)) {
            Log.v(TAG, "Type payment");
            amountFragments.add(AmountFragment.TYPE_PAYMENT);
        } else if (TransactionPreferences.REFUND.getValue(prefs)) {
            Log.v(TAG, "Type refund");
            amountFragments.add(AmountFragment.TYPE_REFUND);
        }
        if (TransactionPreferences.CASHBACK.getValue(prefs)) {
            Log.v(TAG, "Type cashback");
            amountFragments.add(AmountFragment.TYPE_CASHBACK);
        }
        if (TransactionPreferences.INCREASED_AMOUNT.getValue(prefs)) {
            Log.v(TAG, "Type increased amount");
            amountFragments.add(AmountFragment.TYPE_INCREASED_AMOUNT);
        }
        if (amountFragments.isEmpty())
            throw new IllegalStateException("No amount fragment was found for current " +
                    "configuration: \n" + TransactionPreferences.dumpValues(prefs));
    }

    /**
     * Interface method for fragments to get the current transaction context.
     *
     * @return
     */


//    @Override
//    public void fromTDetailDialogToMain(long code)
//    {
//        QRCodeGenFragment QRCodeObj = (QRCodeGenFragment)getSupportFragmentManager().findFragmentById(R.id.main_container);
//        QRCodeObj.RecievedCodeFromMain(code);
//
//        QRCodeGenFragment OBJ1 = new QRCodeGenFragment();
//        mDrawerLayout.closeDrawer(GravityCompat.START);
//        switchFragment(OBJ1,false);
//    }
//
//    @Override
//    public void sendCodeToMainAct(int QRCode)
//    {
//
//        DwnReceiptFragment DwnReceiptObj = (DwnReceiptFragment)getSupportFragmentManager().findFragmentById(R.id.main_container);
////        QRCodeGenFragment OBJ1 = new QRCodeGenFragment();
////        mDrawerLayout.closeDrawer(GravityCompat.START);
////        switchFragment(OBJ1,false);
//    }

    @Override
    public TransactionContext getTransactionContext() {
        return transactionContext;
    }

    /**
     * To be used from different than main UI threads
     *
     * @param message - message to be shown as TOAST
     */
    private void showToastOnUI(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * @see MainActivity#dispatchCommand(Runnable, Runnable)
     */
    @Override
    public void dispatchCommand(final Runnable task) {
        dispatchCommand(task, null);
    }

    /**
     * Should be used for all operations on transaction manager instance. They include the socket
     * communication which should not run on main UI thread.
     * This function also performs the check whether dispatcher is initialized.
     * It is synchronized together with init and de-init functions.
     *
     * @param task    runnable task to be executed
     * @param failure runnable failure to be executed in case when task cannot be executed
     */
    @Override
    public void dispatchCommand(final Runnable task, final Runnable failure) {

        executor.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (frameworkLock) {
                    if (PaymentFramework.getInstance().getDispatcherService() == null) {
                        Log.e(TAG, "Cannot perform transaction command, payment dispatcher service unavailable.");
                        showToastOnUI("Cannot perform transaction command, payment service " +
                                "unavailable.");
                        if (failure != null) {
                            try {
                                failure.run();
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to perform the failure scenario task.", e);
                            }
                        }
                    } else if (task != null) {
                        try {
                            task.run();
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to execute task.", e);
                        }
                    }
                }
            }
        });
    }

    public static void switchFragment(Fragment fragment, boolean login) {
        if (login) {
            Log.v(TAG, "Switching to Login");
            fragment = LoginFragment.newInstance(fragment);
        }
        Log.v(TAG, "Switching fragment to: " + fragment.getClass().getSimpleName());
        FragmentTransaction fragmentTrx = fragmentManager.beginTransaction();
        fragmentTrx.replace(R.id.main_container, fragment);
        fragmentTrx.commit();
    }

    @Override
    public void onTransactionStarted(TransactionStartedEvent transactionStartedEvent) {
        Log.v(TAG, "onTransactionStarted");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    @Override
    public void onTransactionFinished(final TransactionManager.TransactionStatus transactionStatus,
                                      final TransactionFinishedEvent transactionFinishedEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });

        executor.submit(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "onTransactionFinished: " + transactionStatus.name());
                Log.v(TAG, "Receipt: \n" + transactionContext.getReceipt());
                transactionContext.setTransactionStatus(transactionStatus);

                // Prepare bundle
                Bundle bundle = new Bundle();
                bundle.putString(ReceiptFragment.TEXT_RESULT, "TRANSACTION " + transactionStatus.name());
                bundle.putString(ReceiptFragment.TEXT_RECEIPT, transactionContext.getReceipt());

                if (transactionFinishedEvent != null) {
                    transactionContext.createOrUpdateContext(
                            createTagStore(transactionFinishedEvent.getTlvItems()));
                    transactionContext.setTransactionReferencePersistence(
                            mHostManager.getTransactionReference());
                    if (transactionContext.getTagStore().hasTags()) {
                        Log.d(TAG, "Check CVM result");
                        if (isSignatureRequired(transactionContext.getTagStore())) {
                            Log.d(TAG, "Signature required");
                            // Signature required -> display Signature fragment
                            BaseFragment fragment = BaseFragment.newInstance(SignatureFragment.class, bundle);
                            switchFragment(fragment, false);
                            return;
                        } else {
                            // Signature not required -> save transaction to database now
                            new SaveTransactionTask(transactionContext, null, MainActivity.this).execute();
                        }
                    }
                }
                BaseFragment fragment = BaseFragment.newInstance(ReceiptFragment.class, bundle);
                switchFragment(fragment, false);
            }
        });
    }

    @Override
    public void onOutcomeReceived(OutcomeReceivedEvent outcomeReceivedEvent) {
        Log.v(TAG, "onOutcomeReceived");
    }

    @Override
    public void onDisplayTextRequested(final DisplayTextRequestEvent displayTextRequestEvent) {
        Log.v(TAG, "onDisplayTextRequested");
        executor.submit(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putString(MessageFragment.TEXT, displayTextRequestEvent.getText());
                Fragment fragment = BaseFragment.newInstance(MessageFragment.class, bundle);
                switchFragment(fragment, false);
            }
        });
    }

    @Override
    public void onDisplayMenuRequested(final DisplayMenuRequestEvent displayMenuRequestEvent) {
        Log.v(TAG, "onDisplayMenuRequested");
        executor.submit(new Runnable() {
            @Override
            public void run() {
                MenuFragment fragment = new MenuFragment();
                fragment.setMenuEvent(displayMenuRequestEvent);
                Bundle bundle = new Bundle();
                bundle.putString(MenuFragment.TITLE, displayMenuRequestEvent.getTitle());
                ArrayList<String> list = new ArrayList<>(displayMenuRequestEvent.getChoices());
                bundle.putStringArrayList(MenuFragment.MENU, list);
                fragment.setArguments(bundle);
                switchFragment(fragment, false);
            }
        });
    }

    @Override
    public void onPinRequested(PinRequestEvent pinRequestEvent) {
        Log.v(TAG, "onPinRequested");
        BaseFragment fragment = BaseFragment.newInstance(ProcessingFragment.class);
        final PinRequestEvent event = pinRequestEvent;

        Button button = new Button(this);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        // TODO remove temporary simulation after real PinPad is implemented
        button.setText("SIMULATE PIN");
        button.setTextColor(Color.BLACK);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCommand(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            event.sendPin("1234");
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to send PIN to payment framework");
                        }
                    }
                });
            }
        });
        fragment.setSimulationView(button);
        switchFragment(fragment, false);
    }

    @Override
    public void onOnlineRequested(final OnlineRequestEvent onlineRequestEvent) {
        transactionContext.createOrUpdateContext(createTagStore(onlineRequestEvent.getTlvItems()));
        Log.d(TAG, "onOnlineRequested: " + listTags(transactionContext.getTagStore()));

        Log.v(TAG, "onOnlineRequested");
        dispatchCommand(new Runnable() {
            @Override
            public void run() {
                authorizationRequest(onlineRequestEvent);
            }
        });
    }

    private void authorizationRequest(final OnlineRequestEvent onlineRequestEvent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String gatewayPreference = prefs.getString(mGatewayPreferencesKey, mPrefGatewayYello);
        if (gatewayPreference.equals(mPrefGatewayDisable)) {
            // APPROVAL simulation
            try {
                onlineRequestEvent.sendApproved();
            } catch (IOException e) {
                Log.i(TAG, "Failed to confirm online request: ", e);
                showToastOnUI("Failed to confirm online request.");
            }
            return;
        }

        try {
            mHostManager.sendTransaction(transactionContext, onlineRequestEvent);
            transactionContext.setTransactionReferencePersistence(
                    mHostManager.getTransactionReference());
        } catch (IOException e) {
            Log.i(TAG, "Failed to confirm online request: ", e);
            //TODO define the error cases (showing error, or restarting transaction etc...)
            showToastOnUI("Failed to confirm online request.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to perform authorization.", e);
            showToastOnUI("Failed to perform authorization.");
        }
    }

    @Override
    public void onDelayedAuthorizationReceived(DelayedAuthorizationEvent delayedAuthorizationEvent) {
        Log.v(TAG, "onDelayedAuthorizationReceived");
    }

    @Override
    public void onReceiptDataReceived(ReceiptDataEvent receiptDataEvent) {
        Log.v(TAG, "onReceiptDataReceived");
        transactionContext.addToReceipt(receiptDataEvent.getReceiptLine());
    }

    @Override
    public void onNotifyKernelIdReceived(NotifyKernelIdEvent notifyKernelIdEvent) {
        Log.v(TAG, "onNotifyKernelIdReceived: " + notifyKernelIdEvent.getKernelId());
        transactionContext.setKernelId(notifyKernelIdEvent.getKernelId());
    }

    @Override
    public void onGetAmountRequested(final GetAmountRequestEvent getAmountRequestEvent) {
        Log.v(TAG, "onGetAmountRequested");
        dispatchCommand(new Runnable() {
            @Override
            public void run() {
                try {
                    getAmountRequestEvent.sendAmounts(transactionContext.getAmount(),
                            transactionContext.getAmountOther());
                } catch (IOException e) {
                    Log.i(TAG, "Failed to provide the amounts: ", e);
                    //TODO define the error cases (showing error, or restarting transaction etc...)
                    showToastOnUI("Failed to provide the amounts to L2 kernel.");
                }
            }
        });
    }

    @Override
    public void onReversalReceived(OnlineReversalEvent onlineReversalEvent) {
        transactionContext.createOrUpdateContext(createTagStore(onlineReversalEvent.getTlvItems()));
        Log.d(TAG, "onReversalReceived: " + listTags(transactionContext.getTagStore()));

        try {
            mHostManager.sendReversal(transactionContext, onlineReversalEvent);
        } catch (IOException | GeneralSecurityException e) {
            Log.e(TAG, "Failed to perform reversal.", e);
        }
    }

    @Override
    public void onDekRequest(DekRequestEvent dekRequestEvent) {
        Log.v(TAG, "onDekRequest");
    }

    @Override
    public void showLoading(final boolean show) {
        Log.i(TAG, "showLoading: " + show);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRelativeLayoutLoading.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    public PrinterManager getPrinterManager() {
        return printerManager;
    }

    private void connectToPrinter() {
        String addr = PreferenceManager.getDefaultSharedPreferences(this).getString("printer_address", "");
        new PrinterTask(addr).execute();
    }

    @Override
    public void finishEvent(PrinterEvent event) {
        showLoading(false);
        ArrayList<PrinterInfo> list = printerManager.getFoundPrinter();
        if (list.size() > 0) {
            String printerAddress = list.get(0).getBluetoothAddress();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("printer_address", printerAddress).apply();
            connectToPrinter();
        } else {
            showToastOnUI(getString(R.string.printer_error_search));
        }
    }

    public class PrinterTask extends AsyncTask<Void, Void, String> {

        private String address;

        public PrinterTask(String address) {
            this.address = address;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading(true);

            if (address.isEmpty()) {
                showToastOnUI(getString(R.string.printer_search));
            } else {
                showToastOnUI(getString(R.string.printer_connect) + address);
            }
        }

        @Override
        protected String doInBackground(Void... aVoid) {

            String result = "";

            if (address.equals("")) {
                try {
                    getPrinterManager().startDiscoveryPrinter(MainActivity.this, getApplicationContext());
                } catch (PrinterException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    result = e.getLocalizedMessage();
                }
            } else {
                try {
                    printerManager.connect(PrinterManager.PRINTER_MODEL_MP_B20, address, true);
                } catch (PrinterException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    result = e.getLocalizedMessage();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String message = "";

            if (address.equals("")) {
                if (!result.isEmpty()) {
                    message = getString(R.string.printer_error_search) + ": " + result;
                    showLoading(false);
                }
            } else {
                message = result.isEmpty() ? getString(R.string.printer_success_connect) + address :
                        getString(R.string.printer_error_connect) + address + ": " + result;
                showLoading(false);
            }

            if (!message.isEmpty()) {
                showToastOnUI(message);
            }
        }
    }

    private class PreferencesListener implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.equals(mGatewayPreferencesKey))
                initOnline(sharedPreferences);
        }
    }
}