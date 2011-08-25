/*
 * Copyright (c) 2002-2011, Mairie de Paris
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
package fr.paris.lutece.plugins.blobstore.service.download;

import fr.paris.lutece.plugins.blobstore.util.BlobStoreConstants;
import fr.paris.lutece.plugins.blobstore.util.BlobStoreUtils;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.url.UrlItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Uses JSP to serve file.
 *
 */
public class JSPBlobStoreDownloadUrlService implements IBlobStoreDownloadUrlService
{
    private static final long serialVersionUID = 1L;

    /**
    * Gets the file url
    * @param strBlobStore the blob store
    * @param strBlobKey the blob key
    * @return the url
    */
    public String getFileUrl( String strBlobStore, String strBlobKey )
    {
        return getBlobUrl( strBlobStore, strBlobKey, BlobStoreConstants.JSP_DO_DOWNLOAD_FILE );
    }

    /**
     * Gets the downloadUrl
     * @param strBlobStore the blobstore
     * @param strBlobKey the key
     * @return the url
     */
    public String getDownloadUrl( String strBlobStore, String strBlobKey )
    {
        return getBlobUrl( strBlobStore, strBlobKey, BlobStoreConstants.JSP_DO_DOWNLOAD_BLOB );
    }

    /**
     * Builds the blob url
     * @param strBlobStore the blob store
     * @param strBlobKey the blob key
     * @param strJsp the jsp
     * @return the blob url
     */
    private static String getBlobUrl( String strBlobStore, String strBlobKey, String strJsp )
    {
        String strBaseUrl = AppPropertiesService.getProperty( BlobStoreConstants.PROPERTY_BASE_URL,
                AppPropertiesService.getProperty( BlobStoreConstants.PROPERTY_PROD_URL ) );
        String strBlobUrl;

        if ( strBaseUrl != null )
        {
            if ( !strBaseUrl.endsWith( "/" ) )
            {
                strBaseUrl += "/";
            }

            UrlItem urlItem = new UrlItem( strBaseUrl + strJsp );
            urlItem.addParameter( BlobStoreConstants.PARAMETER_BLOB_STORE, strBlobStore );
            urlItem.addParameter( BlobStoreConstants.PARAMETER_BLOB_KEY, strBlobKey );

            List<String> listElements = new ArrayList<String>(  );
            listElements.add( strBlobStore );
            listElements.add( strBlobKey );

            String strTimestamp = Long.toString( new Date(  ).getTime(  ) );
            String strSignature = BlobStoreUtils.getRequestAuthenticator(  ).buildSignature( listElements, strTimestamp );

            urlItem.addParameter( BlobStoreConstants.PARAMETER_TIMESTAMP, strTimestamp );
            urlItem.addParameter( BlobStoreConstants.PARAMETER_SIGNATURE, strSignature );

            strBlobUrl = urlItem.getUrl(  );
        }
        else
        {
            strBlobUrl = null;
        }

        return strBlobUrl;
    }
}
