#/bin/bash

path=$1
depth=$2

function help_cmd {
        echo "Migration depuis un blobstore de profondeur 0 vers une profondeur 1 ou 2."
        echo "$0 <path> <prof>"
        echo "    - path : chemin vers le dossier blobstore a migrer."
        echo "    - prof : profondeur cible (1 ou 2)."
}

if [ -z "$path" ] || [ -z "$depth" ]; then
        echo "<path> ou <prof> manquant"
        help_cmd
        exit 1
fi

if [ ! -d "$path" ]; then
        echo "<path> n'est pas un dossier"
        help_cmd
        exit 2
fi

if [ ! "$depth" -eq "1" ] && [ ! "$depth" -eq "2"  ]; then
        echo "<prof> doit etre defini aux valeurs 1 ou 2."
        help_cmd
        exit 3
fi

cd $path
echo "Migration lancee"
for file in $(ls .)
do
        if [ -f "$file" ]; then
                echo -n "."
                if [ "$depth" -eq 1 ]; then
                        new_folder=${file:0:3}
                else
                        new_folder="${file:0:3}/${file:3:3}"
                fi
                mkdir -p $new_folder
                mv $file $new_folder
        fi
done

echo
echo "Migation terminee"