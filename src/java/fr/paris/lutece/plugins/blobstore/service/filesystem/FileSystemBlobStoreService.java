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
package fr.paris.lutece.plugins.blobstore.service.filesystem;

import fr.paris.lutece.plugins.blobstore.business.BytesBlobStore;
import fr.paris.lutece.plugins.blobstore.business.InputStreamBlobStore;
import fr.paris.lutece.plugins.blobstore.business.filesystem.FileAlreadyExistsException;
import fr.paris.lutece.plugins.blobstore.business.filesystem.FileSystemBlobStoreHome;
import fr.paris.lutece.plugins.blobstore.service.download.IBlobStoreDownloadUrlService;
import fr.paris.lutece.plugins.blobstore.service.download.JSPBlobStoreDownloadUrlService;
import fr.paris.lutece.plugins.blobstore.util.BlobStoreUtils;
import fr.paris.lutece.portal.service.blobstore.BlobStoreService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * FileSystemBlobStoreService
 *
 */
public class FileSystemBlobStoreService implements BlobStoreService
{
    private static final long serialVersionUID = 1L;
    private String _strBasePath;
    private String _strName;
    /** Uses {@link JSPBlobStoreDownloadUrlService} as default one */
    private IBlobStoreDownloadUrlService _downloadUrlService = new JSPBlobStoreDownloadUrlService(  );
    
    /**
     * Gets the downloadService.
     * @return the downloadService
     */
    public IBlobStoreDownloadUrlService getDownloadUrlService(  )
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
    public void setName( String strName ) 
    {
    	_strName = strName;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName(  )
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
    public String getBasePath(  )
    {
        return _strBasePath;
    }

    /**
     * {@inheritDoc}
     */
    public void delete( String strKey )
    {
        try
        {
            FileSystemBlobStoreHome.remove( strKey, getBasePath(  ) );
        }
        catch ( IOException e )
        {
            throw new AppException( e.getMessage(  ), e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getBlob( String strKey )
    {
        BytesBlobStore blob;

        try
        {
            blob = FileSystemBlobStoreHome.findByPrimaryKey( strKey, getBasePath(  ) );
        }
        catch ( IOException e )
        {
            AppLogService.error( e.getMessage(  ), e );

            return null;
        }

        return ( blob == null ) ? null : blob.getValue(  );
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getBlobInputStream( String strKey )
    {
        try
        {
            return FileSystemBlobStoreHome.findByPrimaryKeyInputStream( strKey, getBasePath(  ) );
        }
        catch ( IOException ioe )
        {
            AppLogService.error( ioe.getMessage(  ), ioe );

            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String store( byte[] blob )
    {
        String strKey = BlobStoreUtils.generateNewIdBlob(  );
        BytesBlobStore blobStore = new BytesBlobStore(  );
        blobStore.setId( strKey );
        blobStore.setValue( blob );

        try
        {
            FileSystemBlobStoreHome.create( blobStore, getBasePath(  ) );
        }
        catch ( IOException e )
        {
            throw new AppException( e.getMessage(  ), e );
        }
        catch ( FileAlreadyExistsException fe )
        {
            throw new AppException( fe.getMessage(  ), fe );
        }

        return strKey;
    }

    /**
     * {@inheritDoc}
     */
    public String storeInputStream( InputStream inputStream )
    {
        String strKey = BlobStoreUtils.generateNewIdBlob(  );
        InputStreamBlobStore blob = new InputStreamBlobStore(  );
        blob.setId( strKey );
        blob.setInputStream( inputStream );

        try
        {
            FileSystemBlobStoreHome.createInputStream( blob, getBasePath(  ) );
        }
        catch ( IOException e )
        {
            throw new AppException( e.getMessage(  ), e );
        }
        catch ( FileAlreadyExistsException fe )
        {
            throw new AppException( fe.getMessage(  ), fe );
        }

        return strKey;
    }

    /**
     * {@inheritDoc}
     */
    public void update( String strKey, byte[] blob )
    {
        BytesBlobStore blobStore = new BytesBlobStore(  );
        blobStore.setId( strKey );
        blobStore.setValue( blob );

        try
        {
            FileSystemBlobStoreHome.update( blobStore, getBasePath(  ) );
        }
        catch ( IOException e )
        {
            throw new AppException( e.getMessage(  ), e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateInputStream( String strKey, InputStream inputStream )
    {
        InputStreamBlobStore blob = new InputStreamBlobStore(  );
        blob.setId( strKey );
        blob.setInputStream( inputStream );

        try
        {
            FileSystemBlobStoreHome.updateInputStream( blob, getBasePath(  ) );
        }
        catch ( IOException e )
        {
            throw new AppException( e.getMessage(  ), e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getBlobUrl( String strKey )
    {
    	return _downloadUrlService.getDownloadUrl( getName(  ), strKey );
    }

    /**
     * {@inheritDoc}
     */
	public String getFileUrl(String strKey) 
	{
		return _downloadUrlService.getFileUrl( getName(  ), strKey );
	}
}
