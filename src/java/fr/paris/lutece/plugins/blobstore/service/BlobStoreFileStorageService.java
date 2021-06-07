/*
 * Copyright (c) 2002-2021, Mairie de Paris
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
package fr.paris.lutece.plugins.blobstore.service;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminAuthenticationService;
import fr.paris.lutece.portal.service.file.ExpiredLinkException;
import fr.paris.lutece.portal.service.file.FileService;
import fr.paris.lutece.portal.service.file.IFileDownloadUrlService;
import fr.paris.lutece.portal.service.file.IFileRBACService;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;


import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;

/**
 * 
 * DatabaseBlobStoreService.
 * 
 */
public class BlobStoreFileStorageService implements IFileStoreServiceProvider
{
    private static final long serialVersionUID = 1L;

    /**
     * name defaulted to databaseBlobstore - only one can be supported by webapp
     */
    private static final String FILE_STORE_PROVIDER_NAME = "blobStoreProvider";

    private IFileDownloadUrlService _fileDownloadUrlService ;
    private IFileRBACService _fileRBACService ;
    private IBlobStoreService _blobStoreService ;
    private boolean _bDefault;
    
    /**
     * init
     * @param fileDownloadUrlService
     * @param fileRBACService
     * @param blobStoreService
     */
    public BlobStoreFileStorageService(IFileDownloadUrlService fileDownloadUrlService, 
            IFileRBACService fileRBACService, IBlobStoreService blobStoreService) 
    {
        this._fileDownloadUrlService = fileDownloadUrlService;
        this._fileRBACService = fileRBACService;        
        this._blobStoreService = blobStoreService;
    }

    
    
    /**
     * get the FileRBACService
     * 
     * @return the FileRBACService
     */
    public IFileRBACService getFileRBACService() {
        return _fileRBACService;
    }

    /**
     * set the FileRBACService
     * 
     * @param fileRBACService 
     */
    public void setFileRBACService(IFileRBACService fileRBACService) {
        this._fileRBACService = fileRBACService;
    }

    /**
     * Get the downloadService
     * 
     * @return the downloadService
     */
    public IFileDownloadUrlService getDownloadUrlService( )
    {
        return _fileDownloadUrlService;
    }

    /**
     * Sets the downloadService
     * 
     * @param downloadUrlService
     *            downloadService
     */
    public void setDownloadUrlService( IFileDownloadUrlService downloadUrlService )
    {
        _fileDownloadUrlService = downloadUrlService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName( )
    {
        return FILE_STORE_PROVIDER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( String strKey ) 
    {
        if ( StringUtils.isNotBlank( strKey ) )
        {
            BlobStoreFileItem fileData;
            
            try {
                fileData = new BlobStoreFileItem( strKey, _blobStoreService);
            } 
            catch (NoSuchBlobException ex) 
            {
                AppLogService.error( ex.getMessage( ), ex );
                return ;
            }
            
            // Delete file
            _blobStoreService.delete( fileData.getFileBlobId( ) );
            
            // Delete metadata
            _blobStoreService.delete( strKey );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile( String strKey )
    {
        if ( StringUtils.isNotBlank( strKey ) )
        {
            
            BlobStoreFileItem fileData;
            
            try {
                fileData = new BlobStoreFileItem( strKey, _blobStoreService);
            } 
            catch (NoSuchBlobException ex) 
            {
                AppLogService.error( ex.getMessage( ), ex );
                return null;
            }
            
            
            File file = new File( );
            file.setTitle(fileData.getName( ) );
            file.setSize( (int)fileData.getSize( ) );
            file.setMimeType( fileData.getContentType( ) );
            
            PhysicalFile physicalFile = new PhysicalFile( );
            physicalFile.setValue( _blobStoreService.getBlob( fileData.getFileBlobId( ) ) );
            
            file.setPhysicalFile( physicalFile );
            
            return file;
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String storeBytes( byte [ ] blob )
    {

        return _blobStoreService.store(blob);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String storeInputStream( InputStream inputStream )
    {

        return _blobStoreService.storeInputStream( inputStream );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String storeFileItem( FileItem fileItem )
    {
        
        return  _blobStoreService.storeFileItem( fileItem );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String storeFile( File file )
    {
        // store the content
        String blobId = _blobStoreService.store( file.getPhysicalFile( ).getValue( ) );
        // store metadata
        String metadata = BlobStoreFileItem.buildFileMetadata( file.getTitle( ), file.getSize( ), blobId, file.getMimeType());
        return _blobStoreService.store( metadata.getBytes( ) );
    }

    public void setDefault(boolean bDefault) 
    {
        this._bDefault = bDefault;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDefault( ) 
    {
        return _bDefault;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream(String strKey) 
    {
        return _blobStoreService.getBlobInputStream( strKey );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileDownloadUrlFO(String strKey) 
    {
        return _fileDownloadUrlService.getFileDownloadUrlFO( strKey, getName( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileDownloadUrlFO(String strKey, Map<String,String> additionnalData) 
    {
        return _fileDownloadUrlService.getFileDownloadUrlFO( strKey, additionnalData, getName( ) );
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileDownloadUrlBO(String strKey) 
    {
        return _fileDownloadUrlService.getFileDownloadUrlBO( strKey, getName( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileDownloadUrlBO(String strKey, Map<String,String> additionnalData)  
    {
        return _fileDownloadUrlService.getFileDownloadUrlBO( strKey, additionnalData, getName( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAccessRights(Map<String, String> fileData, User user) throws AccessDeniedException, UserNotSignedException
    {
        if (_fileRBACService != null )
        {
            _fileRBACService.checkAccessRights( fileData, user);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void checkLinkValidity( Map<String,String> fileData ) throws ExpiredLinkException
    {
        _fileDownloadUrlService.checkLinkValidity( fileData );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFileFromRequestBO( HttpServletRequest request ) throws AccessDeniedException, ExpiredLinkException, UserNotSignedException
    {
        Map<String, String> fileData = _fileDownloadUrlService.getRequestDataBO( request );
        
        // check access rights
        checkAccessRights( fileData, AdminAuthenticationService.getInstance( ).getRegisteredUser( request ) );

        // check validity
        checkLinkValidity( fileData );

        String strFileId = fileData.get( FileService.PARAMETER_FILE_ID );

        return getFile( strFileId );            
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFileFromRequestFO(HttpServletRequest request) throws AccessDeniedException, ExpiredLinkException, UserNotSignedException
    {
        
        Map<String, String> fileData = _fileDownloadUrlService.getRequestDataFO( request );

        // check access rights
        checkAccessRights( fileData, SecurityService.getInstance().getRegisteredUser(request) );

        // check validity
        checkLinkValidity( fileData );

        String strFileId = fileData.get( FileService.PARAMETER_FILE_ID );

        return getFile( strFileId );            
    }
}
