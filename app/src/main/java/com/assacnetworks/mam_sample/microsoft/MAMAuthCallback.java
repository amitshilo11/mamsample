package com.assacnetworks.mam_sample.microsoft;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.microsoft.intune.mam.policy.MAMServiceAuthenticationCallback;

class MAMAuthCallback implements MAMServiceAuthenticationCallback {
    @Nullable
    @Override
    public String acquireToken(@NonNull String upn, @NonNull String aadId, @NonNull String resourceId){

        return null;
    }
}
