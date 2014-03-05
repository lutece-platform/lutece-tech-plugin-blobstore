/*
 * Copyright (c) 2002-2013, Mairie de Paris
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
package fr.paris.lutece.plugins.blobstore.service.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore;
import fr.paris.lutece.plugins.blobstore.business.filesystem.FileAlreadyExistsException;
import fr.paris.lutece.plugins.blobstore.business.filesystem.FileSystemBlobStoreHome;
import fr.paris.lutece.plugins.blobstore.business.filesystem.IFileSystemBlobStoreHome;
import fr.paris.lutece.plugins.blobstore.service.IBlobStoreService;
import fr.paris.lutece.plugins.blobstore.service.download.IBlobStoreDownloadUrlService;
import fr.paris.lutece.plugins.blobstore.service.download.JSPBlobStoreDownloadUrlService;
import fr.paris.lutece.plugins.blobstore.util.BlobStoreLibUtils;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;


/**
 * 
 * FileSystemBlobStoreService
 * 
 */
public class FileSystemBlobStoreService implements IBlobStoreService
{
    private static final long serialVersionUID = 1L;
    private String _strBasePath;
    private String _strName;

    /** Uses {@link JSPBlobStoreDownloadUrlService} as default one */
    private IBlobStoreDownloadUrlService _downloadUrlService = new JSPBlobStoreDownloadUrlService( );

    /**
     * Gets the downloadService.
     * @return the downloadService
     */
    public IBlobStoreDownloadUrlService getDownloadUrlService( )
    {
        return _downloadUrlService;
    }

    /**
     * Sets the downloadService
     * @param downloadUrlService downloadService
     */
    public void setDownloadUrlService( IBlobStoreDownloadUrlService downloadUrlService )
    {
        _downloadUrlService = downloadUrlService;
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
    public String getName( )
    {
        return _strName;
    }

    /**
     * Sets the base directory
     * @param strBasePath base path
     */
    public void setBasePath( String strBasePath )
    {
        if ( strBasePath == null )
        {
            AppLogService.error( "Base path is not configured for FileSystemBlobStoreService" );
        }

        _strBasePath = strBasePath;

        if ( !strBasePath.endsWith( "/" ) )
        {
            _strBasePath += File.separator;
        }
    }

    /**
     * Gets the base directory
     * @return the base directory
     */
    public String getBasePath( )
    {
        return _strBasePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( String strKey )
    {
        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            fileSystemBlobStoreHome.remove( strKey, getBasePath( ) );
        }
        catch ( IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getBlob( String strKey )
    {
        BytesBlobStore blob;

        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            blob = fileSystemBlobStoreHome.findByPrimaryKey( strKey, getBasePath( ) );
        }
        catch ( IOException e )
        {
            AppLogService.error( e.getMessage( ), e );

            return null;
        }

        return ( blob == null ) ? null : blob.getValue( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getBlobInputStream( String strKey )
    {
        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            return fileSystemBlobStoreHome.findByPrimaryKeyInputStream( strKey, getBasePath( ) );
        }
        catch ( IOException ioe )
        {
            AppLogService.error( ioe.getMessage( ), ioe );

            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String store( byte[] blob )
    {
        String strKey = BlobStoreLibUtils.generateNewIdBlob( );
        BytesBlobStore blobStore = new BytesBlobStore( );
        blobStore.setId( strKey );
        blobStore.setValue( blob );

        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            fileSystemBlobStoreHome.create( blobStore, getBasePath( ) );
        }
        catch ( IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
        catch ( FileAlreadyExistsException fe )
        {
            throw new AppException( fe.getMessage( ), fe );
        }

        return strKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String storeInputStream( InputStream inputStream )
    {
        String strKey = BlobStoreLibUtils.generateNewIdBlob( );
        InputStreamBlobStore blob = new InputStreamBlobStore( );
        blob.setId( strKey );
        blob.setInputStream( inputStream );

        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            fileSystemBlobStoreHome.createInputStream( blob, getBasePath( ) );
        }
        catch ( IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
        catch ( FileAlreadyExistsException fe )
        {
            throw new AppException( fe.getMessage( ), fe );
        }

        return strKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update( String strKey, byte[] blob )
    {
        BytesBlobStore blobStore = new BytesBlobStore( );
        blobStore.setId( strKey );
        blobStore.setValue( blob );

        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            fileSystemBlobStoreHome.update( blobStore, getBasePath( ) );
        }
        catch ( IOException e )
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
        InputStreamBlobStore blob = new InputStreamBlobStore( );
        blob.setId( strKey );
        blob.setInputStream( inputStream );

        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            fileSystemBlobStoreHome.updateInputStream( blob, getBasePath( ) );
        }
        catch ( IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
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
