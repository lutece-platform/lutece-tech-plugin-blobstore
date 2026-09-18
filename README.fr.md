![](https://dev.lutece.paris.fr/jenkins/buildStatus/icon?job=tech-plugin-blobstore-deploy)
[![Alerte](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=alert_status)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)
[![Line of code](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=ncloc)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)
[![Coverage](https://dev.lutece.paris.fr/sonar/api/project_badges/measure?project=fr.paris.lutece.plugins%3Aplugin-blobstore&metric=coverage)](https://dev.lutece.paris.fr/sonar/dashboard?id=fr.paris.lutece.plugins%3Aplugin-blobstore)

# Descriptif du plugin

## Introduction

Ce plugin permet de stocker des données de taille importante, que ce soit en base de données ou en fichier système.

## Configuration

Configurer la clé privée utilisée pour signer les URL de téléchargement dans le fichier **webapp/WEB-INF/conf/plugins/blobstore.properties** :

```

blobstore.requestAuthenticator.name=signrequest.RequestHashAuthenticator
blobstore.requestAuthenticator.cfg.hashService=signrequest.Sha1HashService
blobstore.requestAuthenticator.cfg.signatureElements=blobstore,blob_key
blobstore.requestAuthenticator.cfg.privateKey= **change me** 

```

Le stockage fichier système lit son répertoire racine et sa profondeur dans le même fichier. Les deux passent par MicroProfile Config : une propriété système, une variable d'environnement **BLOBSTORE_FILE_SYSTEM_PATH** ou un fichier sous **override/** l'emportent sur la valeur livrée ici, ce qui permet à un conteneur d'obtenir un chemin accessible en écriture sans reconstruire :

```

blobstore.file.system.path=/var/blobs/
blobstore.file.system.depth=1

```

## Usage

Il existe plusieurs façons d'utiliser le plugin-blobstore.

- A: Utilisation via les FileService avec le blobStoreFileServiceProvider (recommandé)

- B: Utilisation en complément d'un plugin pour que ce dernier puisse stocker des données de taille importantes dans une baseàpart ou sous forme de fichier système.

A chaque données est associée un ID blob qui est généréaléatoirement. L'utilisation de la librairie **java.util.UUID** assure l'unicitédes identifiants.




 **A/ Utilisation via le blobStoreFileServiceProvider** 



Le fileStoreService peut être ajouté à la classe Home. Une Home est une façade statique : elle résout le FileService via CDI plutôt que par injection :
```

private static IFileStoreServiceProvider _fileStoreService = CDI.current( ).select( FileService.class ).get( )
        .getFileStoreServiceProvider( "blobStoreProvider" );

```
UTilisation ensuite du fileStoreServiceProvider :
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




 **B/ Etape 1 : Implémentation d'un service utilisant un service blobstore** 
Le plugin fournit les deux stockages sous forme de beans CDI. Choisir l'un par son nom et l'injecter dans son service :

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

 **blobstore.databaseBlobStoreService** stocke en base, **blobstore.fileSystemBlobStoreService** stocke en fichier système. Les deux sont configurés dans webapp/WEB-INF/conf/plugins/blobstore.properties : un plugin consommateur n'a rien à déclarer. Un consommateur qui a besoin de son propre stockage nommé, une autre base ou un autre répertoire racine, écrit son propre producteur CDI.

Lorsque le stockage n'est connu qu'à l'exécution (un nom lu dans une requête ou dans une propriété), le sélectionner par programmation. **NamedLiteral.of** refuse une valeur nulle : vérifier le nom avant :

```

if ( StringUtils.isNotBlank( strBlobStoreName ) )
{
    IBlobStoreService blobStoreService = CDI.current( )
            .select( IBlobStoreService.class, NamedLiteral.of( strBlobStoreName ) ).get( );
}

```




 **Etape 2 : Implémentation des méthodes CRUD sur blobstore** 
L'API **BlobStoreService** offre de nombreuses fonctionnalitées permettant de réaliser les opérations basiques de façon simple :

Pour une création d'un blob avec un tableau de bytes ou par InputStream :

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

Pour une modification d'un blob avec un tableau de bytes ou par InputStream :

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

Pour une suppression d'un blob :

```

public void deleteBlob( String strKey )
{
	// Plugin operation
	...
	_blobStoreService.delete( strKey );
	...
}

```

Il estégalement possible d'obtenir une URL permettant de télécharger le fichier stocké. Pour cela, il faut implémenter une méthode qui renvoit l'URL du blob ou du fichier :

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