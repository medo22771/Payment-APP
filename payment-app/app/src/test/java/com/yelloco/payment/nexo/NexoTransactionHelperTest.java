package com.yelloco.payment.nexo;

import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.AUTQ;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.CMPV;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.FAUQ;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.FCMV;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.FRVA;
import static com.yelloco.nexo.message.acquirer.MessageFunction1Code.RVRA;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorisationType.OFFLINE;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorisationType.ONLINE;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorizationResult.APPROVED;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorizationResult.DECLINED;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorizationResult.FAILURE;
import static com.yelloco.payment.transaction.AuthorisationContext.AuthorizationResult
        .NO_ACCEPTABLE_RESPONSE;
import static com.yelloco.payment.transaction.AuthorisationContext.CaptureType.AUTHORIZATION;
import static com.yelloco.payment.transaction.AuthorisationContext.CaptureType.BATCH;
import static com.yelloco.payment.transaction.AuthorisationContext.CaptureType.COMPLETION;
import static com.yelloco.payment.transaction.AuthorisationContext.CompletionExchange.NONE;
import static com.yelloco.payment.transaction.AuthorisationContext.CompletionExchange.ON_DEMAND;
import static com.yelloco.payment.transaction.AuthorisationContext.IncidentAfterAuth.MALFUNCTION;
import static com.yelloco.payment.transaction.AuthorisationContext.IncidentAfterAuth.NO_INCIDENT;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.MockitoAnnotations.initMocks;

import android.content.SharedPreferences;

import com.yelloco.payment.transaction.AuthorisationContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NexoTransactionHelperTest {

    @Spy
    private SharedPreferences preferences;

    private NexoTransactionHelper classUnderTest;

    @Before
    public void setUp() {
        initMocks(this);

        classUnderTest = new NexoTransactionHelper(preferences);
    }

    @Test
    public void canTheTestClassBeCreatedWhenPreferencesMocked() {
        assertThat(classUnderTest, is(not(nullValue())));
    }

    @Test
    public void isCaseAuth1FulfilledWhenProperConditionsSet() {
        setupClassUnderTest(ONLINE, APPROVED, NO_INCIDENT, true, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth1FulfilledWhenProperConditionsSetAndMerchantNotForced() {
        setupClassUnderTest(ONLINE, APPROVED, NO_INCIDENT, false, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth2FulfilledWhenProperConditionsSetAndMerchantNotForced() {
        setupClassUnderTest(ONLINE, APPROVED, NO_INCIDENT, false, AUTHORIZATION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth3FulfilledWhenProperConditionsSet() {
        setupClassUnderTest(ONLINE, APPROVED, NO_INCIDENT, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth4FulfilledWhenProperConditionsSet() {
        setupClassUnderTest(ONLINE, APPROVED, NO_INCIDENT, false, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth5FulfilledWhenProperConditionsSet() {
        setupClassUnderTest(ONLINE, APPROVED, NO_INCIDENT, false, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth6FulfilledWhenProperConditionsSetAndOnDemandCompletion() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, false, AUTHORIZATION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth6FulfilledWhenProperConditionsSetAndNoCompletion() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, false, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth7FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth8FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, false, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth9FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, true, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth10FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, true, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth11FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, true, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth12FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, false, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth13FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, false, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth14FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth15FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, false, COMPLETION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth16FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, false, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth17FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, false, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth18FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, true, AUTHORIZATION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth19FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, true, COMPLETION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth20FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, true, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth21FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, true, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth22FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, false, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth23FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, false, AUTHORIZATION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth24FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth25FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, false, COMPLETION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth26FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, false, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth27FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, false, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth28FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, true, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth29FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, true, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth30FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, true, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth31FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, true, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth32FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, false, AUTHORIZATION,
                ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }


    @Test
    public void isCaseAuth33FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth34FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, false, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth35FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, true, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth36FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, true, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth37FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, true, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth38FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, false, AUTHORIZATION,
                NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth39FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth40FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, false, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth41FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, true, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(FAUQ)));
    }

    @Test
    public void isCaseAuth42FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, true, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    @Test
    public void isCaseAuth43FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, true, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(AUTQ), is(equalTo(AUTQ)));
    }

    /*------------------------------  COMPLETIONS  ------------------------------*/

    @Test
    public void isCaseCompl2FulfilledWhenProperConditionsSetAndMerchantNotForced() {
        setupClassUnderTest(ONLINE, APPROVED, NO_INCIDENT, false, AUTHORIZATION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl3FulfilledWhenProperConditionsSet() {
        setupClassUnderTest(ONLINE, APPROVED, NO_INCIDENT, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl5FulfilledWhenProperConditionsSet() {
        setupClassUnderTest(ONLINE, APPROVED, NO_INCIDENT, false, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl6FulfilledWhenProperConditionsSetAndOnDemandCompletion() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, false, AUTHORIZATION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FRVA)));
    }

    @Test
    public void isCaseCompl6FulfilledWhenProperConditionsSetAndNoCompletion() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, false, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FRVA)));
    }

    @Test
    public void isCaseCompl7FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(RVRA)));
    }

    @Test
    public void isCaseCompl8FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, false, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(RVRA)));
    }

    @Test
    public void isCaseCompl9FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, true, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl10FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, true, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl11FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, APPROVED, MALFUNCTION, true, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl13FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, false, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl15FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, false, COMPLETION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl17FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, false, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl18FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, true, AUTHORIZATION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl19FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, true, COMPLETION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl21FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, NO_INCIDENT, true, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl23FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, false, AUTHORIZATION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl25FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, false, COMPLETION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl27FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, false, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl28FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, true, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl29FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, true, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl31FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, DECLINED, MALFUNCTION, true, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl32FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, false, AUTHORIZATION,
                ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FRVA)));
    }


    @Test
    public void isCaseCompl33FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(RVRA)));
    }

    @Test
    public void isCaseCompl34FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, false, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(RVRA)));
    }

    @Test
    public void isCaseCompl35FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, true, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl36FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, true, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl37FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, NO_INCIDENT, true, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl38FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, false, AUTHORIZATION,
                NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FRVA)));
    }

    @Test
    public void isCaseCompl39FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(RVRA)));
    }

    @Test
    public void isCaseCompl40FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, false, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(RVRA)));
    }

    @Test
    public void isCaseCompl41FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, true, AUTHORIZATION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl42FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, true, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl43FulfilledWhenProperConditions() {
        setupClassUnderTest(ONLINE, NO_ACCEPTABLE_RESPONSE, MALFUNCTION, true, BATCH, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl47FulfilledWhenProperConditions() {
        setupClassUnderTest(OFFLINE, APPROVED, NO_INCIDENT, false, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl49FulfilledWhenProperConditions() {
        setupClassUnderTest(OFFLINE, APPROVED, NO_INCIDENT, false, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl51FulfilledWhenProperConditions() {
        setupClassUnderTest(OFFLINE, DECLINED, NO_INCIDENT, false, COMPLETION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl53FulfilledWhenProperConditions() {
        setupClassUnderTest(OFFLINE, DECLINED, NO_INCIDENT, false, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl54FulfilledWhenProperConditions() {
        setupClassUnderTest(OFFLINE, DECLINED, NO_INCIDENT, true, COMPLETION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl56FulfilledWhenProperConditions() {
        setupClassUnderTest(OFFLINE, DECLINED, NO_INCIDENT, true, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl58FulfilledWhenProperConditions() {
        setupClassUnderTest(OFFLINE, FAILURE, NO_INCIDENT, false, COMPLETION, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl60FulfilledWhenProperConditions() {
        setupClassUnderTest(OFFLINE, FAILURE, NO_INCIDENT, false, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    @Test
    public void isCaseCompl61FulfilledWhenProperConditions() {
        setupClassUnderTest(OFFLINE, FAILURE, NO_INCIDENT, true, COMPLETION, NONE);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(FCMV)));
    }

    @Test
    public void isCaseCompl63FulfilledWhenProperConditions() {
        setupClassUnderTest(OFFLINE, FAILURE, NO_INCIDENT, true, BATCH, ON_DEMAND);

        assertThat(classUnderTest.decideMessageFunction(CMPV), is(equalTo(CMPV)));
    }

    private void setupClassUnderTest(AuthorisationContext.AuthorisationType authorisationType,
            AuthorisationContext.AuthorizationResult authorizationResult,
            AuthorisationContext.IncidentAfterAuth incidentAfterAuth, boolean merchantForced,
            AuthorisationContext.CaptureType captureType,
            AuthorisationContext.CompletionExchange completionExchange) {

        classUnderTest = new NexoTransactionHelper(authorisationType, authorizationResult,
                incidentAfterAuth, merchantForced, captureType, completionExchange);
    }
}