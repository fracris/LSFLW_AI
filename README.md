# 🌟 LittleStars-AI

Un progetto Java che integra [EmbASP](https://github.com/DeMaCS-UNICAL/EmbASP-Java) per la programmazione dichiarativa (Answer Set Programming) tramite interfacce grafiche e engine logici.

## 📦 Dipendenze

Il progetto utilizza **Maven** per la gestione delle dipendenze. Una delle librerie principali è **EmbASP**, che non è disponibile su Maven Central ma può essere importata tramite [JitPack](https://jitpack.io).

---

## ✅ Metodo consigliato: Dipendenza tramite JitPack

Assicurati che nel tuo `pom.xml` sia presente:

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
      <groupId>com.github.DeMaCS-UNICAL</groupId>
      <artifactId>EmbASP-Java</artifactId>
      <version>v7.1.0</version>
</dependency>
```

Poi esegui:

```bash
mvn clean install -U
```

> ℹ️ Nota: la versione **deve essere `v7.1.0`** (con la `v`) perché è il nome esatto del tag rilasciato nel repository GitHub.

---

## ❌ Se JitPack non funziona (metodo alternativo manuale)

Se Maven non riesce a scaricare la dipendenza da JitPack (es. per problemi di build del repo o mancanza di accesso), puoi procedere in modo manuale.

### 1. Scarica il file `embasp-7.1.0.jar`

Scaricalo dalla sezione *Releases* del repository:
🔗 https://github.com/DeMaCS-UNICAL/EmbASP-Java/releases/tag/v7.1.0

### 2. Inseriscilo nella cartella `libs/` del progetto (creala se non esiste)

Esempio:
```
LittleStars-AI/
├── libs/
│   └── embasp-7.1.0.jar
```

### 3. Installa la libreria nel tuo repository Maven locale

Esegui il seguente comando:

```bash
mvn install:install-file \
  -Dfile=libs/embasp-7.1.0.jar \
  -DgroupId=it.unical.mat \
  -DartifactId=embasp \
  -Dversion=7.1.0 \
  -Dpackaging=jar
```

### 4. Modifica il `pom.xml` per usare la versione installata localmente:

```xml
<dependency>
  <groupId>it.unical.mat</groupId>
  <artifactId>embasp</artifactId>
  <version>7.1.0</version>
</dependency>
```

---

## 📌 Note finali

- Questo approccio manuale **funziona solo localmente**, quindi ogni sviluppatore dovrà eseguire lo stesso comando se clona il progetto.
- In alternativa, puoi usare direttamente il file `.jar` con `systemPath`, ma è **sconsigliato**.

---

