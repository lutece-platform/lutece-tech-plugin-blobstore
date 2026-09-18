![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=tech-plugin-blobstore-deploy)
[![Alerte](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=alert_status)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)
[![Line of code](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=ncloc)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)
[![Coverage](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=coverage)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)

# Plugin's description

## Introduction

This plugin handles the big data storage, in database or file system.

## Configuration

Configure the private key used to sign the download URLs in the file **webapp/WEB-INF/conf/plugins/blobstore.properties** :

```

blobstore.requestAuthenticator.name=signrequest.RequestHashAuthenticator
blobstore.requestAuthenticator.cfg.hashService=signrequest.Sha1HashService
blobstore.requestAuthenticator.cfg.signatureElements=blobstore,blob_key
blobstore.requestAuthenticator.cfg.privateKey= **change me** 

```

The file system store reads its root directory and its depth from the same file. Both resolve through MicroProfile Config, so a system property, an environment variable **BLOBSTORE_FILE_SYSTEM_PATH** or a file under **override/** beats the value shipped here — which is how a container gets a writable path without rebuilding :

```

blobstore.file.system.path=/var/blobs/
blobstore.file.system.depth=1

```

## Usage

There are two ways of using the plugin-blobstore.

- A: Use as a FileService via the blobStoreFileServiceProvider (recommanded)

- B: Use in addition of a plugin in which the latter will be able to store big data in a distinct database or in file system.

Each data is linked to an ID blob which is generated randomly. The use of the library **java.util.UUID** ensures the unicity of the ids.




 **A/ Use the blobStoreFileServiceProvider** 



The fileStoreService can be added to the Home class. A Home is a static facade, so it resolves the FileService through CDI rather than by injection :
```

private static IFileStoreServiceProvider _fileStoreService = CDI.current( ).select( FileService.class ).get( )
        .getFileStoreServiceProvider( "blobStoreProvider" );

```
You can then use that fileStoreService :
```

...
	// get the file in multipart request and store it
        IFileStoreServiceProvider fileStoreService = MyHome.getFileStoreServiceProvider( );
        MultipartItem file = multipartRequest.getFile( "file" );
      
        if ( file != null  	&& file.getSize( ) > 0 )
        {
            try
            {
                String strFileStoreKey = fileStoreService.storeFileItem( file );
                ...
            }
            catch( FileServiceException e )
            {
                ...
            }
        }
...
	// get an URL for display
	String strFileUrl = fileStoreService.getFileDownloadUrlBO( strFileKey );
...

```




 **B/ Step 1 : Implentation of a service that will use a BlobStoreService"** 




The plugin ships both stores as CDI beans. Pick one by its name and inject it into your service :

```

@ApplicationScoped
public class MyPluginService
{
    @Inject
    @Named( "blobstore.databaseBlobStoreService" )
    private IBlobStoreService _blobStoreService;
    .
    .
    .
}

```

 **blobstore.databaseBlobStoreService** stores in database, **blobstore.fileSystemBlobStoreService** stores in file system. Both are configured in webapp/WEB-INF/conf/plugins/blobstore.properties, so a consumer plugin has nothing to declare. A consumer that needs its own named store, a separate database or another root directory, writes its own CDI producer for it.

When the store is only known at runtime (a name read from a request or from a property), select it programmatically. **NamedLiteral.of** rejects a null value, so check the name first :

```

if ( StringUtils.isNotBlank( strBlobStoreName ) )
{
    IBlobStoreService blobStoreService = CDI.current( )
            .select( IBlobStoreService.class, NamedLiteral.of( strBlobStoreName ) ).get( );
}

```




 **B/ Step 2 : Implementation of basic methods of the BlobStoreService** 




The API of **BlobStoreService** offers several functionnalities that allows to implements basic operation?

To store a blob with an array of bytes or with an InputStream :

```

public void storeBlob( byte[] blob )
{
	// Plugin operation
	...
	_blobStoreService.store( blob );
	...
}

public void storeBlob( InputStream blob )
{
	// Plugin operation
	...
	_blobStoreService.storeInputStream( blob );
	...
}

```

To modify a blob with an array of bytes or with an InputStream :

```

public void updateBlob( String strKey, byte[] blob )
{
	// Plugin operation
	...
	_blobStoreService.update( strKey, blob );
	...
}

public void updateBlob( String strKey, InputStream blob )
{
	// Plugin operation
	...
	_blobStoreService.updateInputStream( strKey, blob );
	...
}

```

To delete a blob :

```

public void deleteBlob( String strKey )
{
	// Plugin operation
	...
	_blobStoreService.delete( strKey );
	...
}

```

It is also possible to retrieve the URL to download the stored file :

```

public String getBlobUrl( String strKey )
{
	// Plugin operation
	...
	String strBlobUrl = _blobStoreService.getBlobUrl( strKey );
	...
}

public String getFileUrl( String strKey )
{
	// Plugin operation
	...
	String strFileUrl = _blobStoreService.getFileUrl( strKey );
	...
}

```


[Maven documentation and reports](https://dev.lutece.paris.fr/plugins/plugin-blobstore/)



 *generated by [xdoc2md](https://github.com/lutece-platform/tools-maven-xdoc2md-plugin) - do not edit directly.*