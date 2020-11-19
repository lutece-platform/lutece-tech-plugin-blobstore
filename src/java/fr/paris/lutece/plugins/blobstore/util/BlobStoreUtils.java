/*
 * Copyright (c) 2002-2020, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.blobstore.util;

import fr.paris.lutece.plugins.blobstore.service.BlobStorePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.signrequest.AbstractPrivateKeyAuthenticator;

import java.util.UUID;

/**
 * BlobStoreUtils.
 */
public final class BlobStoreUtils
{
    /** The Constant BEAN_REQUEST_AUTHENTICATOR. */
    public static final String BEAN_REQUEST_AUTHENTICATOR = "blobstore.requestAuthenticator";

    /**
     * Private constructor.
     */
    private BlobStoreUtils( )
    {
    }

    /**
     * Generate a new random ID blob.
     *
     * @return a new random id blob
     */
    public static String generateNewIdBlob( )
    {
        UUID key = UUID.randomUUID( );

        return key.toString( );
    }

    /**
     * Gets the {@link AbstractPrivateKeyAuthenticator}.
     *
     * @return the RequestAuthenticator
     */
    public static AbstractPrivateKeyAuthenticator getRequestAuthenticator( )
    {
        return (AbstractPrivateKeyAuthenticator) SpringContextService.getBean( BEAN_REQUEST_AUTHENTICATOR );
    }

    /**
     * Get the workflow plugin.
     *
     * @return the workflow plugin
     */
    public static Plugin getPlugin( )
    {
        return PluginService.getPlugin( BlobStorePlugin.PLUGIN_NAME );
    }
}
