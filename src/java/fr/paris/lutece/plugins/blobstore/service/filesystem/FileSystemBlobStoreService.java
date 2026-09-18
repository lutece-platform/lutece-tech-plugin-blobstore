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
package fr.paris.lutece.plugins.blobstore.service.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore;
import fr.paris.lutece.plugins.blobstore.business.filesystem.FileAlreadyExistsException;
import fr.paris.lutece.plugins.blobstore.business.filesystem.IFileSystemBlobStoreHome;
import fr.paris.lutece.plugins.blobstore.service.BlobStoreFileItem;
import fr.paris.lutece.plugins.blobstore.service.IBlobStoreService;
import fr.paris.lutece.plugins.blobstore.service.download.IBlobStoreDownloadUrlService;
import fr.paris.lutece.plugins.blobstore.service.download.JSPBlobStoreDownloadUrlService;
import fr.paris.lutece.plugins.blobstore.util.BlobStoreUtils;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

import fr.paris.lutece.portal.service.upload.MultipartItem;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * FileSystemBlobStoreService.
 */
@ApplicationScoped
@Named( FileSystemBlobStoreService.BEAN_SERVICE )
public class FileSystemBlobStoreService implements IBlobStoreService
{
    /** The CDI bean name of the file system blob store shipped by the plugin. */
    public static final String BEAN_SERVICE = "blobstore.fileSystemBlobStoreService";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The configuration key holding the base path. */
    private static final String PROPERTY_BASE_PATH = "blobstore.file.system.path";

    /** The configuration key holding the depth. */
    private static final String PROPERTY_DEPTH = "blobstore.file.system.depth";

    /** The base path used when the key holds no value. */
    private static final String DEFAULT_BASE_PATH = "/var/blobs/";

    /** The depth used when the key holds no value. */
    private static final String DEFAULT_DEPTH = "1";

    /** The base path. */
    private String _strBasePath;

    /** The name : it is put in the download URLs, which resolve the store back by that name. */
    @Inject
    @ConfigProperty( name = "blobstore.fileSystemBlobStoreService.name", defaultValue = BEAN_SERVICE )
    private String _strName;

    /** The depth. */
    private Integer _intDepth = 0;

    /** The configured base path. */
    @Inject
    @ConfigProperty( name = PROPERTY_BASE_PATH, defaultValue = DEFAULT_BASE_PATH )
    private String _strConfiguredBasePath;

    /** The configured depth. */
    @Inject
    @ConfigProperty( name = PROPERTY_DEPTH, defaultValue = DEFAULT_DEPTH )
    private Integer _nConfiguredDepth;

    /** Uses {@link JSPBlobStoreDownloadUrlService} as default one. */
    private IBlobStoreDownloadUrlService _downloadUrlService = new JSPBlobStoreDownloadUrlService( );

    /** The home of the blobs stored on the file system. */
    @Inject
    private IFileSystemBlobStoreHome _fileSystemBlobStoreHome;

    /**
     * Applies the configured base path and depth.
     */
    @PostConstruct
    public void init( )
    {
        setBasePath( _strConfiguredBasePath );
        setDepth( _nConfiguredDepth );
    }

    /**
     * Gets the downloadService.
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
    public void setDownloadUrlService( final IBlobStoreDownloadUrlService downloadUrlService )
    {
        _downloadUrlService = downloadUrlService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#setName(java .lang.String)
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
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#delete(java .lang.String)
     */
    @Override
    public void delete( final String strKey )
    {
        try
        {
            _fileSystemBlobStoreHome.remove( strKey, getBasePath( ), getDepth( ) );
        }
        catch( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#getBlob(java .lang.String)
     */
    @Override
    public byte [ ] getBlob( final String strKey )
    {
        final BytesBlobStore blob;

        try
        {
            blob = _fileSystemBlobStoreHome.findByPrimaryKey( strKey, getBasePath( ), getDepth( ) );
        }
        catch( final IOException e )
        {
            AppLogService.error( e.getMessage( ), e );

            return null;
        }

        return ( blob == null ) ? null : blob.getValue( );
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#getBlobInputStream (java.lang.String)
     */
    @Override
    public InputStream getBlobInputStream( final String strKey )
    {
        try
        {
            return _fileSystemBlobStoreHome.findByPrimaryKeyInputStream( strKey, getBasePath( ), getDepth( ) );
        }
        catch( final IOException ioe )
        {
            AppLogService.error( ioe.getMessage( ), ioe );

            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#store(byte[])
     */
    @Override
    public String store( final byte [ ] blob )
    {
        final String strKey = BlobStoreUtils.generateNewIdBlob( );
        final BytesBlobStore blobStore = new BytesBlobStore( );
        blobStore.setId( strKey );
        blobStore.setValue( blob );

        try
        {
            _fileSystemBlobStoreHome.create( blobStore, getBasePath( ), getDepth( ) );
        }
        catch( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
        catch( final FileAlreadyExistsException fe )
        {
            throw new AppException( fe.getMessage( ), fe );
        }

        return strKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#storeInputStream (java.io.InputStream)
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
            _fileSystemBlobStoreHome.createInputStream( blob, getBasePath( ), getDepth( ) );
        }
        catch( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
        catch( final FileAlreadyExistsException fe )
        {
            throw new AppException( fe.getMessage( ), fe );
        }

        return strKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#storeFileItem (java.io.InputStream)
     */
    @Override
    public String storeFileItem( MultipartItem fileItem )
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

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#update(java .lang.String, byte[])
     */
    @Override
    public void update( final String strKey, final byte [ ] blob )
    {
        final BytesBlobStore blobStore = new BytesBlobStore( );
        blobStore.setId( strKey );
        blobStore.setValue( blob );

        try
        {
            _fileSystemBlobStoreHome.update( blobStore, getBasePath( ), getDepth( ) );
        }
        catch( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#updateInputStream (java.lang.String, java.io.InputStream)
     */
    @Override
    public void updateInputStream( final String strKey, final InputStream inputStream )
    {
        final InputStreamBlobStore blob = new InputStreamBlobStore( );
        blob.setId( strKey );
        blob.setInputStream( inputStream );

        try
        {
            _fileSystemBlobStoreHome.updateInputStream( blob, getBasePath( ), getDepth( ) );
        }
        catch( final IOException e )
        {
            throw new AppException( e.getMessage( ), e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#getBlobUrl( java.lang.String)
     */
    @Override
    public String getBlobUrl( final String strKey )
    {
        return _downloadUrlService.getDownloadUrl( getName( ), strKey );
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.paris.lutece.portal.service.blobstore.BlobStoreService#getFileUrl( java.lang.String)
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
