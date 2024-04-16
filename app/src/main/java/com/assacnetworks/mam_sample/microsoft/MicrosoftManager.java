package com.assacnetworks.mam_sample.microsoft;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalIntuneAppProtectionPolicyRequiredException;
import com.microsoft.identity.client.exception.MsalUserCancelException;
import com.microsoft.intune.mam.policy.MAMEnrollmentManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MicrosoftManager {
    private static final Logger LOGGER = Logger.getLogger(MicrosoftManager.class.getName());

    private Context context;
    private Activity activity;
    private AppAccount mUserAccount;
    private MAMEnrollmentManager mEnrollmentManager;
    public static final String[] MSAL_SCOPES = {"https://graph.microsoft.com/User.Read"};

    public MicrosoftManager(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void onClickSignIn() {
        // initiate the MSAL authentication on a background thread
        Thread thread = new Thread(() -> {
            LOGGER.info("Starting interactive auth");

            try {
                String loginHint = null;
                if (mUserAccount != null) {
                    loginHint = mUserAccount.getUPN();
                }
                MSALUtil.acquireToken(activity, MSAL_SCOPES, loginHint, new AuthCallback());
            } catch (MsalException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, "getString(R.string.err_auth)", e);
                showMessage("Authentication exception occurred - check logcat for more details.");
            }
        });
        thread.start();
    }

    private void showMessage(final String message) {
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private class AuthCallback implements AuthenticationCallback {
        @Override
        public void onError(final MsalException exc) {
            LOGGER.log(Level.SEVERE, "authentication failed", exc);

            if (exc instanceof MsalIntuneAppProtectionPolicyRequiredException) {
                MsalIntuneAppProtectionPolicyRequiredException appException = (MsalIntuneAppProtectionPolicyRequiredException) exc;

                // Note: An app that has enabled APP CA with Policy Assurance would need to pass these values to `remediateCompliance`.
                // For more information, see https://docs.microsoft.com/en-us/mem/intune/developer/app-sdk-android#app-ca-with-policy-assurance
                final String upn = appException.getAccountUpn();
                final String aadid = appException.getAccountUserId();
                final String tenantId = appException.getTenantId();
                final String authorityURL = appException.getAuthorityUrl();

                // The user cannot be considered "signed in" at this point, so don't save it to the settings.
                mUserAccount = new AppAccount(upn, aadid, tenantId, authorityURL);

                final String message = "Intune App Protection Policy required.";
                showMessage(message);

                LOGGER.info("MsalIntuneAppProtectionPolicyRequiredException received.");
                LOGGER.info(String.format("Data from broker: UPN: %s; AAD ID: %s; Tenant ID: %s; Authority: %s",
                        upn, aadid, tenantId, authorityURL));
            } else if (exc instanceof MsalUserCancelException) {
                showMessage("User cancelled sign-in request");
            } else {
                showMessage("Exception occurred - check logcat");
            }
        }

        @Override
        public void onSuccess(final IAuthenticationResult result) {
            IAccount account = result.getAccount();

            final String upn = account.getUsername();
            final String aadId = account.getId();
            final String tenantId = account.getTenantId();
            final String authorityURL = account.getAuthority();

            String message = "Authentication succeeded for user " + upn;
            LOGGER.info(message);

            // Save the user account in the settings, since the user is now "signed in".
            mUserAccount = new AppAccount(upn, aadId, tenantId, authorityURL);
            AppSettings.saveAccount(context, mUserAccount);

            // Register the account for MAM.
            mEnrollmentManager.registerAccountForMAM(upn, aadId, tenantId, authorityURL);

//            displayMainView();
        }

        @Override
        public void onCancel() {
            showMessage("User cancelled auth attempt");
        }
    }
}
