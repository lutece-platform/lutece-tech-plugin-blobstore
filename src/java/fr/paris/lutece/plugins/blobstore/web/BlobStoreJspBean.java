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
package fr.paris.lutece.plugins.blobstore.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import fr.paris.lutece.plugins.blobstore.util.BlobStoreConstants;
import fr.paris.lutece.plugins.blobstore.util.BlobStoreUtils;
import fr.paris.lutece.portal.service.blobstore.BlobStoreFileItem;
import fr.paris.lutece.portal.service.blobstore.BlobStoreService;
import fr.paris.lutece.portal.service.blobstore.NoSuchBlobException;
import fr.paris.lutece.portal.service.fileupload.FileUploadService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.filesystem.UploadUtil;

/**
 * Provides blob download.
 *
 */
public class BlobStoreJspBean
{	
	private static final String PROPERTY_MESSAGE_NO_SUCH_BLOB = "";
	private static final String PROPERTY_MESSAGE_ERROR_RETRIEVING_BLOB = "";
	private static final String PROPERTY_MESSAGE_NO_SUCH_BLOBSTORE = "";
	private static final String PROPERTY_MESSAGE_ACCESS_DENIED = "";
	
	/**
	 * Download a file : blob key is the metadata key
	 * @param request the request
	 * @param response the response
	 * @return the error message if any, <code>null</code> otherwise.
	 */
	public String doDownloadFile( HttpServletRequest request, HttpServletResponse response )
	{
		String strErrorMessage = null;
		if ( BlobStoreUtils.getRequestAuthenticator().isRequestAuthenticated( request ) )
		{
			String strBlobKey = request.getParameter( BlobStoreConstants.PARAMETER_BLOB_KEY );
			String strBlobstore = request.getParameter( BlobStoreConstants.PARAMETER_BLOB_STORE );
	
			BlobStoreService blobstoreService = (BlobStoreService) SpringContextService.getBean( strBlobstore );
			
			if ( blobstoreService != null )
			{
				if ( StringUtils.isNotBlank( strBlobKey ) )
				{
					try
					{
						
						BlobStoreFileItem fileItem = new BlobStoreFileItem( strBlobKey, blobstoreService );
						DownloadableFile file = new DownloadableFile();
						file.setContentType( fileItem.getContentType(  ) );
						file.setFileName( UploadUtil.cleanFileName( FileUploadService.getFileNameOnly( fileItem ) ) );
						file.setInputStream( fileItem.getInputStream(  ) );
						file.setSize( (int) fileItem.getSize(  ) );
	
						strErrorMessage = writeFile( request, response, file );
					} 
					catch ( IOException ioe )
					{
						AppLogService.error( ioe.getMessage(  ), ioe );
						// error message
						strErrorMessage = I18nService.getLocalizedString( PROPERTY_MESSAGE_ERROR_RETRIEVING_BLOB, request.getLocale(  ) );
					}
					catch ( NoSuchBlobException nsbe )
					{
						// error message not found
						strErrorMessage = I18nService.getLocalizedString( PROPERTY_MESSAGE_NO_SUCH_BLOB, request.getLocale(  ) );
					}
				}
				else
				{
					// error message key
					strErrorMessage = I18nService.getLocalizedString( PROPERTY_MESSAGE_NO_SUCH_BLOB, request.getLocale(  ) );
				}
			}
			else
			{
				// error message blob store
				strErrorMessage = I18nService.getLocalizedString( PROPERTY_MESSAGE_NO_SUCH_BLOBSTORE, request.getLocale(  ) );
			}
		}
		else
		{
			strErrorMessage = I18nService.getLocalizedString( PROPERTY_MESSAGE_ACCESS_DENIED, request.getLocale(  ) );
		}
		
		// nothing to return
		return strErrorMessage;
	}
	
	/**
	 * Downloads a blob.
	 * @param request the requrest
	 * @param response the response
	 * @return the error message if any, <code>null</code> otherwise.
	 */
	public String doDownloadBlob( HttpServletRequest request, HttpServletResponse response )
	{
		String strBlobKey = request.getParameter( BlobStoreConstants.PARAMETER_BLOB_KEY );
		String strBlobstore = request.getParameter( BlobStoreConstants.PARAMETER_BLOB_STORE );

		BlobStoreService blobstoreService;
		try
		{
			blobstoreService = (BlobStoreService) SpringContextService.getBean( strBlobstore );
		}
		catch ( NoSuchBeanDefinitionException ex )
		{
			blobstoreService = null;
		}
		
		String strErrorMessage = null;
		
		if ( blobstoreService != null )
		{
			if ( StringUtils.isNotBlank( strBlobKey ) )
			{
				// content type is unknown
				// file name is blob key
				// size is unknown
				InputStream is = blobstoreService.getBlobInputStream( strBlobKey );
				if ( is != null )
				{
					DownloadableFile file = new DownloadableFile();
					file.setFileName( strBlobKey );
					file.setInputStream( is );

					strErrorMessage = writeFile( request, response, file );
				}
				else
				{
					// error message
					strErrorMessage = I18nService.getLocalizedString( PROPERTY_MESSAGE_ERROR_RETRIEVING_BLOB, request.getLocale(  ) );
				}
			}
			else
			{
				// error message key
				strErrorMessage = I18nService.getLocalizedString( PROPERTY_MESSAGE_NO_SUCH_BLOB, request.getLocale(  ) );
			}
		}
		else
		{
			// error message blob store
			strErrorMessage = I18nService.getLocalizedString( PROPERTY_MESSAGE_NO_SUCH_BLOBSTORE, request.getLocale(  ) );
		}
		
		// nothing to return
		return strErrorMessage;
	}
	
	/**
	 * Writes the file to the response
	 * @param request the request
	 * @param response the response
	 * @param file the file
	 * @return the error message if any, <code>null</code> othrewise.
	 */
	private String writeFile( HttpServletRequest request, HttpServletResponse response, DownloadableFile file )
	{
		String strErrorMessage = null;
		OutputStream os = null;
		InputStream is = file.getInputStream();
		try
		{
            response.setHeader( "Content-Disposition", "attachment ;filename=\"" + file.getFileName(  ) );
            response.setHeader( "Pragma", "public" );
            response.setHeader( "Expires", "0" );
            response.setHeader( "Cache-Control", "must-revalidate,post-check=0,pre-check=0" );
            
            if ( file.getContentType() != null )
            {
            	response.setContentType( file.getContentType(  ) );
            }
            else
            {
            	response.setContentType( "application/force-download" );
            }
            
            if ( file.getSize(  ) != 0 )
            {
            	response.setContentLength( file.getSize(  ) );
            }

			os = response.getOutputStream(  );
			IOUtils.copy( is, os );
		}
		catch ( IOException ioe )
		{
			AppLogService.error( ioe.getMessage(), ioe );
			// error message
			strErrorMessage = I18nService.getLocalizedString( PROPERTY_MESSAGE_ERROR_RETRIEVING_BLOB, request.getLocale(  ) );
		}
		finally
		{
			IOUtils.closeQuietly( is );
			IOUtils.closeQuietly( os );
		}
		
		return strErrorMessage;
	}
	
	/**
	 * 
	 * DownloadableFile
	 *
	 */
	private static class DownloadableFile
	{
		private String _strFileName;
		private String _strContentType;
		private int _nSize;
		private InputStream _inputStream;
		
		/**
		 * Gets the input stream
		 * @return the input stream
		 */
		public InputStream getInputStream(  )
		{
			return _inputStream;
		}
		
		/**
		 * Sets the input stream
		 * @param inputStream the input stream
		 */
		public void setInputStream( InputStream inputStream )
		{
			_inputStream = inputStream;
		}
		
		/**
		 * Gets the file name
		 * @return the file name
		 */
		public String getFileName(  )
		{
			return _strFileName;
		}
		
		/**
		 * Sets the file name
		 * @param strFileName the file name
		 */
		public void setFileName( String strFileName )
		{
			_strFileName = strFileName;
		}

		/**
		 * Gets the file size
		 * @return the file size
		 */
		public int getSize(  )
		{
			return _nSize;
		}
		
		/**
		 * Sets the file size
		 * @param nSize the file size
		 */
		public void setSize( int nSize )
		{
			_nSize = nSize;
		}
		
		/**
		 * Gets the content type
		 * @return the content type
		 */
		public String getContentType(  )
		{
			return _strContentType;
		}
		
		/**
		 * Sets the content type
		 * @param strContentType the content type
		 */
		public void setContentType( String strContentType )
		{
			_strContentType = strContentType;
		}
	}
}
