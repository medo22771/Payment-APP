package com.yelloco.payment.transaction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import android.content.Context;
import android.content.SharedPreferences;

import com.yelloco.payment.CustomEditor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

public class SharedPreferencesTransactionReferencePersistenceTest {
    private static final int MAX_VALUE = 999999;

    private static final String SHARED_PREFS_FILE_NAME = "sharedFileName";

    private static final int aValue = 10;

    @Mock
    private SharedPreferences sharedPrefs;

    @Mock
    private Context context;

    private TransactionReferencePersistence transactionReferencePersistence;

    private CustomEditor customEditor;

    @Before
    public void setup() throws IOException {
        initMocks(this);
        sharedPrefs = mock(SharedPreferences.class);
        context = mock(Context.class);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
        customEditor = new CustomEditor();
        when(sharedPrefs.edit()).thenReturn(customEditor);
        transactionReferencePersistence = new SharedPreferencesTransactionReferencePersistence(
                context, SHARED_PREFS_FILE_NAME,
                MAX_VALUE);
        when(sharedPrefs.getInt(anyString(), anyInt())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return customEditor.getStoredValue();
            }
        });
    }

    @Test
    public void isInitialRefReturnedWhenQueriedForFirstTime() {
        int firstRef = transactionReferencePersistence.getAndIncrementRef();

        assertEquals(firstRef, 1);
    }

    @Test
    public void isIncrementedRefReturnedWhenQueriedAfterStored() {
        transactionReferencePersistence.setValue(aValue);

        assertEquals(transactionReferencePersistence.getAndIncrementRef(), aValue + 1);
    }

    @Test
    public void isRefValueRotatedWhenMaximumIsReached() {
        transactionReferencePersistence.setValue(MAX_VALUE);

        assertEquals(transactionReferencePersistence.getAndIncrementRef(), 1);
    }
}