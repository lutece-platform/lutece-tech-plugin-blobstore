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
package fr.paris.lutece.plugins.blobstore.service;

import fr.paris.lutece.plugins.blobstore.service.filesystem.FileSystemBlobStoreService;
import fr.paris.lutece.portal.service.file.IFileDownloadUrlService;
import fr.paris.lutece.portal.service.file.IFileRBACService;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.CdiHelper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Produces the file store service provider backed by a blob store.
 */
@ApplicationScoped
public class BlobStoreFileStorageServiceProducer
{
    /** The CDI bean name of the produced file store service provider. */
    public static final String BEAN_SERVICE = "blobstore.blobStoreFileService";

    /** The configuration key naming the download URL service. */
    private static final String PROPERTY_DOWNLOAD_SERVICE = BEAN_SERVICE + ".downloadService";

    /** The configuration key naming the RBAC service. */
    private static final String PROPERTY_RBAC_SERVICE = BEAN_SERVICE + ".rbacService";

    /** The configuration key naming the blob store holding the files. */
    private static final String PROPERTY_BLOB_STORE_SERVICE = BEAN_SERVICE + ".blobStoreService";

    /** The download URL services to choose from. */
    @Inject
    private Instance<IFileDownloadUrlService> _downloadServices;

    /** The RBAC services to choose from. */
    @Inject
    private Instance<IFileRBACService> _rbacServices;

    /** The blob stores to choose from. */
    @Inject
    private Instance<IBlobStoreService> _blobStoreServices;

    /**
     * Builds the file store service provider from the configured bean names.
     * 
     * @param strDownloadServiceName
     *            the bean name of the download URL service
     * @param strRbacServiceName
     *            the bean name of the RBAC service
     * @param strBlobStoreServiceName
     *            the bean name of the blob store holding the files
     * @return the file store service provider
     * @throws AppException
     *             when a configured name resolves to no bean, or to several
     */
    @Produces
    @ApplicationScoped
    @Named( BEAN_SERVICE )
    public IFileStoreServiceProvider produceBlobStoreFileStorageService(
            @ConfigProperty( name = PROPERTY_DOWNLOAD_SERVICE, defaultValue = "defaultFileDownloadService" ) String strDownloadServiceName,
            @ConfigProperty( name = PROPERTY_RBAC_SERVICE, defaultValue = "defaultFileNoRBACService" ) String strRbacServiceName,
            @ConfigProperty( name = PROPERTY_BLOB_STORE_SERVICE, defaultValue = FileSystemBlobStoreService.BEAN_SERVICE ) String strBlobStoreServiceName )
    {
        return new BlobStoreFileStorageService( resolveConfigured( _downloadServices, strDownloadServiceName, PROPERTY_DOWNLOAD_SERVICE ),
                resolveConfigured( _rbacServices, strRbacServiceName, PROPERTY_RBAC_SERVICE ),
                resolveConfigured( _blobStoreServices, strBlobStoreServiceName, PROPERTY_BLOB_STORE_SERVICE ) );
    }

    /**
     * Resolves the bean a configuration key names.
     * 
     * @param <T>
     *            the bean type
     * @param instances
     *            the beans to choose from
     * @param strName
     *            the bean name read from the configuration
     * @param strKey
     *            the configuration key holding that name
     * @return the bean carrying that name
     * @throws AppException
     *             when the name resolves to no bean, or to several
     */
    private <T> T resolveConfigured( Instance<T> instances, String strName, String strKey )
    {
        try
        {
            return CdiHelper.resolve( instances, strName );
        }
        catch( IllegalStateException e )
        {
            throw new AppException( strKey + " : " + e.getMessage( ), e );
        }
    }
}
