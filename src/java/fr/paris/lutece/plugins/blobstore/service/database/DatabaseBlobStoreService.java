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
package fr.paris.lutece.plugins.blobstore.service.database;

import fr.paris.lutece.plugins.blobstore.business.database.DatabaseBlobStore;
import fr.paris.lutece.plugins.blobstore.business.database.DatabaseBlobStoreHome;
import fr.paris.lutece.portal.service.util.BlobStoreService;

import org.apache.commons.lang.StringUtils;


/**
 *
 * DatabaseBlobStoreService
 *
 */
public class DatabaseBlobStoreService implements BlobStoreService
{
    private static final String FIRST = "1";

    /**
     * Generate a new blob key
     * @return a new blob key
     */
    public String generateNewIdBlob(  )
    {
        // TODO : The key is generated sequentially. For security issues, it is better
        // to have an algorithm that generates automatically the key in a String
        String strKey = DatabaseBlobStoreHome.getLastPrimaryKey(  );

        if ( StringUtils.isNotBlank( strKey ) && StringUtils.isNumeric( strKey ) )
        {
            int nNewKey = Integer.parseInt( strKey ) + 1;
            strKey = Integer.toString( nNewKey );
        }
        else
        {
            strKey = FIRST;
        }

        return strKey;
    }

    /**
     * {@inheritDoc}
     */
    public void delete( String strid )
    {
        DatabaseBlobStoreHome.remove( strid );
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getBlob( String strId )
    {
        byte[] blob = null;

        if ( StringUtils.isNotBlank( strId ) )
        {
            DatabaseBlobStore blobStore = DatabaseBlobStoreHome.findByPrimaryKey( strId );

            if ( blobStore != null )
            {
                blob = blobStore.getValue(  );
            }
        }

        return blob;
    }

    /**
     * {@inheritDoc}
     */
    public String store( byte[] blob )
    {
        DatabaseBlobStore blobStore = new DatabaseBlobStore(  );
        blobStore.setId( generateNewIdBlob(  ) );
        blobStore.setValue( blob );
        DatabaseBlobStoreHome.create( blobStore );

        return blobStore.getId(  );
    }

    /**
     * {@inheritDoc}
     */
    public void update( String strId, byte[] blob )
    {
        if ( StringUtils.isNotBlank( strId ) )
        {
            DatabaseBlobStore blobStore = DatabaseBlobStoreHome.findByPrimaryKey( strId );

            if ( blobStore != null )
            {
                blobStore.setValue( blob );
                DatabaseBlobStoreHome.update( blobStore );
            }
        }
    }
}
