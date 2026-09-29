# AYNEHA Keyboard SDK

Bibliothèque Android (Kotlin) permettant à n'importe quelle application
tierce d'intégrer la saisie AYNEHA — de trois façons indépendantes, à
combiner librement.

## Les 3 modes d'intégration

| Mode | Composant | Cas d'usage |
|---|---|---|
| **1. Widget in-app** | `AynehaKeyboardView` | Clavier AYNEHA posé directement dans l'écran de l'app hôte (pas besoin que l'utilisateur change de clavier système) |
| **2. Translittération automatique** | `AynehaTextWatcher` / `AynehaKeyboard.transliterate()` | L'utilisateur tape en latin sur son clavier habituel, le texte est converti à la volée en glyphes AYNEHA |
| **3. Clavier système (IME)** | `AynehaInputMethodService` | Clavier installable au niveau système, utilisable dans *toutes* les apps de l'utilisateur (comme Gboard), pas seulement l'app hôte |

Les modes 1 et 3 réutilisent exactement le même code de disposition de
touches (`KeyboardLayouts`) — comportement garanti identique.

## Installation

Le SDK est fourni en code source, sous forme de module Gradle
(`:ayneha-keyboard`) à inclure dans le projet hôte :

```kotlin
// settings.gradle.kts de l'application hôte
include(":ayneha-keyboard")
project(":ayneha-keyboard").projectDir = File("chemin/vers/AynehaKeyboardSDK/ayneha-keyboard")
```

```kotlin
// build.gradle.kts du module app de l'hôte
dependencies {
    implementation(project(":ayneha-keyboard"))
}
```

(Pour une distribution binaire classique, ce module se compile aussi en
`.aar` via `./gradlew :ayneha-keyboard:assembleRelease`, publiable ensuite
sur un dépôt Maven privé ou JitPack.)

## Utilisation

### Mode 1 — Widget in-app

```xml
<com.maigus.ayneha.sdk.AynehaKeyboardView
    android:id="@+id/aynehaKeyboard"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:ak_showDigitRow="true"
    app:ak_startInTransliterationMode="false" />
```

```kotlin
findViewById<AynehaKeyboardView>(R.id.aynehaKeyboard).attachTo(monEditText)
```

Chaque touche insère directement le glyphe AYNEHA correspondant. L'attribut
`ak_startInTransliterationMode` (ou `setShowLatinLabels(true)`) affiche des
étiquettes latines d'aide sur les touches pour les utilisateurs pas encore
familiers du script — **le caractère inséré reste toujours l'AYNEHA natif**,
seule l'étiquette change.

### Mode 2 — Translittération automatique

```kotlin
AynehaKeyboard.enableTransliteration(monEditText)
// ou, sans toucher à l'UI :
val texteAyneha = AynehaKeyboard.transliterate("bismala")
```

⚠️ Translittération **phonétique de base** (digrammes `sh`, `ts`, `dj`, `ny`,
`ng` + lettres simples), sans inférence automatique des accents
(nasalisation, doublement, mutité, ouverture) qui dépendent du contexte
linguistique réel du mot. Extensible via `AynehaTransliterator.customDigraphs`
et `.customRules`. Pour une saisie exacte avec accents, préférer le mode 1
ou 3.

### Mode 3 — Clavier système

```kotlin
AynehaKeyboard.openSystemKeyboardSettings(this)  // ouvre Paramètres > Langues et saisie
AynehaKeyboard.isSystemKeyboardEnabled(this)     // vérifie s'il est activé
AynehaKeyboard.showInputMethodPicker(this)        // ouvre le sélecteur de clavier
```

Android n'autorise aucune app à s'activer elle-même comme clavier système
par défaut (c'est le flux standard de tous les claviers tiers) : l'utilisateur
doit valider manuellement l'activation dans les paramètres.

## Mapping officiel des glyphes

`AynehaCodepoints` expose l'intégralité des 98 glyphes de la Matrice
Officielle de l'Alphabet AYNEHA (zone privée Unicode U+E000–U+E061) :

- `DIGITS` / `digit(0..9)` — chiffres, U+E000–U+E009
- `CONSONANTS` — 22 consonnes de base, U+E00A–U+E01F
- `VOWELS` — 5 voyelles de base, U+E020–U+E024
- `COMBINATIONS` — 50 combinaisons lettre + accent (Double, Nasalisé, Muet,
  Ouvert), U+E025–U+E056
- `ACCENT_*`, `VIRGULE`, `POINT_VIRGULE` — accents isolés et ponctuation,
  U+E057–U+E061

## Personnalisation visuelle

Le SDK reprend par défaut la charte graphique AYNEHA (nuit / dune / sable /
ochre / rivière / argile, `res/values/colors.xml`). Une application hôte peut
la redéfinir en surchargeant les mêmes noms de couleur dans son propre
`colors.xml`, ou via les attributs XML du widget :
`ak_backgroundColor`, `ak_keyBackgroundColor`, `ak_keyTextColor`,
`ak_accentColor`.

## Application de démonstration

Le module `:sample` illustre les trois modes dans une seule activité — à
lancer directement pour voir le SDK en action.

## Structure du dépôt

```
AynehaKeyboardSDK/
├── ayneha-keyboard/                  ← le SDK (module bibliothèque, .aar)
│   └── src/main/
│       ├── AndroidManifest.xml       ← déclare le service IME (optionnel à l'usage)
│       ├── java/.../sdk/
│       │   ├── AynehaCodepoints.kt   ← mapping officiel des 98 glyphes
│       │   ├── KeyboardLayouts.kt    ← disposition des touches (partagée widget/IME)
│       │   ├── AynehaKeyboardView.kt ← widget in-app
│       │   ├── AynehaTextTarget.kt   ← abstraction EditText / InputConnection
│       │   ├── AynehaTransliterator.kt ← conversion latin → AYNEHA
│       │   ├── AynehaTextWatcher.kt  ← translittération en temps réel
│       │   ├── AynehaKeyboard.kt     ← façade publique du SDK
│       │   └── ime/AynehaInputMethodService.kt ← clavier système
│       └── res/
│           ├── font/ayneha_regular.ttf
│           ├── values/{colors,attrs,strings}.xml
│           ├── drawable/ak_key_*.xml
│           └── xml/method.xml        ← descripteur requis par le framework IME
├── sample/                           ← app de démo, 3 modes
└── README.md
```

## Ouvrir le projet

1. Ouvrir le dossier `AynehaKeyboardSDK` dans Android Studio (Hedgehog ou
   plus récent) — les deux modules (`:ayneha-keyboard` et `:sample`) sont
   déjà inclus dans `settings.gradle.kts`.
2. Laisser Android Studio synchroniser Gradle et régénérer le wrapper
   (non fourni en binaire dans cette archive, faute d'accès aux dépôts
   Google depuis l'environnement où elle a été générée). Sinon :
   ```
   gradle wrapper --gradle-version 8.7
   ```
3. Lancer le module `:sample` (Run ▶) pour voir les 3 modes en action.

`minSdk` = 24, `compileSdk`/`targetSdk` = 34.

## Translittération phonologique latin → AYNEHA

`AynehaTransliterator` applique désormais plusieurs règles phonologiques et
morphophonologiques des langues songhay, chacune activable/désactivable via
`AynehaTransliterationOptions` :

| Règle | Option | Défaut | Exemple vérifié |
|---|---|---|---|
| Longueur vocalique (voyelle doublée → Voyelle+Double) | `vowelLength` | `true` | `ka` → K+A · `kaa` → K+(A+Double) |
| Gémination consonantique (consonne doublée → Consonne+Double) | `consonantGemination` | `true` | `furo` → F·U·R·O · `furro` → F·U·(R+Double)·O |
| Palatalisation K/G devant I/E → TΣ/DJ | `palatalizeVelars` | `true` | `ki` → TΣ·I · `gi` → DJ·I · `ka` → K·A (inchangé) |
| Assimilation nasale N+labiale→M, N+vélaire→Ŋ | `assimilateNasals` | `true` | `anba` → A·M·B·A · `anda` → A·N·D·A (inchangé, D non labial/vélaire) |
| /v/ → W (aucune lettre dédiée) | *(toujours actif)* | — | `vent` → W·E·N·T |
| /p/ conservé (lettre dédiée aux emprunts) | `nativizeP` (→F si activé) | `false` | `pere` → P·E·R·E (par défaut) · F·E·R·E (si `nativizeP=true`) |
| Épenthèse vocalique entre consonnes non géminées/digramme | `epenthesis` | `false` (heuristique) | `ndra` → N·I·D·I·R·A (si activé) |

**Note sur `ng`/`ny`** : ces deux séquences latines sont toujours résolues
comme les lettres dédiées NG/NY (un seul phonème), *avant* que la règle
d'assimilation nasale n'ait l'occasion de s'appliquer — cohérent avec le fait
que l'alphabet possède déjà une lettre propre pour ces phonèmes. La règle
d'assimilation nasale ne s'applique donc, en pratique, qu'aux séquences sans
digramme dédié (ex. `n`+labiale).

Ces 6 tests ont été vérifiés par compilation et exécution réelle (JVM,
Kotlin 1.9.24) avant livraison.

## Limites connues / pistes d'évolution

- La tonologie (Ton Haut / Ton Bas / tons flottants) n'est pas représentée :
  la matrice actuelle des 98 glyphes ne comporte pas de diacritique tonal
  dédié.
- L'élision vocalique aux frontières morphologiques (ex. racine + `-o`/`-oo`
  défini) n'est pas traitée : elle nécessiterait une segmentation
  morphologique que ce moteur lettre-à-lettre n'effectue pas.
- L'épenthèse est une heuristique simplifiée (insertion systématique entre
  deux consonnes consécutives non géminées/digramme) : la vraie épenthèse
  dépend de la structure syllabique précise du mot. Désactivée par défaut.
- La signification phonologique exacte de l'accent **Muet** (disponible dans
  `AynehaCodepoints.COMBINATIONS` pour certaines consonnes) n'a pas été
  précisée : aucune règle automatique ne le produit pour l'instant — à
  ajouter si son usage est clarifié.
- `AynehaKeyboardView` ne propose pas encore de bulle de variantes au
  long-press (Double/Muet/Nasalisé/Ouvert) — la donnée existe
  (`KeyboardLayouts.accentRow`, `AynehaCodepoints.COMBINATIONS`) mais
  l'interaction n'est pas encore câblée.
- Pas encore de publication Maven/JitPack automatisée — le SDK se consomme
  aujourd'hui en module source.

## Licence / License

- **Code du SDK** (`ayneha-keyboard`, `sample`) : **GNU LGPL v3.0** — voir [`LICENSE`](LICENSE) (texte complet de la GPLv3) et [`COPYING.LESSER`](COPYING.LESSER) (permissions additionnelles de la LGPL, comme recommandé par la Free Software Foundation).
  - Toute modification du SDK lui-même doit rester publiée sous la même licence.
  - Une application tierce qui **intègre** ce SDK (même fermée ou commerciale) n'est **pas obligée** d'être open source, contrairement à la GPL stricte — c'est le but de la LGPL pour une bibliothèque destinée à être réutilisée.
- **Police AYNEHA (Ayneha Type)** : SIL Open Font License 1.1 — voir [`OFL.txt`](OFL.txt). Le fichier de police doit toujours être accompagné de cette licence lors de toute redistribution.
- **Nom « AYNEHA », logos et identité visuelle** : non couverts par la LGPL ; contacter l'auteur avant tout usage commercial ou en tant que marque.

*Code: GNU LGPL v3.0 (see LICENSE + COPYING.LESSER). Font: SIL OFL 1.1 (see OFL.txt). The AYNEHA name, logos and visual identity are not covered by the LGPL.*

**Concepteur et créateur : Mahamadou Issiaka MAIGA (MAIGUS)**
Site officiel : https://ayneha-songhay.github.io/
