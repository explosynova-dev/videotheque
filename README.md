# Vidéothèque

Application Android personnelle pour enregistrer, classer (durée + tags) et retrouver des liens vidéo YouTube.

## Installer sur le téléphone

Le fichier prêt à installer est `Videotheque.apk` (à la racine du projet).

1. Copier `Videotheque.apk` sur le téléphone (câble USB, Google Drive, e-mail…).
2. L'ouvrir depuis le téléphone ; Android demande d'autoriser l'installation depuis cette source : accepter.
3. L'icône « Vidéothèque » apparaît avec les autres applications.

Usage le plus rapide : dans YouTube → **Partager** → **Vidéothèque** → choisir les tags → **Enregistrer** (retour automatique dans YouTube).

## Changer le nom de l'application

Dans `app/src/main/res/values/strings.xml`, modifier `app_name` (et `share_label`, le nom affiché dans le menu Partager).

## Recompiler

Les outils (JDK 17, SDK Android) sont installés localement dans `.toolchain/` (non versionné).

```bash
JAVA_HOME="$PWD/.toolchain/jdk-17.0.20.1+1" ./gradlew testDebugUnitTest assembleDebug
```

L'APK produit : `app/build/outputs/apk/debug/app-debug.apk`. Le projet s'ouvre aussi tel quel dans Android Studio.

## Organisation du code (`app/src/main/java/com/perso/videotheque/`)

| Dossier | Rôle |
|---|---|
| `domain/` | Modèles, format des durées, analyse des liens, filtres et tris (logique pure, testée) |
| `data/local/` | Base SQLite locale (Room) : vidéos, tags, liaison vidéo↔tag |
| `data/repository/` | Point d'accès unique aux données |
| `data/metadata/` | Récupération titre / chaîne / miniature / durée depuis YouTube, sans clé API |
| `ui/home/` | Écran principal : recherche, filtres durée + tags, tri, liste |
| `ui/edit/` | Ajout / modification / suppression d'une vidéo |
| `ui/navigation/` | Les 2 écrans et le parcours « Partager » |
| `util/` | Ouverture dans l'app YouTube (ou le navigateur) |

Notes :
- La durée est stockée en secondes (`durationSeconds`), indépendamment des tags.
- Filtre durée : les bornes 5 min et 1 h 30 sont ouvertes (« 5 min ou moins », « 1 h 30 et plus ») pour ne jamais masquer une vidéo plus courte ou plus longue.
- Mots-clés zones du corps (Hanche, Épaule, Dos, Haut du corps, Bas du corps) : reconnus en français et en anglais dans le titre de la vidéo et présélectionnés à l'ajout (`domain/BodyKeywords.kt` pour ajouter des mots). Hanche implique Bas du corps, Épaule implique Haut du corps.
- Tags : bouton **Gérer** à côté de « Tags » (accueil et écran d'ajout) pour créer ou supprimer un tag. Supprimer un tag le retire des vidéos sans les supprimer. Les tags par défaut (`AppDatabase.DEFAULT_TAGS`) ne sont insérés qu'à la création de la base : un tag supprimé ne revient pas, et un tag de zone du corps supprimé n'est plus présélectionné.
- Plusieurs tags sélectionnés = « au moins un des tags ». Le mode « tous les tags » existe déjà dans la logique (`TagMatchMode.ALL`) si besoin plus tard.
- Si YouTube change sa page, seule la durée automatique peut cesser de fonctionner : elle reste saisissable à la main, et `YouTubePageParser.kt` est le seul fichier à adapter.
