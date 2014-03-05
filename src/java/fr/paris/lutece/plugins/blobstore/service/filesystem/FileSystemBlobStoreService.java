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
import fr.paris.lutece.plugins.blobstore.util.BlobStoreUtils;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;


/**
 * FileSystemBlobStoreService.
 */
public class FileSystemBlobStoreService implements IBlobStoreService
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The base path. */
    private String _strBasePath;

    /** The name. */
    private String _strName;

    /** The depth. */
    private Integer _intDepth = 0;

    /** Uses {@link JSPBlobStoreDownloadUrlService} as default one. */
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
    public void setDownloadUrlService( final IBlobStoreDownloadUrlService downloadUrlService )
    {
        _downloadUrlService = downloadUrlService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.paris.lutece.portal.service.blobstore.BlobStoreService#setName(java
     * .lang.String)
     */
    @Override
    public void setName( final String strName )
    {
        _strName = strName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#getName()
     */
    @Override
    public String getName( )
    {
        return _strName;
    }

    /**
     * Sets the base directory.
     * 
     * @param strBasePath
     *            base path
     */
    public void setBasePath( final String strBasePath )
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
     * Gets the base directory.
     * 
     * @return the base directory
     */
    public String getBasePath( )
    {
        return _strBasePath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.paris.lutece.portal.service.blobstore.BlobStoreService#delete(java
     * .lang.String)
     */
    @Override
    public void delete( final String strKey )
    {
        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            fileSystemBlobStoreHome.remove( strKey, getBasePath( ), getDepth( ) );
        }
        catch ( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.paris.lutece.portal.service.blobstore.BlobStoreService#getBlob(java
     * .lang.String)
     */
    @Override
    public byte[] getBlob( final String strKey )
    {
        final BytesBlobStore blob;

        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            blob = fileSystemBlobStoreHome.findByPrimaryKey( strKey, getBasePath( ), getDepth( ) );
        }
        catch ( final IOException e )
        {
            AppLogService.error( e.getMessage( ), e );

            return null;
        }

        return ( blob == null ) ? null : blob.getValue( );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.paris.lutece.portal.service.blobstore.BlobStoreService#getBlobInputStream
     * (java.lang.String)
     */
    @Override
    public InputStream getBlobInputStream( final String strKey )
    {
        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );

            return fileSystemBlobStoreHome.findByPrimaryKeyInputStream( strKey, getBasePath( ), getDepth( ) );
        }
        catch ( final IOException ioe )
        {
            AppLogService.error( ioe.getMessage( ), ioe );

            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.paris.lutece.portal.service.blobstore.BlobStoreService#store(byte[])
     */
    @Override
    public String store( final byte[] blob )
    {
        final String strKey = BlobStoreUtils.generateNewIdBlob( );
        final BytesBlobStore blobStore = new BytesBlobStore( );
        blobStore.setId( strKey );
        blobStore.setValue( blob );

        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            fileSystemBlobStoreHome.create( blobStore, getBasePath( ), getDepth( ) );
        }
        catch ( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
        catch ( final FileAlreadyExistsException fe )
        {
            throw new AppException( fe.getMessage( ), fe );
        }

        return strKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.paris.lutece.portal.service.blobstore.BlobStoreService#storeInputStream
     * (java.io.InputStream)
     */
    @Override
    public String storeInputStream( final InputStream inputStream )
    {
        final String strKey = BlobStoreUtils.generateNewIdBlob( );
        final InputStreamBlobStore blob = new InputStreamBlobStore( );
        blob.setId( strKey );
        blob.setInputStream( inputStream );

        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            fileSystemBlobStoreHome.createInputStream( blob, getBasePath( ), getDepth( ) );
        }
        catch ( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
        catch ( final FileAlreadyExistsException fe )
        {
            throw new AppException( fe.getMessage( ), fe );
        }

        return strKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.paris.lutece.portal.service.blobstore.BlobStoreService#update(java
     * .lang.String, byte[])
     */
    @Override
    public void update( final String strKey, final byte[] blob )
    {
        final BytesBlobStore blobStore = new BytesBlobStore( );
        blobStore.setId( strKey );
        blobStore.setValue( blob );

        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            fileSystemBlobStoreHome.update( blobStore, getBasePath( ), getDepth( ) );
        }
        catch ( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.paris.lutece.portal.service.blobstore.BlobStoreService#updateInputStream
     * (java.lang.String, java.io.InputStream)
     */
    @Override
    public void updateInputStream( final String strKey, final InputStream inputStream )
    {
        final InputStreamBlobStore blob = new InputStreamBlobStore( );
        blob.setId( strKey );
        blob.setInputStream( inputStream );

        try
        {
            IFileSystemBlobStoreHome fileSystemBlobStoreHome = SpringContextService
                    .getBean( FileSystemBlobStoreHome.BEAN_SERVICE );
            fileSystemBlobStoreHome.updateInputStream( blob, getBasePath( ), getDepth( ) );
        }
        catch ( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.paris.lutece.portal.service.blobstore.BlobStoreService#getBlobUrl(
     * java.lang.String)
     */
    @Override
    public String getBlobUrl( final String strKey )
    {
        return _downloadUrlService.getDownloadUrl( getName( ), strKey );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.paris.lutece.portal.service.blobstore.BlobStoreService#getFileUrl(
     * java.lang.String)
     */
    @Override
    public String getFileUrl( final String strKey )
    {
        return _downloadUrlService.getFileUrl( getName( ), strKey );
    }

    /**
     * Gets the depth.
     * 
     * @return the depth
     */
    public Integer getDepth( )
    {
        return _intDepth;
    }

    /**
     * Sets the depth.
     * 
     * @param depth
     *            the new depth
     */
    public void setDepth( final Integer depth )
    {
        if ( depth != null )
        {
            this._intDepth = depth;
        }
    }
}
