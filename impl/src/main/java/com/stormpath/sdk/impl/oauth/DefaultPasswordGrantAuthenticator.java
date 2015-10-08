/*
* Copyright 2015 Stormpath, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.stormpath.sdk.impl.oauth;

import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.oauth.*;
import com.stormpath.sdk.ds.DataStore;
import com.stormpath.sdk.impl.ds.InternalDataStore;
import com.stormpath.sdk.impl.http.HttpHeaders;
import com.stormpath.sdk.impl.http.MediaType;
import com.stormpath.sdk.lang.Assert;

/**
 * @since 1.0.RC5.1
 */
public class DefaultPasswordGrantAuthenticator implements PasswordGrantAuthenticator {

    private Application application;

    private InternalDataStore dataStore;

    final static String OAUTH_TOKEN_PATH = "/oauth/token";

    public DefaultPasswordGrantAuthenticator(Application application, DataStore dataStore) {
        this.application = application;
        this.dataStore = (InternalDataStore) dataStore;
    }

    @Override
    public OauthGrantAuthenticationResult authenticate(PasswordGrantRequest passwordGrantRequest) {
        Assert.notNull(this.application, "application cannot be null or empty");

        CreateGrantAuthenticationAttempt createGrantAuthenticationAttempt = new DefaultCreateGrantAuthenticationAttempt(dataStore);
        createGrantAuthenticationAttempt.setLogin(passwordGrantRequest.getLogin());
        createGrantAuthenticationAttempt.setPassword(passwordGrantRequest.getPassword());
        createGrantAuthenticationAttempt.setGrantType(passwordGrantRequest.getGrantType());
        if (passwordGrantRequest.getAccountStore() != null){
            createGrantAuthenticationAttempt.setAccountStore(passwordGrantRequest.getAccountStore());
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        GrantAuthenticationToken grantResult = dataStore.create(application.getHref() + OAUTH_TOKEN_PATH, createGrantAuthenticationAttempt, GrantAuthenticationToken.class, httpHeaders);

        OauthGrantAuthenticationResultBuilder builder = new DefaultOauthGrantAuthenticationResultBuilder(grantResult);

        AccessToken test = dataStore.getResource(grantResult.getAccessTokenHref(), AccessToken.class);

        return builder.build();
    }

    // While authenticate method resides in Application class this is not necessary. Leaving here though because this should be refactored soon.
    @Override
    public PasswordGrantAuthenticator forApplication(Application application) {
        Assert.notNull(application, "application cannot be null or empty.");
        this.application = application;
        return this;
    }
}
