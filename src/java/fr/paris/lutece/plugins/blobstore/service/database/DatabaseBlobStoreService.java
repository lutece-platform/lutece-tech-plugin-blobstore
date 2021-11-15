/*
 * Copyright (c) 2002-2021, City of Paris
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

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore;
import fr.paris.lutece.plugins.blobstore.business.database.DatabaseBlobStoreHome;
import fr.paris.lutece.plugins.blobstore.business.database.IDatabaseBlobStoreHome;
import fr.paris.lutece.plugins.blobstore.service.BlobStoreFileItem;
import fr.paris.lutece.plugins.blobstore.service.IBlobStoreService;
import fr.paris.lutece.plugins.blobstore.service.download.IBlobStoreDownloadUrlService;
import fr.paris.lutece.plugins.blobstore.service.download.JSPBlobStoreDownloadUrlService;
import fr.paris.lutece.plugins.blobstore.util.BlobStoreLibUtils;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import java.io.IOException;
import org.apache.commons.fileupload.FileItem;

/**
 * 
 * DatabaseBlobStoreService.
 * 
 */
public class DatabaseBlobStoreService implements IBlobStoreService
{
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE_COULD_NOT_CREATE_BLOB = "BlobStore Error when generating a new id blob";

    /**
     * name defaulted to databaseBlobstore - only one can be supported by webapp
     */
    private String _strName = "databaseBlobstore";

    /** Uses {@link JSPBlobStoreDownloadUrlService} as default one */
    private IBlobStoreDownloadUrlService _downloadUrlService = new JSPBlobStoreDownloadUrlService( );

    /**
     * Gets the downloadService
     * 
     * @return the downloadService
     */
    public IBlobStoreDownloadUrlService getDownloadUrlService( )
    {
        return _downloadUrlService;
    }

    /**
     * Sets the downloadService
     * 
     * @param downloadUrlService
     *            downloadService
     */
    public void setDownloadUrlService( IBlobStoreDownloadUrlService downloadUrlService )
    {
        _downloadUrlService = downloadUrlService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName( )
    {
        return _strName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( String strKey )
    {
        IDatabaseBlobStoreHome databaseBlobStoreHome = SpringContextService.getBean( DatabaseBlobStoreHome.BEAN_SERVICE );
        databaseBlobStoreHome.remove( strKey );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte [ ] getBlob( String strKey )
    {
        byte [ ] blob = null;

        if ( StringUtils.isNotBlank( strKey ) )
        {
            IDatabaseBlobStoreHome databaseBlobStoreHome = SpringContextService.getBean( DatabaseBlobStoreHome.BEAN_SERVICE );
            BytesBlobStore blobStore = databaseBlobStoreHome.findByPrimaryKey( strKey );

            if ( blobStore != null )
            {
                blob = blobStore.getValue( );
            }
        }

        return blob;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String store( byte [ ] blob )
    {
        String strKey = BlobStoreLibUtils.generateNewIdBlob( );

        if ( StringUtils.isNotBlank( strKey ) )
        {
            BytesBlobStore blobStore = new BytesBlobStore( );
            blobStore.setId( strKey );
            blobStore.setValue( blob );

            IDatabaseBlobStoreHome databaseBlobStoreHome = SpringContextService.getBean( DatabaseBlobStoreHome.BEAN_SERVICE );
            databaseBlobStoreHome.create( blobStore );
        }
        else
        {
            AppLogService.error( MESSAGE_COULD_NOT_CREATE_BLOB );
        }

        return strKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update( String strKey, byte [ ] blob )
    {
        if ( StringUtils.isNotBlank( strKey ) )
        {
            IDatabaseBlobStoreHome databaseBlobStoreHome = SpringContextService.getBean( DatabaseBlobStoreHome.BEAN_SERVICE );
            BytesBlobStore blobStore = databaseBlobStoreHome.findByPrimaryKey( strKey );

            if ( blobStore != null )
            {
                blobStore.setValue( blob );
                databaseBlobStoreHome.update( blobStore );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String storeInputStream( InputStream inputStream )
    {
        String strKey = BlobStoreLibUtils.generateNewIdBlob( );

        if ( StringUtils.isNotBlank( strKey ) )
        {
            InputStreamBlobStore blobStore = new InputStreamBlobStore( );
            blobStore.setInputStream( inputStream );
            blobStore.setId( strKey );

            IDatabaseBlobStoreHome databaseBlobStoreHome = SpringContextService.getBean( DatabaseBlobStoreHome.BEAN_SERVICE );
            databaseBlobStoreHome.createInputStream( blobStore );
        }
        else
        {
            AppLogService.error( MESSAGE_COULD_NOT_CREATE_BLOB );
        }

        return strKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#storeFileItem (java.io.InputStream)
     */
    @Override
    public String storeFileItem( FileItem fileItem )
    {
        try
        {
            // store the file content
            String strBlobContentKey = storeInputStream( fileItem.getInputStream( ) );

            // build metadata as json file and store it
            String strMetadata = BlobStoreFileItem.buildFileMetadata( fileItem.getName( ), fileItem.getSize( ), strBlobContentKey, fileItem.getContentType( ) );
            String strMetadataKey = store( strMetadata.getBytes( ) );

            return strMetadataKey;
        }
        catch( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateInputStream( String strKey, InputStream inputStream )
    {
        InputStreamBlobStore blobStore = new InputStreamBlobStore( );
        blobStore.setInputStream( inputStream );
        blobStore.setId( strKey );

        IDatabaseBlobStoreHome databaseBlobStoreHome = SpringContextService.getBean( DatabaseBlobStoreHome.BEAN_SERVICE );
        databaseBlobStoreHome.updateInputStream( blobStore );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getBlobInputStream( String strKey )
    {
        IDatabaseBlobStoreHome databaseBlobStoreHome = SpringContextService.getBean( DatabaseBlobStoreHome.BEAN_SERVICE );

        return databaseBlobStoreHome.findByPrimaryKeyInputStream( strKey );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBlobUrl( String strKey )
    {
        return _downloadUrlService.getDownloadUrl( getName( ), strKey );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileUrl( String strKey )
    {
        return _downloadUrlService.getFileUrl( getName( ), strKey );
    }
}
